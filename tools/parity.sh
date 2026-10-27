#!/usr/bin/env bash
# Parity check: run a world for N ticks with every optimization OFF, then ON (or only one), with
# every vanilla Random seeded, and diff the resulting world-state hashes (docs/methodology.md §1).
#
#   tools/parity.sh <world-dir> [ticks=6000] [--only <key>] [--vanilla]   (--vanilla: off vs off)
#
# Needs: the PrismLauncher instance below with the litemod already installed (tools/test-in-game.sh
# or a copy into mods/), and <world-dir>/pose.txt = "x y z yaw pitch" (optional). The instance's
# liteoptimizer.json and parity/run.properties are restored afterwards. The world is copied into
# the instance as saves/_parity for each run and removed at the end.
set -euo pipefail

WORLD_DIR=${1:?world dir}; shift
TICKS=6000; ONLY=""; VANILLA=0
while [ $# -gt 0 ]; do
  case "$1" in
    --only) ONLY=$2; shift 2 ;;
    --vanilla) VANILLA=1; shift ;;
    *) TICKS=$1; shift ;;
  esac
done

INSTANCE=${PARITY_INSTANCE:-"1.12.2 test ai"}
LAUNCHER=${PARITY_LAUNCHER:-prismlauncher}
AI_DESKTOP=${PARITY_DESKTOP:-/home/froyln/Dev/ai-desktop.sh}
PRISM_ROOT=${PARITY_PRISM_ROOT:-"$HOME/.local/share/PrismLauncher"}
INSTANCE_DIR="$PRISM_ROOT/instances/$INSTANCE"
MC="$INSTANCE_DIR/minecraft"
OPT="$MC/liteconfig/common/liteoptimizer.json"
RUN_CONFIG="$MC/parity/run.properties"
SAVE="_parity"
SEED=${PARITY_SEED:-1}
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="$ROOT/run/parity"; mkdir -p "$OUT"

[ -d "$WORLD_DIR/region" ] || { echo "$WORLD_DIR is not a world (no region/)"; exit 2; }
[ -f "$WORLD_DIR/level.dat" ] || { echo "$WORLD_DIR is not a world (no level.dat)"; exit 2; }
"$AI_DESKTOP" status >/dev/null 2>&1 || { echo "isolated Sway desktop is unavailable; start it before parity runs" >&2; exit 5; }
ss -ltn 2>/dev/null | grep -q '127\.0\.0\.1:5900' || { echo "isolated WayVNC is unavailable on 127.0.0.1:5900" >&2; exit 5; }
POSE=""; [ -f "$WORLD_DIR/pose.txt" ] && POSE=$(cat "$WORLD_DIR/pose.txt")

[ -f "$OPT" ] && cp "$OPT" "$OPT.parity-backup" || true
[ -f "$RUN_CONFIG" ] && cp "$RUN_CONFIG" "$RUN_CONFIG.parity-backup" || true
remove_save() { [ -d "$MC/saves/$SAVE" ] && rm -r "$MC/saves/$SAVE" || true; }
cleanup() {
  [ -f "$OPT.parity-backup" ] && mv -f "$OPT.parity-backup" "$OPT" || true
  [ -f "$RUN_CONFIG.parity-backup" ] && mv -f "$RUN_CONFIG.parity-backup" "$RUN_CONFIG" || rm -f "$RUN_CONFIG"
  rm -f "$OPT.parity-tmp" "$RUN_CONFIG.parity-tmp"
  remove_save
}
trap cleanup EXIT

write_config() {  # $1 = off|on
  python3 - "$1" "$ONLY" "$OPT" "$ROOT/src/main/java/dev/froyln/liteoptimizer/config/Option.java" <<'PY'
import json, os, re, sys
mode, only, path, options = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
keys = [k for k in json.load(open(path)) if not k.startswith("_")] if os.path.exists(path) else []
# keys the source knows but the instance's config file does not yet: a missing key means "enabled"
keys += [k for k in re.findall(r'^\s*[A-Z_0-9]+\("([a-z_0-9]+)",', open(options).read(), re.M) if k not in keys]
pillars = {"render", "world", "ai", "memory"}
dependencies = {"camera_visgraph_cache": {"sodium"}}
required = dependencies.get(only, set())
cfg = {}
for k in keys:
    if mode == "off":
        cfg[k] = k in required
    elif only:
        cfg[k] = (k in pillars) or (k == only) or (k in required)
    else:
        cfg[k] = True
with open(path + ".parity-tmp", "w") as f:
    json.dump(cfg, f, indent=2)
os.replace(path + ".parity-tmp", path)
PY
}

run_once() {  # $1 = label
  local label=$1
  local run_label="${label}-$$-${RANDOM}"
  remove_save; cp -r "$WORLD_DIR" "$MC/saves/$SAVE"
  mkdir -p "$MC/parity"
  rm -f "$MC/parity/$run_label.txt" "$MC/parity/$run_label.detail.txt"
  python3 - "$RUN_CONFIG" "$SEED" "$SAVE" "$TICKS" "$run_label" "$POSE" "${PARITY_VERBOSE:-}" "${PARITY_TRACE:-}" <<'PY'
import os, sys
path, seed, world, ticks, label, pose, verbose, trace = sys.argv[1:]
values = {
    "liteoptimizer.parity": seed,
    "liteoptimizer.parity.world": world,
    "liteoptimizer.parity.ticks": ticks,
    "liteoptimizer.parity.label": label,
    "liteoptimizer.nopause": "1",
}
if pose:
    values["liteoptimizer.parity.pose"] = pose.replace(" ", "_")
if verbose:
    values["liteoptimizer.parity.verbose"] = "1"
if trace:
    values["liteoptimizer.parity.trace"] = "1"
with open(path + ".parity-tmp", "w") as f:
    for key, value in values.items():
        f.write(key + "=" + value + "\n")
os.replace(path + ".parity-tmp", path)
PY
  if python3 - "$INSTANCE_DIR/natives" <<'PY'
import os, sys
needle = os.fsencode(os.path.realpath(sys.argv[1]))
for pid in os.listdir('/proc'):
    if not pid.isdigit() or pid == str(os.getpid()):
        continue
    try:
        command = open('/proc/' + pid + '/cmdline', 'rb').read()
    except (OSError, PermissionError):
        continue
    if b'org.prismlauncher.EntryPoint' in command and needle in command:
        sys.exit(0)
sys.exit(1)
PY
  then
    echo "[parity] this target instance is already running — close it first"; exit 4
  fi
  echo "[parity] run '$label' ($TICKS ticks) ..."
  if [ -n "${PARITY_PROFILE:-}" ]; then
    "$AI_DESKTOP" exec "$LAUNCHER" --dir "$PRISM_ROOT" --show-window --launch "$INSTANCE" --profile "$PARITY_PROFILE" >/dev/null 2>&1 &
  else
    "$AI_DESKTOP" exec "$LAUNCHER" --dir "$PRISM_ROOT" --show-window --launch "$INSTANCE" --offline ParityTester >/dev/null 2>&1 &
  fi
  local pid=$!
  local waited=0 seen=0
  local start_timeout=${PARITY_START_TIMEOUT:-600}
  # Wait for the harness to acknowledge this unique label; the launcher may reuse cached JVM args.
  while :; do
    if [ $seen -eq 1 ]; then
      if ! pgrep -f -- "-Djava.library.path=$INSTANCE_DIR/natives" >/dev/null; then break; fi
    elif grep -F "parity run '$run_label' started" "$MC/logs/latest.log" >/dev/null 2>&1; then
      seen=1
    fi
    if [ $seen -eq 0 ] && [ $waited -ge "$start_timeout" ]; then
      python3 - "$INSTANCE_DIR/natives" <<'PY'
import os, signal, sys
needle = os.fsencode("-Djava.library.path=" + os.path.realpath(sys.argv[1]))
for pid in os.listdir('/proc'):
    if not pid.isdigit():
        continue
    try:
        command = open('/proc/' + pid + '/cmdline', 'rb').read()
    except (OSError, PermissionError):
        continue
    if needle in command:
        os.kill(int(pid), signal.SIGTERM)
PY
      echo "[parity] target game did not acknowledge run '$run_label' — check $MC/logs/latest.log"; exit 3
    fi
    if [ $waited -ge 3600 ]; then
      echo "[parity] timed out waiting for '$label'"; exit 4
    fi
    sleep 5; waited=$((waited+5))
  done
  wait $pid 2>/dev/null || true
  [ -f "$MC/parity/$run_label.txt" ] || { echo "[parity] no dump for $label — check $MC/logs/latest.log"; exit 3; }
  cp "$MC/parity/$run_label.txt" "$OUT/$label.txt"
  [ -f "$MC/parity/$run_label.detail.txt" ] && cp "$MC/parity/$run_label.detail.txt" "$OUT/$label.detail.txt" || true
}

write_config off; run_once off
if [ $VANILLA = 1 ]; then write_config off; run_once off2; B=off2; else write_config on; run_once on; B=on; fi

if diff -q "$OUT/off.txt" "$OUT/$B.txt" >/dev/null; then
  echo "[parity] PASS — identical state after $TICKS ticks ($(tail -1 "$OUT/off.txt"))"
else
  echo "[parity] FAIL — differing lines:"; diff "$OUT/off.txt" "$OUT/$B.txt" | head -20; exit 1
fi

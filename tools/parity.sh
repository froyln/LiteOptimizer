#!/usr/bin/env bash
# Parity check: run a world for N ticks with every optimization OFF, then ON (or only one), with
# every vanilla Random seeded, and diff the resulting world-state hashes (docs/methodology.md §1).
#
#   tools/parity.sh <world-dir> [ticks=6000] [--only <key>] [--vanilla]   (--vanilla: off vs off)
#
# Needs: the PrismLauncher instance below with the litemod already installed (tools/test-in-game.sh
# or a copy into mods/), and <world-dir>/pose.txt = "x y z yaw pitch" (optional). The instance's
# instance.cfg and liteoptimizer.json are modified for the runs and restored afterwards. The world
# is copied into the instance as saves/_parity for each run and removed at the end.
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

INSTANCE="1.12.2 test ai"
INSTANCE_DIR="$HOME/.local/share/PrismLauncher/instances/$INSTANCE"
MC="$INSTANCE_DIR/minecraft"
CFG="$INSTANCE_DIR/instance.cfg"
OPT="$MC/liteconfig/common/liteoptimizer.json"
SAVE="_parity"
SEED=${PARITY_SEED:-1}
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="$ROOT/run/parity"; mkdir -p "$OUT"

[ -d "$WORLD_DIR/region" ] || { echo "$WORLD_DIR is not a world (no region/)"; exit 2; }
POSE=""; [ -f "$WORLD_DIR/pose.txt" ] && POSE=$(cat "$WORLD_DIR/pose.txt")

cp "$CFG" "$CFG.parity-backup"
[ -f "$OPT" ] && cp "$OPT" "$OPT.parity-backup" || true
remove_save() { [ -d "$MC/saves/$SAVE" ] && rm -r "$MC/saves/$SAVE" || true; }
cleanup() {
  mv -f "$CFG.parity-backup" "$CFG"
  [ -f "$OPT.parity-backup" ] && mv -f "$OPT.parity-backup" "$OPT" || true
  remove_save
}
trap cleanup EXIT

write_config() {  # $1 = off|on
  python3 - "$1" "$ONLY" "$OPT" <<'PY'
import json, os, sys
mode, only, path = sys.argv[1], sys.argv[2], sys.argv[3]
keys = [k for k in json.load(open(path)) if not k.startswith("_")] if os.path.exists(path) else []
pillars = {"render", "world", "ai", "memory"}
cfg = {}
for k in keys:
    if mode == "off":
        cfg[k] = False
    elif only:
        cfg[k] = (k in pillars) or (k == only)
    else:
        cfg[k] = True
json.dump(cfg, open(path, "w"), indent=2)
PY
}

run_once() {  # $1 = label
  local label=$1
  remove_save; cp -r "$WORLD_DIR" "$MC/saves/$SAVE"
  rm -f "$MC/parity/$label.txt"
  local jvm="-Dliteoptimizer.nopause=1 ${PARITY_VERBOSE:+-Dliteoptimizer.parity.verbose=1 -Dliteoptimizer.parity.trace=1} -Dliteoptimizer.parity=$SEED -Dliteoptimizer.parity.world=$SAVE -Dliteoptimizer.parity.ticks=$TICKS -Dliteoptimizer.parity.label=$label"
  [ -n "$POSE" ] && jvm="$jvm -Dliteoptimizer.parity.pose=$(printf '%s' "$POSE" | tr ' ' ',')"
  python3 - "$CFG" "$jvm" <<'PY'
import sys, re
path, jvm = sys.argv[1], sys.argv[2]
text = open(path).read()
text = re.sub(r"(?m)^OverrideJavaArgs=.*$", "OverrideJavaArgs=true", text)
if "OverrideJavaArgs=" not in text: text += "\nOverrideJavaArgs=true\n"
text = re.sub(r"(?m)^JvmArgs=.*$", "JvmArgs=" + jvm, text)
if "JvmArgs=" not in text: text += "JvmArgs=" + jvm + "\n"
open(path, "w").write(text)
PY
  if pgrep -f "[p]rismlauncher --launch" >/dev/null; then
    echo "[parity] a launched instance is already running — close it first"; exit 4
  fi
  echo "[parity] run '$label' ($TICKS ticks) ..."
  prismlauncher --launch "$INSTANCE" --offline ParityTester >/dev/null 2>&1 &
  local pid=$!
  local waited=0 dumped=0
  # the game exits by itself after writing the dump (ParityRun → Minecraft.shutdown)
  while kill -0 $pid 2>/dev/null; do
    sleep 5; waited=$((waited+5))
    if [ $waited -gt 3600 ]; then echo "[parity] timeout, killing pid $pid"; kill $pid 2>/dev/null || true; break; fi
    if [ -f "$MC/parity/$label.txt" ]; then
      dumped=$((dumped+5))
      if [ $dumped -gt 60 ]; then echo "[parity] dump written but game still open after 60 s — closing pid $pid"; kill $pid 2>/dev/null || true; break; fi
    fi
  done
  wait $pid 2>/dev/null || true
  [ -f "$MC/parity/$label.txt" ] || { echo "[parity] no dump for $label — check $MC/logs/latest.log"; exit 3; }
  cp "$MC/parity/$label.txt" "$OUT/$label.txt"; [ -f "$MC/parity/$label.detail.txt" ] && cp "$MC/parity/$label.detail.txt" "$OUT/" || true
}

write_config off; run_once off
if [ $VANILLA = 1 ]; then write_config off; run_once off2; B=off2; else write_config on; run_once on; B=on; fi

if diff -q "$OUT/off.txt" "$OUT/$B.txt" >/dev/null; then
  echo "[parity] PASS — identical state after $TICKS ticks ($(tail -1 "$OUT/off.txt"))"
else
  echo "[parity] FAIL — differing lines:"; diff "$OUT/off.txt" "$OUT/$B.txt" | head -20; exit 1
fi

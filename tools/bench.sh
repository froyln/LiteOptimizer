#!/usr/bin/env bash
# Benchmark: load a world, wait, record frame times + integrated-server MSPT with the
# optimizations OFF, then ON (docs/methodology.md §2), and print the summary + deltas.
#
#   tools/bench.sh <world-dir> [warmup_s=30] [seconds=60] [--only <key>|--compare <key>] [--off-only|--on-only] [--profile] [--jfr] [--heap] [--fly <blocks/tick>] [--keep] [--no-entities] [--no-screenshot]
#
# --keep: leave the played copy at <instance>/saves/_bench (to copy generated chunks back into the world dir)
#
# --profile: keep vanilla's section profiler running for the recording window and copy
#            bench/<label>.profile.txt (ms per frame per section, rendered chunk count) next to the csv.
#            This is the tool for the client thread: JFR barely samples it (LWJGL native calls).
# --heap:    once the recording starts: jcmd GC.run + GC.heap_info + top-40 GC.class_histogram →
#            run/bench/<world>-<label>.heap.txt (memory pillar numbers). Runs on the system JDK like --jfr.
# --jfr:     run the instance on the system OpenJDK 8 (JFR built in) and record a JFR profile of the
#            recording window with tools/profile.sh → docs/profiles/<world>-<label>.top.txt
#            (useful for the server thread).
#
# Same instance handling as tools/parity.sh (instance.cfg / liteoptimizer.json restored after).
# Output: run/bench/<world>-off.csv and -on.csv.
set -euo pipefail

WORLD_DIR=${1:?world dir}; shift
WARMUP=30; SECONDS_=60; ONLY=""; COMPARE=""; MODES="off on"; PROFILE=0; JFR=0; HEAP=0; FLY=0; KEEP=0; NOENT=0; SCREENSHOT=1
while [ $# -gt 0 ]; do
  case "$1" in
    --only) ONLY=$2; shift 2 ;;
    --compare) COMPARE=$2; shift 2 ;;
    --off-only) MODES="off"; shift ;;
    --on-only) MODES="on"; shift ;;
    --profile) PROFILE=1; shift ;;
    --jfr) JFR=1; shift ;;
    --heap) HEAP=1; shift ;;
    --fly) FLY=$2; shift 2 ;;
    --keep) KEEP=1; shift ;;
    --no-screenshot) SCREENSHOT=0; shift ;;
    --no-entities) NOENT=1; shift ;;  # screenshot parity: entities are per-run random
    *) if [ "$WARMUP" = 30 ] && [ -z "${W_SET:-}" ]; then WARMUP=$1; W_SET=1; else SECONDS_=$1; fi; shift ;;
  esac
done

INSTANCE=${BENCH_INSTANCE:-"1.12.2 test ai"}
PRISM_ROOT=${BENCH_PRISM_ROOT:-"$HOME/.local/share/PrismLauncher"}
LAUNCHER=${BENCH_LAUNCHER:-"prismlauncher"}
AI_DESKTOP=${BENCH_DESKTOP:-/home/froyln/Dev/ai-desktop.sh}
VNC_PORT=${BENCH_VNC_PORT:-5900}
INSTANCE_DIR="$PRISM_ROOT/instances/$INSTANCE"
MC="$INSTANCE_DIR/minecraft"
CFG="$INSTANCE_DIR/instance.cfg"
OPT="$MC/liteconfig/common/liteoptimizer.json"
RUN_CONFIG="$MC/parity/run.properties"
GAMEOPT="$MC/options.txt"
SAVE="_bench"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="$ROOT/run/bench"; mkdir -p "$OUT"
NAME=$(basename "$WORLD_DIR")
game_pids() {
  python3 - "$INSTANCE_DIR/natives" <<'PY'
import os, sys
needle = b"-Djava.library.path=" + os.fsencode(os.path.realpath(sys.argv[1]))
for pid in os.listdir("/proc"):
    if not pid.isdigit():
        continue
    try:
        command = open("/proc/" + pid + "/cmdline", "rb").read().split(b"\0")
    except OSError:
        continue
    if needle in command:
        print(pid)
PY
}
game_running() { [ -n "$(game_pids)" ]; }
stop_game() { game_pids | xargs -r kill -TERM; }

[ -d "$WORLD_DIR/region" ] || { echo "$WORLD_DIR is not a world (no region/)"; exit 2; }
"$AI_DESKTOP" status >/dev/null 2>&1 || { echo "isolated Sway desktop is unavailable; start it before benchmark runs" >&2; exit 5; }
ss -ltn 2>/dev/null | grep -q "127\\.0\\.0\\.1:$VNC_PORT" || { echo "isolated WayVNC is unavailable on 127.0.0.1:$VNC_PORT" >&2; exit 5; }
POSE=""; [ -f "$WORLD_DIR/pose.txt" ] && POSE=$(cat "$WORLD_DIR/pose.txt")

[ -z "$ONLY" ] || [ -z "$COMPARE" ] || { echo "[bench] --only and --compare are mutually exclusive"; exit 2; }
if game_running; then
  echo "[bench] Minecraft is running; close it before changing instance settings"; exit 4
fi
cp "$CFG" "$CFG.bench-backup"
cp "$GAMEOPT" "$GAMEOPT.bench-backup"
[ -f "$RUN_CONFIG" ] && cp "$RUN_CONFIG" "$RUN_CONFIG.bench-backup" || true
# uncapped frame rate for the recording (260 = "unlimited"); restored with the backup after
sed -i 's/^maxFps:.*/maxFps:260/' "$GAMEOPT"
[ -f "$OPT" ] && cp "$OPT" "$OPT.bench-backup" || true
remove_save() { [ -d "$MC/saves/$SAVE" ] && rm -r "$MC/saves/$SAVE" || true; }
cleanup() {
  mv -f "$CFG.bench-backup" "$CFG"
  mv -f "$GAMEOPT.bench-backup" "$GAMEOPT"
  [ -f "$RUN_CONFIG.bench-backup" ] && mv -f "$RUN_CONFIG.bench-backup" "$RUN_CONFIG" || rm -f "$RUN_CONFIG"
  [ -f "$OPT.bench-backup" ] && mv -f "$OPT.bench-backup" "$OPT" || true
  [ $KEEP = 1 ] || remove_save
}
trap cleanup EXIT

write_config() {  # $1 = off|on
  python3 - "$1" "$ONLY" "$OPT" "$ROOT/src/main/java/dev/froyln/liteoptimizer/config/Option.java" "$COMPARE" <<'PY'
import json, os, re, sys
mode, only, path, options, compare = sys.argv[1:]
keys = [k for k in json.load(open(path)) if not k.startswith("_")] if os.path.exists(path) else []
# keys the source knows but the instance's config file does not yet: a missing key means "enabled"
source_keys = re.findall(r'^\s*[A-Z_0-9]+\("([a-z_0-9]+)",', open(options).read(), re.M)
keys += [k for k in source_keys if k not in keys]
pillars = {"render", "world", "ai", "memory"}
cfg = {k: (False if mode == "off" else ((k in pillars) or (k == only)) if only else True) for k in keys}
if compare:
    if compare not in source_keys:
        raise SystemExit("Unknown comparison key: " + compare)
    cfg = {k: True for k in keys}
    cfg[compare] = mode == "on"
json.dump(cfg, open(path, "w"), indent=2)
PY
}

run_once() {  # $1 = label
  local label=$1
  remove_save; cp -r "$WORLD_DIR" "$MC/saves/$SAVE"
  rm -f "$MC/bench/$label.csv"; [ -d "$MC/bench/vtx-$label" ] && rm -r "$MC/bench/vtx-$label" || true
  mkdir -p "$(dirname "$RUN_CONFIG")"
  # parity=1 only for the main-menu auto-load / no-pause / tick gate; the seeding is harmless
  local jvm="-Dliteoptimizer.nopause=1 -Dliteoptimizer.parity=1 -Dliteoptimizer.parity.world=$SAVE -Dliteoptimizer.parity.ticks=999999999 -Dliteoptimizer.bench=$label -Dliteoptimizer.bench.warmup=$WARMUP -Dliteoptimizer.bench.seconds=$SECONDS_ -Dliteoptimizer.bench.quit=1"
  [ -n "$COMPARE" ] && jvm="$jvm -Dliteoptimizer.bench.unvalidated=$COMPARE"
  [ -n "$ONLY" ] && jvm="$jvm -Dliteoptimizer.bench.unvalidated=$ONLY"
  [ $SCREENSHOT = 1 ] && jvm="$jvm -Dliteoptimizer.screenshot=$NAME-$label"
  [ $PROFILE = 1 ] && jvm="$jvm -Dliteoptimizer.bench.profile=1"
  [ "$FLY" != 0 ] && jvm="$jvm -Dliteoptimizer.bench.fly=$FLY"
  [ $NOENT = 1 ] && jvm="$jvm -Dliteoptimizer.bench.noentities=1"
  [ -n "${EXTRA_JVM:-}" ] && jvm="$jvm $EXTRA_JVM"  # experiments, e.g. EXTRA_JVM=-Dliteoptimizer.dev.x=1
  [ -n "$POSE" ] && jvm="$jvm -Dliteoptimizer.parity.pose=$(printf '%s' "$POSE" | tr ' ' '_')"
  python3 - "$RUN_CONFIG" "$NAME" "$SAVE" "$WARMUP" "$SECONDS_" "$label" "$PROFILE" "$FLY" "$NOENT" "$POSE" "$SCREENSHOT" "$COMPARE" "$ONLY" <<'PY'
import sys
path, name, world, warmup, seconds, label, profile, fly, noentities, pose, screenshot, compare, only = sys.argv[1:]
values = {
    "liteoptimizer.parity": "1",
    "liteoptimizer.parity.world": world,
    "liteoptimizer.parity.ticks": "999999999",
    "liteoptimizer.parity.label": label,
    "liteoptimizer.bench": label,
    "liteoptimizer.bench.warmup": warmup,
    "liteoptimizer.bench.seconds": seconds,
    "liteoptimizer.bench.quit": "1",
}
if screenshot == "1": values["liteoptimizer.screenshot"] = name + "-" + label
if profile == "1": values["liteoptimizer.bench.profile"] = "1"
if fly != "0": values["liteoptimizer.bench.fly"] = fly
if noentities == "1": values["liteoptimizer.bench.noentities"] = "1"
if pose: values["liteoptimizer.parity.pose"] = pose.replace(" ", "_")
if compare or only: values["liteoptimizer.bench.unvalidated"] = compare or only
with open(path, "w") as f:
    for key, value in values.items(): f.write(key + "=" + value + "\n")
PY
  python3 - "$CFG" "$jvm" "$((JFR|HEAP))" <<'PY'
import sys, re, json
path, jvm, profile = sys.argv[1], sys.argv[2], sys.argv[3] == "1"
text = open(path).read()
def setkv(text, key, value):
    if re.search(r"(?m)^" + key + "=", text):
        return re.sub(r"(?m)^" + key + "=.*$", key + "=" + value, text)
    return text + "\n" + key + "=" + value + "\n"
text = setkv(text, "OverrideJavaArgs", "true")
text = setkv(text, "JvmArgs", json.dumps(jvm))
if profile:
    text = setkv(text, "OverrideJavaLocation", "true")
    text = setkv(text, "JavaPath", "/usr/lib/jvm/java-8-openjdk/bin/java")
open(path, "w").write(text)
PY
  if game_running; then
    echo "[bench] the target instance is already running — close it first"; exit 4
  fi
  # Run-local launcher roots are disposable. Restart them to discard cached optional JVM flags.
  if [[ "$PRISM_ROOT" == "$ROOT/run/"* || "$(basename "$LAUNCHER")" == *MultiMC ]]; then
    python3 - "$PRISM_ROOT" "$(basename "$LAUNCHER")" <<'PY_LAUNCHER'
import os, pathlib, signal, sys, time
root, launcher = os.path.realpath(sys.argv[1]), sys.argv[2]
multimc = launcher.endswith('MultiMC')
for entry in pathlib.Path('/proc').iterdir():
    if not entry.name.isdigit():
        continue
    try:
        args = (entry / 'cmdline').read_bytes().split(b'\0')
        args = [os.fsdecode(arg) for arg in args if arg]
        if not args or os.path.basename(args[0]) != ('MultiMC' if multimc else 'prismlauncher'):
            continue
        if multimc and '/ai-desktop.service' not in (entry / 'cgroup').read_text():
            continue
        dir_arg = next((i for i, arg in enumerate(args[:-1]) if arg in ('--dir', '-d')), None)
        if dir_arg is None or os.path.realpath(args[dir_arg + 1]) != root:
            continue
        os.kill(int(entry.name), signal.SIGTERM)
        for attempt in range(100):
            if not entry.exists():
                break
            time.sleep(0.1)
        else:
            raise SystemExit('Disposable launcher did not exit; refusing cached flags')
    except (FileNotFoundError, ProcessLookupError):
        pass
PY_LAUNCHER
  fi
  echo "[bench] run '$label' (warmup ${WARMUP}s, record ${SECONDS_}s) ..."
  local stamp="$OUT/.launch-stamp"; touch "$stamp"; sleep 1
  rm -f "$OUT/$NAME-$label.heap.txt" "$MC/bench/$label.profile.txt" "$MC/bench/$label.chunks.txt"  # never copy a previous run's files
  if [[ "$(basename "$LAUNCHER")" == *MultiMC ]]; then
    "$AI_DESKTOP" exec "$LAUNCHER" --dir "$PRISM_ROOT" --launch "$INSTANCE" >/dev/null 2>&1 &
  else
    "$AI_DESKTOP" exec "$LAUNCHER" --dir "$PRISM_ROOT" --launch "$INSTANCE" --offline BenchTester >/dev/null 2>&1 &
  fi
  # wait_recording: block until *this* launch's log (newer than the stamp, so a previous run with
  # the same label cannot match) says the recording window started.
  wait_recording() {
    local startup_waited=0
    until [ "$MC/logs/latest.log" -nt "$stamp" ] && rg -q "bench '$label' recording" "$MC/logs/latest.log" 2>/dev/null; do
      if [ $startup_waited -ge 600 ]; then return 1; fi
      sleep 2; startup_waited=$((startup_waited+2))
    done
  }
  wait_recording || { echo "[bench] target instance did not start recording '$label'"; exit 3; }
  if [ $JFR = 1 ]; then
    ( wait_recording || exit 0
      PROFILE_PID="$(game_pids | head -n1)" "$ROOT/tools/profile.sh" "$NAME-$label" "$((SECONDS_ - 5))" >/dev/null 2>&1 || echo "[bench] profile failed for $label" ) &  # JFR starts a few s into the window; must end before the game quits
  fi
  if [ $HEAP = 1 ]; then
    ( wait_recording || exit 0
      jcmd=/usr/lib/jvm/java-8-openjdk/bin/jcmd; mcpid=$(game_pids | head -n1 || true)
      [ -n "$mcpid" ] || { echo "[bench] heap: game pid not found for $label"; exit 0; }
      { "$jcmd" "$mcpid" GC.run; "$jcmd" "$mcpid" GC.run; "$jcmd" "$mcpid" GC.heap_info; "$jcmd" "$mcpid" GC.class_histogram; } \
        > "$OUT/$NAME-$label.heap.txt" 2>&1 || echo "[bench] heap dump failed for $label" ) &
  fi
  local waited=0 dumped=0
  while game_running; do
    sleep 5; waited=$((waited+5))
    if [ $waited -gt 1800 ]; then echo "[bench] timeout, stopping target game"; stop_game; break; fi
    if [ -f "$MC/bench/$label.csv" ]; then
      dumped=$((dumped+5))
      if [ $dumped -gt 90 ]; then echo "[bench] csv written but game still open — stopping target game"; stop_game; break; fi
    fi
  done
  [ -f "$MC/bench/$label.csv" ] || { echo "[bench] no csv for $label — check $MC/logs/latest.log"; exit 3; }
  cp "$MC/bench/$label.csv" "$OUT/$NAME-$label.csv"
  cp "$OPT" "$OUT/$NAME-$label.effective.config.json"
  cp "$MC/logs/latest.log" "$OUT/$NAME-$label.log"
  cp "$GAMEOPT" "$OUT/$NAME-$label.options.txt"
  [ -f "$MC/bench/$label.profile.txt" ] && cp "$MC/bench/$label.profile.txt" "$OUT/$NAME-$label.profile.txt" || true
  [ -f "$MC/bench/$label.chunks.txt" ] && cp "$MC/bench/$label.chunks.txt" "$OUT/$NAME-$label.chunks.txt" || true
  if [ -d "$MC/bench/vtx-$label" ]; then rm -r "$OUT/$NAME-vtx-$label" 2>/dev/null || true; mv "$MC/bench/vtx-$label" "$OUT/$NAME-vtx-$label"; fi
}

for mode in $MODES; do
  write_config $mode
  cp "$OPT" "$OUT/$NAME-$mode.config.json"
  run_once $mode
done
if [ "$MODES" = "off on" ]; then
  python3 "$ROOT/tools/bench-summary.py" "$OUT/$NAME-off.csv" "$OUT/$NAME-on.csv"
else
  python3 "$ROOT/tools/bench-summary.py" "$OUT/$NAME-$MODES.csv"
fi

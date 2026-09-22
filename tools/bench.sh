#!/usr/bin/env bash
# Benchmark: load a world, wait, record frame times + integrated-server MSPT with the
# optimizations OFF, then ON (docs/methodology.md §2), and print the summary + deltas.
#
#   tools/bench.sh <world-dir> [warmup_s=30] [seconds=60] [--only <key>] [--off-only|--on-only] [--profile] [--jfr] [--heap] [--fly <blocks/tick>] [--keep] [--no-entities]
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
WARMUP=30; SECONDS_=60; ONLY=""; MODES="off on"; PROFILE=0; JFR=0; HEAP=0; FLY=0; KEEP=0; NOENT=0
while [ $# -gt 0 ]; do
  case "$1" in
    --only) ONLY=$2; shift 2 ;;
    --off-only) MODES="off"; shift ;;
    --on-only) MODES="on"; shift ;;
    --profile) PROFILE=1; shift ;;
    --jfr) JFR=1; shift ;;
    --heap) HEAP=1; shift ;;
    --fly) FLY=$2; shift 2 ;;
    --keep) KEEP=1; shift ;;
    --no-entities) NOENT=1; shift ;;  # screenshot parity: entities are per-run random
    *) if [ "$WARMUP" = 30 ] && [ -z "${W_SET:-}" ]; then WARMUP=$1; W_SET=1; else SECONDS_=$1; fi; shift ;;
  esac
done

INSTANCE="1.12.2 test ai"
INSTANCE_DIR="$HOME/.local/share/PrismLauncher/instances/$INSTANCE"
MC="$INSTANCE_DIR/minecraft"
CFG="$INSTANCE_DIR/instance.cfg"
OPT="$MC/liteconfig/common/liteoptimizer.json"
GAMEOPT="$MC/options.txt"
SAVE="_bench"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="$ROOT/run/bench"; mkdir -p "$OUT"
NAME=$(basename "$WORLD_DIR")

[ -d "$WORLD_DIR/region" ] || { echo "$WORLD_DIR is not a world (no region/)"; exit 2; }
POSE=""; [ -f "$WORLD_DIR/pose.txt" ] && POSE=$(cat "$WORLD_DIR/pose.txt")

cp "$CFG" "$CFG.bench-backup"
cp "$GAMEOPT" "$GAMEOPT.bench-backup"
# uncapped frame rate for the recording (260 = "unlimited"); restored with the backup after
sed -i 's/^maxFps:.*/maxFps:260/' "$GAMEOPT"
[ -f "$OPT" ] && cp "$OPT" "$OPT.bench-backup" || true
remove_save() { [ -d "$MC/saves/$SAVE" ] && rm -r "$MC/saves/$SAVE" || true; }
cleanup() {
  mv -f "$CFG.bench-backup" "$CFG"
  mv -f "$GAMEOPT.bench-backup" "$GAMEOPT"
  [ -f "$OPT.bench-backup" ] && mv -f "$OPT.bench-backup" "$OPT" || true
  [ $KEEP = 1 ] || remove_save
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
cfg = {k: (False if mode == "off" else ((k in pillars) or (k == only)) if only else True) for k in keys}
json.dump(cfg, open(path, "w"), indent=2)
PY
}

run_once() {  # $1 = label
  local label=$1
  remove_save; cp -r "$WORLD_DIR" "$MC/saves/$SAVE"
  rm -f "$MC/bench/$label.csv"; [ -d "$MC/bench/vtx-$label" ] && rm -r "$MC/bench/vtx-$label" || true
  # parity=1 only for the main-menu auto-load / no-pause / tick gate; the seeding is harmless
  local jvm="-Dliteoptimizer.nopause=1 -Dliteoptimizer.parity=1 -Dliteoptimizer.parity.world=$SAVE -Dliteoptimizer.parity.ticks=999999999 -Dliteoptimizer.bench=$label -Dliteoptimizer.bench.warmup=$WARMUP -Dliteoptimizer.bench.seconds=$SECONDS_ -Dliteoptimizer.bench.quit=1 -Dliteoptimizer.screenshot=$NAME-$label"
  [ $PROFILE = 1 ] && jvm="$jvm -Dliteoptimizer.bench.profile=1"
  [ "$FLY" != 0 ] && jvm="$jvm -Dliteoptimizer.bench.fly=$FLY"
  [ $NOENT = 1 ] && jvm="$jvm -Dliteoptimizer.bench.noentities=1"
  [ -n "${EXTRA_JVM:-}" ] && jvm="$jvm $EXTRA_JVM"  # experiments, e.g. EXTRA_JVM=-Dliteoptimizer.dev.x=1
  [ -n "$POSE" ] && jvm="$jvm -Dliteoptimizer.parity.pose=$(printf '%s' "$POSE" | tr ' ' '_')"
  python3 - "$CFG" "$jvm" "$((JFR|HEAP))" <<'PY'
import sys, re
path, jvm, profile = sys.argv[1], sys.argv[2], sys.argv[3] == "1"
text = open(path).read()
def setkv(text, key, value):
    if re.search(r"(?m)^" + key + "=", text):
        return re.sub(r"(?m)^" + key + "=.*$", key + "=" + value, text)
    return text + "\n" + key + "=" + value + "\n"
text = setkv(text, "OverrideJavaArgs", "true")
text = setkv(text, "JvmArgs", jvm)
if profile:
    text = setkv(text, "OverrideJavaLocation", "true")
    text = setkv(text, "JavaPath", "/usr/lib/jvm/java-8-openjdk/bin/java")
open(path, "w").write(text)
PY
  if pgrep -f "[p]rismlauncher --launch" >/dev/null; then
    echo "[bench] a launched instance is already running — close it first"; exit 4
  fi
  echo "[bench] run '$label' (warmup ${WARMUP}s, record ${SECONDS_}s) ..."
  local stamp="$OUT/.launch-stamp"; touch "$stamp"; sleep 1
  rm -f "$OUT/$NAME-$label.heap.txt" "$MC/bench/$label.profile.txt" "$MC/bench/$label.chunks.txt"  # never copy a previous run's files
  prismlauncher --launch "$INSTANCE" --offline BenchTester >/dev/null 2>&1 &
  local pid=$!
  # wait_recording: block until *this* launch's log (newer than the stamp, so a previous run with
  # the same label cannot match) says the recording window started; false if the game died first
  wait_recording() {
    until [ "$MC/logs/latest.log" -nt "$stamp" ] && rg -q "bench '$label' recording" "$MC/logs/latest.log" 2>/dev/null; do
      sleep 2; kill -0 $pid 2>/dev/null || return 1
    done
  }
  if [ $JFR = 1 ]; then
    ( wait_recording || exit 0
      "$ROOT/tools/profile.sh" "$NAME-$label" "$((SECONDS_ - 5))" >/dev/null 2>&1 || echo "[bench] profile failed for $label" ) &  # JFR starts a few s into the window; must end before the game quits
  fi
  if [ $HEAP = 1 ]; then
    ( wait_recording || exit 0
      jcmd=/usr/lib/jvm/java-8-openjdk/bin/jcmd; mcpid=$(pgrep -f "liteoptimizer.bench=$label" | head -1 || true)
      [ -n "$mcpid" ] || { echo "[bench] heap: game pid not found for $label"; exit 0; }
      { "$jcmd" "$mcpid" GC.run; "$jcmd" "$mcpid" GC.run; "$jcmd" "$mcpid" GC.heap_info; "$jcmd" "$mcpid" GC.class_histogram | head -102; } \
        > "$OUT/$NAME-$label.heap.txt" 2>&1 || echo "[bench] heap dump failed for $label" ) &
  fi
  local waited=0 dumped=0
  while kill -0 $pid 2>/dev/null; do
    sleep 5; waited=$((waited+5))
    if [ $waited -gt 1800 ]; then echo "[bench] timeout, killing pid $pid"; kill $pid 2>/dev/null || true; break; fi
    if [ -f "$MC/bench/$label.csv" ] && ! pgrep -f "liteoptimizer.bench=$label" >/dev/null; then break; fi
    if [ -f "$MC/bench/$label.csv" ]; then
      dumped=$((dumped+5))
      if [ $dumped -gt 90 ]; then echo "[bench] csv written but game still open — closing pid $pid"; kill $pid 2>/dev/null || true; break; fi
    fi
  done
  wait $pid 2>/dev/null || true
  [ -f "$MC/bench/$label.csv" ] || { echo "[bench] no csv for $label — check $MC/logs/latest.log"; exit 3; }
  cp "$MC/bench/$label.csv" "$OUT/$NAME-$label.csv"
  [ -f "$MC/bench/$label.profile.txt" ] && cp "$MC/bench/$label.profile.txt" "$OUT/$NAME-$label.profile.txt" || true
  [ -f "$MC/bench/$label.chunks.txt" ] && cp "$MC/bench/$label.chunks.txt" "$OUT/$NAME-$label.chunks.txt" || true
  if [ -d "$MC/bench/vtx-$label" ]; then rm -r "$OUT/$NAME-vtx-$label" 2>/dev/null || true; mv "$MC/bench/vtx-$label" "$OUT/$NAME-vtx-$label"; fi
}

for mode in $MODES; do write_config $mode; run_once $mode; done
if [ "$MODES" = "off on" ]; then
  python3 "$ROOT/tools/bench-summary.py" "$OUT/$NAME-off.csv" "$OUT/$NAME-on.csv"
else
  python3 "$ROOT/tools/bench-summary.py" "$OUT/$NAME-$MODES.csv"
fi

#!/usr/bin/env bash
# Record a JFR CPU profile of the running game and print the hottest frames.
# Needs the instance to run OpenJDK 8u262+ (set JavaPath=/usr/lib/jvm/java-8-openjdk/bin/java
# in the instance's instance.cfg; the bundled Oracle jre-legacy 8u202 needs
# -XX:+UnlockCommercialFeatures instead). Attach tools come from the system JDK 8 / 21.
#
# Usage: tools/profile.sh <label> [seconds]   →  docs/profiles/<label>.jfr + <label>.top.txt
set -euo pipefail
LABEL=${1:?label}; SECS=${2:-60}
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="$ROOT/docs/profiles"; mkdir -p "$OUT"
JCMD=/usr/lib/jvm/java-8-openjdk/bin/jcmd
JFR=/usr/lib/jvm/java-21-openjdk/bin/jfr

PID=${PROFILE_PID:-$(pgrep -f '[j]ava-8-openjdk/bin/java .*launchwrapper|[j]re-legacy/bin/java .*launchwrapper' | head -n1)}
[ -n "$PID" ] || { echo "game not running" >&2; exit 1; }
echo "recording pid $PID for ${SECS}s → $OUT/$LABEL.jfr"
"$JCMD" "$PID" JFR.start name="$LABEL" duration="${SECS}s" filename="$OUT/$LABEL.jfr" settings=profile >/dev/null
until [ -s "$OUT/$LABEL.jfr" ] && ! "$JCMD" "$PID" JFR.check 2>/dev/null | grep -q "name=$LABEL.*running"; do sleep 2; done

# top self frames (first frame of each sample) and top inclusive frames, both by sample count
set +o pipefail  # `head` closes the pipe early; that is not an error
"$JFR" print --events jdk.ExecutionSample "$OUT/$LABEL.jfr" > "$OUT/$LABEL.samples.txt"
{
  echo "# $LABEL — $(date -Is) — ${SECS}s"; echo; echo "## top self frames"
  awk '/stackTrace = \[/{getline; sub(/^[ \t]+/, ""); sub(/line:.*/, ""); print}' "$OUT/$LABEL.samples.txt" | sort | uniq -c | sort -rn | head -40
  echo; echo "## top inclusive frames"
  awk '/stackTrace = \[/{f=1; next} /^[ \t]*\]/{f=0} f{sub(/^[ \t]+/, ""); sub(/line:.*/, ""); print}' "$OUT/$LABEL.samples.txt" | sort | uniq -c | sort -rn | head -60
} > "$OUT/$LABEL.top.txt"
rm "$OUT/$LABEL.samples.txt"
cat "$OUT/$LABEL.top.txt"

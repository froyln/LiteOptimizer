#!/usr/bin/env bash
# Install the freshly built litemod into the PrismLauncher instance "1.12.2 test ai" (LiteLoader
# 1.12.2 + the technical mod stack, assets already downloaded) and launch it offline.
# Preferred over `./gradlew runClient`, whose asset download stalls on this network.
#
# Usage: ./gradlew build && tools/test-in-game.sh
set -euo pipefail

INSTANCE="1.12.2 test ai"
AI_DESKTOP=${TEST_AI_DESKTOP:-/home/froyln/Dev/ai-desktop.sh}
MODS_DIR="$HOME/.local/share/PrismLauncher/instances/$INSTANCE/minecraft/mods/1.12.2"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$AI_DESKTOP" status >/dev/null 2>&1 || { echo "isolated Sway desktop is unavailable; start it before launching Minecraft" >&2; exit 5; }
ss -ltn 2>/dev/null | grep -q '127\.0\.0\.1:5900' || { echo "isolated WayVNC is unavailable on 127.0.0.1:5900" >&2; exit 5; }
[ -d "$MODS_DIR" ] || { echo "Instance mods folder not found: $MODS_DIR" >&2; exit 1; }
litemod="$(ls -t "$ROOT"/build/libs/liteoptimizer-liteloader-*.litemod 2>/dev/null | head -n1)"
[ -n "$litemod" ] || { echo "No litemod in build/libs/ - run ./gradlew build first." >&2; exit 1; }

rm -f "$MODS_DIR"/liteoptimizer-liteloader-*.litemod
cp "$litemod" "$MODS_DIR/"
echo "Installed $(basename "$litemod") into $MODS_DIR"

exec "$AI_DESKTOP" exec prismlauncher --launch "$INSTANCE" --offline LiteOptTest --show-window

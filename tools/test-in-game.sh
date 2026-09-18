#!/usr/bin/env bash
# Install the freshly built litemod into the PrismLauncher instance "1.12.2 test ai" (LiteLoader
# 1.12.2 + the technical mod stack, assets already downloaded) and launch it offline.
# Preferred over `./gradlew runClient`, whose asset download stalls on this network.
#
# Usage: ./gradlew build && tools/test-in-game.sh
set -euo pipefail

INSTANCE="1.12.2 test ai"
MODS_DIR="$HOME/.local/share/PrismLauncher/instances/$INSTANCE/minecraft/mods/1.12.2"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

[ -d "$MODS_DIR" ] || { echo "Instance mods folder not found: $MODS_DIR" >&2; exit 1; }
litemod="$(ls -t "$ROOT"/build/libs/liteoptimizer-liteloader-*.litemod 2>/dev/null | head -n1)"
[ -n "$litemod" ] || { echo "No litemod in build/libs/ - run ./gradlew build first." >&2; exit 1; }

rm -f "$MODS_DIR"/liteoptimizer-liteloader-*.litemod
cp "$litemod" "$MODS_DIR/"
echo "Installed $(basename "$litemod") into $MODS_DIR"

exec prismlauncher --launch "$INSTANCE" --offline LiteOptimizerTester --show-window

# Revision 96 full-stack B1 render smoke — 2026-10-15

Artifact SHA-256 is recorded in `artifact-sha256.txt`. I launched it through the existing isolated Prism clone with the target 1.12.2 technical mod profile and opened `B1-rev96-render-smoke`, a disposable copy of `B1-storage-smoke`. The game log confirms LiteOptimizer and the full installed mod/resource-pack set loaded. The log also confirms `render/tile_entity_frustum` and `render/model_display_lists` were enabled.

The rendered storage scene showed item frames and visible block entities (including the redstone/lever display and the frame wall) with those options enabled. The client then returned to the title screen through Save and Quit; the original B1 fixture was not opened. `storage-scene.png` and `client.log` preserve the observed session, and `liteoptimizer.json` records the option values.

This is a focused visual compatibility smoke, not a full pixel-parity comparison or a complete per-mod feature matrix. It supports the existing isolated display-list gain evidence and the documented tile-entity-frustum tradeoff; it does not independently prove the visibility bounds at every camera angle or close the release gate. The log contains existing missing-texture and MaLiLib configuration warnings; they did not prevent world join or rendering.

## Revision 96 follow-up

The exact artifact was relaunched in the same disposable full-stack clone and rejoined this B1
copy. MiniHUD Info Lines toggled ON and then OFF via its `H` hotkey; the ON screenshot and client
log are `rev96-minihud-info-lines-on.png` and `rev96-minihud-client.log`. The OFF toast was
visually confirmed before Save and Quit. No blocks were changed. This adds one exact-revision
MiniHUD interaction, not a pass for the remaining MiniHUD features or full compatibility matrix.

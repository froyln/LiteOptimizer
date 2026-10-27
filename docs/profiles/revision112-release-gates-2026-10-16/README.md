# Frozen revision 112 release-gate session — 2026-10-16

Artifact SHA-256: `f4f331643d280fc9c90aaacc538ed4037ecdf79bd12639c1ce8c50a7bd2d3592`; the installed archive in disposable Prism clone `1.12.2 test ai parity rev44` matches. No product source changed in this session. `frozen-source-sha256.json` records source/build inputs; `frozen-payload-sha256.json` records archive entries with only the automatic manifest revision normalized. Rebuilds that change only metadata do not require repeated functional tests.

## Newly completed runtime checks

All visual/input checks used headless Sway through loopback WayVNC; the owner's desktop was untouched. A persistent VNC connection avoids per-connection virtual-seat focus loss. The initial vanilla checks used the existing disposable unmodified Mojang 1.12.2 fixture, restarted only after process/port checks showed it stopped.

- **Terrain streaming:** Survival walking from `(0.5,4,0.5)` to `(269.899,4,-216.642)`; F3 records X chunk 0→16 and Z chunk 0→-14, with rendered terrain at the start/middle/end. RCON `stream112` increased from 0 to 35,240 cm. The fixture teleport set the start position only; the crossing used held W, not distant teleports. The second scoreboard query is the final result; the earlier query was taken while walking. No movement warning or reset occurred during the walk. `streaming-*.png`, `streaming-rcon.txt`.
- **Dimension travel and clear rendering:** a valid 4×5 obsidian frame at x268–271, y3–7, z-214 was ignited by the server; the client walked into it and waited. RCON confirms Dimension -1, while `portal-arrived.png` clearly renders the Nether scene at `(26.5,25,7.5)`, without a transition overlay. The server logged a dimension-conversion speed warning, also reproduced by the historical LiteLoader-only control. This supersedes the earlier rev112 visually incomplete portal attempt.
- **F3+T resource reload:** held F3/T triggered `Reloading ResourceManager` at client time 16:05:58; atlases and worker threads restarted, then the world rendered normally (`resource-reload-rendered.png`). The selected resource packs still report missing texture files; this check does not claim to repair those assets. `client-vanilla.log` preserves the reload.
- **ItemScroller and inventory transfer:** in a vanilla chest at Nether `(23,25,7)`, one upward wheel notch over slot 0 moved one cobblestone to the player's existing stack. RCON verifies chest 64→63 and player 1→2. A downward notch returned it; RCON verifies chest 64 and player 1. Before/after/returned screenshots and `itemscroller-rcon.txt` prove both directions. The initial downward notch with no transferable extra player item changed nothing. The initial fixture commands without `execute` targeted the Overworld and are retained in the transcript; the successful Nether fixture uses `execute LiteOptTest`.

These results supplement rev112's already recorded join/reconnect, ordinary movement, small corrections, placement/breaking, real villager trade, saved inventory and potion-use checks. The vanilla interaction checklist now has functional evidence on this code payload. This session disconnected through the menu without a connection reset; earlier unexplained resets remain a risk rather than a diagnosed mod defect. The full B1 installed-mod matrix remains open.

## Optimization evidence audit

A read-only reviewer checked all nine release-default options against the catalog and retained profiles. Recorded gain and boundary/parity evidence exists for every option; unvalidated options remain normalized off. Do not re-run these merely to replace manifest revision numbers.

| option | retained gain | authoritative evidence |
|---|---|---|
| sodium | RD16 frame 3.43→1.38 ms (-59.7%), slowest 1% -52.5%; RD10 -22.4%/-40.2% | `../revision95-sodium-B6-rd16-repeat-2026-10-15/README.md` and RD10 sibling; only sodium differs |
| model_display_lists | frame 6.26→5.14 ms (-17.8%), slowest 1% -12.7%; full-image OFF/ON and OFF/OFF zero pixels | `../0.6.0-rc2-2026-10-13/README.md` |
| tile_entity_frustum | frame 6.05→4.07 ms (-32.7%), slowest 1% -59.7% | `../tile-entity-frustum-B1-2026-10-14/README.md`; client draw omission documented; stable-save parity replaces nondeterministic B1 |
| camera_visgraph_cache | three-pair median frame -3.4%, slowest 1% -7.7%, graph -48–51% | `../camera-visgraph-cache-B6-2026-10-14/README.md`; small/noise-sensitive gain qualified |
| blockstate_property_storage | Guava objects -10.5 MB, old-gen -14 MB | `../../catalog.md`, `../../history/plan-through-2026-10-13.md`; raw `run/bench/mem-blockstate_property_storage.heap.txt` versus off |
| baked_quad_dedup | -2.2 MB, unique quads 43,690→29,214 | same catalog/history; raw `run/bench/mem-baked_quad_dedup.heap.txt`; this session's reload reproduces the quad counts |
| property_hashcode_cache | frame -21%, slowest 1% -50%, MSPT 3.78→3.07 ms | catalog/history; B3/B4 parity |
| hopper_entity_lookup | MSPT 3.75→3.22 ms (-14%) | catalog/history; B2/B3/B4 parity; order/RNG retained |
| structure_lookup_index | B4 MSPT -30% | catalog/history; B4 parity; original raw attribution less complete than recent profiles |

Revision101's 6,000-tick OFF/OFF and OFF/ON stable-save dumps match at `9494ea25e3dee2acba33a46502beb2369ea15a1ccc1797f0ba7130c729ee6341`. Subsequent queue-only render changes do not alter those shared/world/memory implementations. Bundled Sodium gains support the sodium option as a whole, not separate per-subfeature claims.

## B1 feature checks

A fresh `B1-release-rev112` copy was made from the disposable clone's `B1-storage-smoke`; only the copy was opened/edited. It renders at the recorded benchmark pose (`b1-benchmark-pose.png`). The later beacon fixture and remote movement modify this copy only.

- IsoRenders' installed revision11 has F6/F7 export keys conflicting with the stack. Temporarily remapped to P/O, it exported a 1024² pickaxe and furnace PNG at client time16:13:14. Both exported images were inspected (`isorenders-item.png`, `isorenders-block.png`) and the world still rendered afterward. This tests the installed build, not the newer IsoRenders source checkout.
- VoxelMap's Hide Minimap was initially ON. With it OFF and F3 hidden, the B1 terrain clearly renders in the minimap (`voxelmap-minimap.png`). The initially empty full map populated asynchronously; it was not a persistent rendering failure.
- LiteTweaker Disable Beacon Beam ON visibly removed the fixture and other visible beams; OFF restored them (`beacon-beams-on.png`, `litetweaker-beams-disabled.png`, `litetweaker-beams-restored.png`). The setting was restored OFF.
- BBOutline Y displayed green/red outlines around the fixture scene and Y removed them (`bboutline-on.png`, `bboutline-off.png`), restoring its initial state.
- MiniHUD Beacon Range ON visibly drew translucent range volumes around active beacons (`minihud-beacon-range.png`); restored OFF. Spawn Chunks (Real) visibly drew its red/green grid around the world's recorded spawn (-2031,4,560). Chunk Unload Order displayed labels before and after a relocation from x-1967.5 to x-1599.5; the labels changed with the newly loaded area (`minihud-spawnchunks-unload-before.png`, `minihud-unload-after.png`). This is a functional overlay/update smoke, not exact order validation. Both toggles were restored OFF.

For WorldEditCUI, a separate loopback Forge server uses the cached Forge14.23.5.2859 universal/dependencies and [official WorldEdit6.1.10](https://www.curseforge.com/minecraft/mc-mods/worldedit/files/2941712), SHA-256 `cd4768ded9e2b527e3fc093fdf4ce07f058187de7961bdc0d7fa821c1b756f75`. Server files are disposable under `run/worldedit-cui-fixture`; accepted EULA was copied from the existing test server. It loads WorldEdit and accepts the existing LiteLoader client as a vanilla connection without changing the client mod set. In its disposable B1 copy, `//pos1 -1978,30,709` and `//pos2 -1975,34,712` produce an 80-block selection; `worldedit-cui-selection.png` clearly shows its red grid and corner. No selection operation changed blocks. This is WorldEditCUI compatibility evidence, separate from unmodified-server compatibility.

The full named feature matrix remains incomplete. UsefulMod's installed `/tm` class is an unregistered no-op, so it cannot provide meaningful command coverage; test its actual rendering toggles instead. Baseline-incompatible TellMe/Model Gap Fix remain limitations.

UsefulMod actual rendering features passed on the frozen client in the disposable WorldEdit B1 fixture. A diamond item frame at (-1978,31,712) shows its wooden frame normally; `showItemFrameFrame` OFF removes the wooden geometry while retaining the diamond, and ON restores it (`usefulmod-frame-*.png`). Temporary K conflicted with Tweakeroo tool-switch, so it was toggled back and semicolon used instead; the frame binding was reset afterward. With `shulkerBoxDisplay` ON, hovering a shulker containing seven diamonds and holding Shift displays the 27-slot preview with seven diamonds (`usefulmod-shulker-preview.png`); the option was restored OFF. UsefulMod client-command coverage is N/A for this installed build, whose only command implementation is unregistered and empty. Temporary bindings are restored during session cleanup.

Additional focused checks: Litematica loaded the 93-block crafting station, rendered All Layers, then Single Layer Y=30 and Y=31 with visibly different schematic blocks (`litematica-layer30.png`, `litematica-layer31.png`), restored All and removed the temporary placement. Carpet Client piston visualizer ON plus empty-hand Use on an unextended piston displayed “Pushes 1 Blocks”; second Use removed the preview (`carpet-piston-preview*.png`). This works locally on the WorldEdit fixture; Carpet server sync retains earlier independent evidence.

Tweakeroo Fast Block Placement and Fast Left Click were enabled. Held Use placed a plank and held attack removed that plank and the backing stone, confirmed as air by RCON (`tweakeroo-fast-*.png`, `tweakeroo-fast-break-rcon.txt`). This is functional compatibility coverage, not a placement-rate benchmark. Hand Restock ON in Survival replenished the held planks after a placement: one held +16 in inventory became16 held +one placed; server NBT confirmed16 in hotbar slot0 (`tweakeroo-restock*`). Flexible placement rotation held X showed its overlay and placed a piston with server data5; this validates the input/render path, not every orientation or accurate-placement protocol.

## Final verification and cleanup

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited0 in10s. Build113 SHA-256 `115010902e5d6e711fe65a358da63c9c7de2928ebd31da80034c18846f84f7cd`; `final-build-compare.json` proves identical archive entry sets and bytes except the automatic manifest revision112→113. Source/build-input hashes are unchanged. The runtime evidence therefore applies to build113 without a redundant reinstall/retest.

Client quit cleanly at17:00:35. Original options/keybindings and MiniHUD/LiteTweaker/UsefulMod/Tweakeroo configurations were restored byte-for-byte from saved copies in the disposable clone. VoxelMap Hide Minimap and Carpet pistonVisualizer were restored true/false respectively. Both local dedicated fixtures were stopped through RCON; the isolated desktop remains available. No owner world or normal desktop was used.

Still open: Tweakeroo dropped-shulker stacking, particle suppression, tile-entity distance override and remaining item-frame/hologram variations; MiniHUD visibly generated structure boxes; full B1 matrix certification. Earlier resets remain unexplained, though this long grouped session had no reset. TellMe/MaLiLib and Model Gap Fix fail in the baseline and cannot be counted as working. No tag or release-complete claim is made.

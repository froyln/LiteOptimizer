# Tile-entity culling attribution — B1, 2026-10-13

**Decision:** frustum culling has a repeated incremental frame-average benefit, but render
parity remains inconclusive; do not certify it for release yet. Ordinary Sodium TE occlusion
is redundant, and this test does not establish a gain; keep it release-ineligible.

## Conditions and reproducibility

Installed artifact SHA-256: `b9e5d1f346175b253190d513eab696776c1cd5e77eb8cad819fbeb8de5f2a94c`.
Source: `091d4e1` working tree with the production parity-settings fix. Full installed litemod
manifest: `mods.sha256`. Same artifact throughout; no product code changed in this evaluation.
Fixture: disposable copies of `~/Dev/OptimizationMod-1.12.2-worlds/B1-storage`.
Pose: `-1970 38 700 45 30` (recovered storage pose, different from the previous entity test).
Isolated Sway/WayVNC, configured Prism legacy Java, NVIDIA, uncapped FPS, 892×952 captures.
Both halves hold Sodium, display lists, and the other TE-culling switch on; saved JSON configs
confirm the only changed key. Sodium config normalization forces the parity backend/settings.
MiniHUD info lines were disabled for all runs. `--no-entities` keeps item frames/paintings
and TESRs, while suppressing moving entities. This is a static B1 scene test, not every TESR.

Long comparisons (20 s warmup, 40 s recording, `--profile --no-entities`):

```
tools/bench.sh run/eval/te-frustum-B1 20 40 --compare tile_entity_frustum --profile --no-entities
tools/bench.sh run/eval/te-occlusion-B1 20 40 --compare tile_entity_occlusion --profile --no-entities
```

| pair | frame avg off→on | slowest 1% avg off→on | TESR section off→on | full-image RGB differences |
|---|---|---|---|---:|
| Frustum, original scene | 5.74→4.69 ms (-18.3%) | 12.51→14.45 ms (worse) | 3.673→1.745 ms/frame | 24 |
| Occlusion, frustum held on | 4.66→4.56 ms (-2.0%) | 14.43→14.16 ms | 1.977→1.902 ms/frame | 21 |
| Frustum confirmation, particles disabled | 5.77→4.74 ms (-17.8%) | 16.15→14.73 ms | 3.693→1.687 ms/frame | 17 |

The two long frustum pairs support average-frame gain and a roughly halved TESR section cost.
They do not support a consistent tail-latency claim: the first pair worsened its 1% metric,
and both sets have outlier frames. The single 2% occlusion result does not establish a repeatable
benefit and cannot override the redundant ordinary-path source trace below.

## Pixel comparison diagnosis

Unchanged original frustum-off versus a new frustum-off control: **19 differing pixels**.
The control used the same pose/settings and a 20 s warmup/10 s recording; it diagnoses captures,
not performance. The original setup is not deterministic enough for a 0-pixel verdict.

A confirmation held Tweakeroo `YeetToggles.disableParticles=true` in both halves. This repeated
the gain but left 17 differences. A final capture-focused pair also held `disableItemGlint=true`
(20 s warmup/10 s recording). Its full-image RGB difference is **9 pixels** at:
`(226,309), (227,309), (225,310), (43,319), (45,322), (46,323), (47,324), (47,325), (39,456)`.
Vanilla `RenderItem.renderEffect` uses `Minecraft.getSystemTime()` for glint offsets, so it was
reasonable to control glint, but these controls did not resolve the remaining mismatch.

No pixels were masked, cropped, or accepted as an exception. This is **not** a render-parity
pass. The initial off/off mismatch establishes a capture reproducibility problem; a control
under the final settings is still needed before attributing the remaining pixels to frustum
culling. The short final pair's performance numbers are retained but are not the gain claim.
Compare saved PNGs and `pixel-comparisons.json`. Stop broad test-setting changes; next diagnosis
must inspect the residual display pixels and the final-settings off/off control.

## Why ordinary TE occlusion is redundant

`ChunkRenderManager.iterateChunks` obtains the visible section list from the graph culler.
`addChunk` → `addEntitiesToRenderLists` appends that section's TEs to `visibleBlockEntities`.
`SodiumWorldRenderer.renderTileEntities` dispatches that list. The helper's section loop includes
each TE's own position section, so `isChunkVisible` is true and the helper returns false.
Global renderers are exempt. No ordinary renderer can be skipped by this extra test.

Damaged-block rendering and foreign dispatcher calls are separate contexts, not covered by
that argument. Also, the helper's section range ends at `pos+1`, whereas frustum bounds extend
to `pos+2`; the catalog's earlier assertion that they used the identical box is unsupported for
those contexts. Read-only source review confirmed the redundancy and this proof gap.

## Restoration and validation

All five runner invocations exited 0. Instance options/JVM/LiteOptimizer settings, saved Sodium
JSON, MiniHUD and Tweakeroo config, and the prior `_bench` save were restored. Original worlds
were never played or modified. Config restoration was checked byte-for-byte. The tested jar
remains installed; no Minecraft process remains running.
Java 8 `./gradlew build --offline` exited 0 in 6 s. Detailed CSVs, profiles, configs, PNGs,
summaries and pixel counts are retained here. Final read-only evidence review found no material inaccuracies; `git diff --check` passed.
Full installed-mod compatibility remains a gate.

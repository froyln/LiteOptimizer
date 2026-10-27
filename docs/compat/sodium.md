# Sodium pillar × instance mods — compat matrix (task 0.3a)

Inputs: Vintagium's 43 injection sites (`~/Dev/references/Vintagium/.../mixin`, extracted by
regex) × `docs/compat/instance-mixins.md` (all 639 foreign hooks) × the decompiled mixins of
Tweakeroo, Litematica (forgeflower) for exact `@At` anchors × every mod's `mixins*.json`
(`priority`, `injectors.defaultRequire`).

Facts that shape every resolution:

- **No installed mod `@Overwrite`s a render class.** The only overwrites in the whole set are
  Carpet Client `EntityPlayerSP`, UsefulMod `GuiScreen` / `TileEntityPiston`. Newlight uses
  injects/redirects only (the earlier "Newlight overwrites lighting" note in the catalog was
  wrong — still off-limits for *our* code, but there is no two-overwrite crash risk).
- Most configs use `priority 1000`; Sodium's `MixinWorldRenderer` explicitly uses `priority 500`.
  Lower-priority mixins are applied later, so that class can replace Tweakeroo's earlier
  `RenderGlobal.setupTerrain` injections. Tweakeroo sets `defaultRequire=0`, so missing anchors are
  skipped instead of crashing. Do not infer compatibility from successful startup alone.
- `defaultRequire`: Tweakeroo **0** (missing anchors are skipped silently), Litematica /
  MiniHUD / malilib / tellme / connect / chatcalc / itemscroller / schematicpreview **1**
  (missing anchor = crash), Carpet Client / Newlight / UsefulMod / LiteTweaker / VoxelMap /
  BBOutline unset (Mixin default 0).

| vanilla method Vintagium changes | how | foreign hooks on it | resolution |
|---|---|---|---|
| `RenderGlobal.setupTerrain` | HEAD inject + cancel | Tweakeroo: `@Inject` at `FIELD frustumUpdatePosX`, `@Inject` after `INVOKE ViewFrustum.updateChunkPositions`, `@Redirect RenderChunk.needsImmediateUpdate` (all inside the body); Tweakeroo `@Inject` around the call in `EntityRenderer.renderWorldPass` | Sodium uses `@Inject` + cancel instead of overwriting this hooked method. Tweakeroo's old/new perimeter rebuild uses vanilla `ViewFrustum` and is not needed by Sodium's absolute-coordinate section grid. Its spectator flag is needed, though: Free Camera sets `CameraUtils.setFreeCameraSpectator(true)` around `setupTerrain` so the camera can see through opaque blocks. An optional `@Pseudo` bridge tracks that flag and passes it to Sodium's culler; without it, the camera's visibility graph can stop inside an opaque section. Exact-artifact ON/OFF and camera movement/boundary checks remain required. Tweakeroo's `forceChunkRendersOnMainThread` redirect is separately bridged in `ChunkRenderManager`; when enabled, queued Sodium rebuilds execute synchronously on the render thread. Per-section build revisions reject late async results during upload filtering. |
| `RenderGlobal.renderBlockLayer` | **overwrite** (same signature) | Litematica `@Inject` after each of the 4 *calls* in `EntityRenderer.renderWorldPass` (`require=1`) | calls remain in `renderWorldPass` (Vintagium doesn't touch it) ⇒ Litematica's schematic layer rendering fires exactly as before. |
| `RenderGlobal.updateChunks`, `renderEntities` | untouched / inject at `FIELD renderInfos` | Litematica `@Inject` after the calls in `renderWorldPass`; UsefulMod `isOutlineActive`/`playEvent` members | unchanged. |
| `RenderGlobal.markBlocksForUpdate` (private) | **overwrite** | none directly; Litematica `notifyBlockUpdate` RETURN, Newlight/Carpet/Tweakeroo `notifyLightSet` HEAD | those methods keep their bodies and call our `markBlocksForUpdate` ⇒ block/light updates reach Sodium's rebuild queue; foreign injects unaffected. |
| `RenderGlobal.loadRenderers`, `setWorldAndLoadRenderers`, `<init>`, `setDisplayListEntitiesDirty` | inject / redirect `renderDistanceChunks` | Litematica `loadRenderers` RETURN (`require=1`) | coexisting injects. |
| `RenderGlobal.hasNoChunkUpdates`, `getRenderedChunks`, `getDebugInfoRenders` | **overwrite** | none | — |
| `RenderGlobal.renderSky(FI)` | inject | VoxelMap `renderSky` member (inject, no overwrite) | coexisting injects. |
| `RenderGlobal.updateClouds` | untouched (Vintagium's cloud work is in `GameSettings.shouldRenderClouds`) | VoxelMap member | — |
| `ChunkRenderDispatcher.<init>` | redirect `Queues.newArrayBlockingQueue` (pool size) | Tweakeroo `updateTransparencyLater` HEAD | different methods. Note: with Sodium the vanilla dispatcher is idle; Tweakeroo's transparency tweak becomes inert (it forces vanilla resorts on the main thread). Document. |
| `BufferBuilder.sortVertexData` | **overwrite** | Carpet Client 1.4d has a `BufferBuilder` mixin with no refmap entries (adds members only) | coexist. |
| `BlockModelRenderer.renderModel` | **overwrite** (vanilla path still used for TESR/item rendering; terrain uses Sodium's own pipeline) | none | — |
| `BlockRendererDispatcher.getModelForState` | Vintagium **bypasses** it (`BlockModelShapes.getModelForState` directly in `ChunkRenderRebuildTask`) | Tweakeroo + LiteTweaker inject here (`alwaysRenderBarrierModel` etc.) | **port change:** call `BlockRendererDispatcher.getModelForState(state)` in our rebuild task so those tweaks keep affecting terrain. |
| `Frustum.isBoundingBoxInFrustum` | **overwrite** | none | — |
| `EnumFacing.getFacingFromVector` | **overwrite** | Newlight `IMixinEnumFacing` (accessor) | accessor only; coexist. |
| `BiomeColorHelper.getColorAtPos` | **overwrite** | none | — |
| `MultipartBakedModel.getQuads`, `WeightedBakedModel.getQuads`, `BakedQuad.<init>`, `UnpackedBakedQuad` | overwrite / inject | none (`UnpackedBakedQuad` is Forge — dropped in the port) | — |
| `Minecraft.isAmbientOcclusionEnabled`, `GameSettings.shouldRenderClouds` | **overwrite** | none on these members (UsefulMod/BBOutline hook other `GameSettings` members) | — |
| `ParticleManager.renderParticles/renderLitParticles` | inject at `INVOKE Particle.renderParticle` | Tweakeroo `addEffect`/`addBlockDestroyEffects`, UsefulMod `addBlockDestroyEffects` | different methods. |
| `RenderManager.renderEntityStatic` | inject + redirect `getBrightnessForRender` | UsefulMod `renderEntityStatic` member (inject) | coexist. |
| `RenderPainting.doRender` | inject | none | — |
| `ChunkProviderClient.loadChunk/unloadChunk/tick/setChunkMapCenter` | inject | Litematica `unloadChunk` RETURN (`require=1`), MiniHUD `unloadChunk` call site in `WorldClient` | coexist; `unloadChunk` keeps its body. |
| `WorldClient.refreshVisibleChunks`, `doVoidFogParticles` | inject / redirect `new Random` | LiteTweaker `doVoidFogParticles` member, Carpet `invalidateRegionAndSetBlock`, MiniHUD `doPreChunk` | coexist. |
| `EntityRenderer.renderWorld` (HEAD inject), `renderRainSnow` (redirect `fancyGraphics`) | inject / redirect | Tweakeroo `renderWorld` HEAD/RETURN/after `getMouseOver`, `renderRainSnow` HEAD; Carpet/BBOutline/Litematica/malilib/LiteTweaker/VoxelMap/Tweakeroo on `renderWorldPass` | all injects; coexist. |
| `GuiOverlayDebug.renderDebugInfoLeft/Right` | inject | Litematica `GuiOverlayDebug` call | coexist. |
| `BlockColors`, `ItemColors` `<init>`/`register…` | inject | UsefulMod `BlockColors$4/$5` (anonymous handler mixins) | coexist. |
| `TileEntityRendererDispatcher.render` | untouched by Vintagium; `tile_entity_frustum` injects HEAD and `tile_entity_occlusion` injects before the nested renderer call | Tweakeroo + UsefulMod inject there (Tweakeroo redirects `getMaxRenderDistanceSquared`; UsefulMod redirects `getDistanceSq`) | the occlusion hook runs after the range-check calls and does not cancel at HEAD; full-stack runtime smoke remains required. |

Behaviour that changes under the Sodium pillar even though nothing crashes (write into the
release notes; all restored by `"render": false`):

1. Tweakeroo `rebuildChunksAroundCamera` body hooks are inert; its camera-spectator signal is bridged into Sodium's chunk culler. Free Camera movement and dirty-chunk behavior still need exact-artifact smoke tests.
2. Tweakeroo `forceChunkRendersOnMainThread` now selects synchronous Sodium section builds, while `updateTransparencyLater` remains inert because Sodium replaces the vanilla dispatcher. Runtime checks are still required for the bridge; disable the Sodium pillar (`"render": false`) to restore vanilla dispatcher behavior.
3. Litematica's *own* schematic renderer is untouched (it has its own `RenderGlobal`-like
   classes on its schematic world); only the real world's terrain goes through Sodium.

Port rules derived from this (task 0.3b):

- Keep every overwritten method's signature; never remove `updateChunks`, `renderBlockLayer`,
  `renderEntities`, `loadRenderers`, `notifyBlockUpdate`, `notifyLightSet`, `unloadChunk`.
- Route model lookup through `BlockRendererDispatcher.getModelForState`.
- Our config already registers first (file name); do not raise `priority` — raising it would
  make foreign injects apply *before* our overwrites and be erased.
- Mixin 0.7.5: drop `@Dynamic`, `conformVisibility`, set `minVersion 0.7.4`.

---

# Port report (task 0.3b, 2026-09-18)

Vintagium (`me.jellysquid.mods.sodium.{client,common}` + `repack.joml`) ported to LiteLoader /
Mixin 0.7.5, mixins under `dev.froyln.liteoptimizer.mixin.render.sodium.*`, gated by
`render/sodium`. What Sodium keeps: the section graph, worker-thread meshing (`ChunkBuilder`,
cloned world slices), one VBO per section, particle culling, the fast buffer-builder sort and
vertex-format init. What is replaced so the frame is vanilla's, bit for bit:

| vanilla behaviour | Sodium/Vintagium did | what ships |
|---|---|---|
| block meshing: `BlockModelRenderer` smooth/flat + `AmbientOcclusionFace`, `BlockFluidRenderer` | own light pipelines, per-vertex biome blending, quad re-orientation, own fluid renderer | vanilla's algorithms verbatim into Sodium's buffers (`pipeline/BlockRenderer`, `pipeline/FluidRenderer`); vanilla's own `AmbientOcclusionFace` class via an inner-class mixin; positions rounded once as `BufferBuilder.putPosition`/`pos` (block pos + offset as doubles) |
| vertex layout `DefaultVertexFormats.BLOCK` (28 B: pos 3f, colour 4ub, uv 2f, light 2s) | compact half-float format, encoded lightmap coords | the same 28-byte layout (`SFPModelVertexType`), raw lightmap shorts — every section's bytes equal vanilla's (`tools/vtx-diff.py`: 485/485 sections identical on B1) |
| draw: `VboRenderList` — matrix stack, `RenderChunk`'s 1.000001 seam matrix, client arrays, one `glDrawArrays` per section | GLSL program, VAOs, `glMultiDrawArrays`, region multidraw | `ChunkRenderBackendFixedFunction`: vanilla's draw path per section (`glMultiDrawArrays` was ~10× a plain draw per range on NVIDIA; production selects only the fixed-function backend) |
| one buffer per layer in `getAllInBoxMutable` order (x, y, z); translucent sorted back-to-front, stable, centroid distance, resorted after a 1-block move | per-face slices, unstable sort, resort only within 96 blocks, block-face culling | block order, one buffer per pass (`use_block_face_culling` off by default), the same sort, resort everywhere |
| `setupTerrain` graph search: first parent wins, `EnumFacing` order, closed-pocket rule, wrapped root enumeration above/below the world, exact AABB frustum test | culling state merged from every parent, sorted roots, 1.125-block frustum margin | vanilla's search (`ChunkGraphCuller`): same section set, same order |
| sections built as soon as their chunk loads; unloaded neighbours are the blank chunk (air, sky 15, plains) | wait for the 8 neighbours | build immediately (`ChunkRenderContainer.canRebuild`) — otherwise the outer ring of the render distance is missing |
| `Chunk.getLightFor` in a missing section: sky light above the heightmap only; `ChunkCache` outside the build height: default light | default light everywhere | `ClonedChunkSection` carries the heightmap for missing sections |
| a global block entity (beacon, end gateway) is in the section list **and** the global set — drawn twice per frame | once | twice (the additive beam glow is brighter that way) |
| `BlockRendererDispatcher.getModelForState` | `BlockModelShapes` directly | the dispatcher (Tweakeroo/LiteTweaker model tweaks keep working) |

Result: **0 differing pixels** vanilla-vs-mod at the B1 (base: beacons, water streams, sea
lanterns, stained glass, 300 sections) and B3 poses under the protocol in `docs/methodology.md`
§ render parity, with the vanilla-vs-vanilla control also at 0.

Known, documented deviations (none of them change a pixel at a fixed pose):

- Sodium's meshing runs on worker threads and uploads when done; vanilla builds nearest
  sections on the main thread (`updateChunkNow`). A moving camera can see a section appear a
  frame earlier or later than in vanilla.
- The vanilla renderer's `renderInfos`, `ChunkRenderDispatcher`, `ViewFrustum` and
  `RenderChunk`s are idle; mods that read them see empty structures (compat matrix above).
- Production parity settings (enforced 2026-10-13): multidraw, compact vertex format, block-face
  culling, and fog occlusion are forced off on config load/save; translucency sorting is forced
  on. Their controls are removed. Old JSON cannot opt into the non-parity paths. Backend reference
  classes remain in the port, but production config selects SFP/fixed-function rendering.

Performance (uncapped, `docs/bench/README.md`): B6 flight over ocean/islands at RD 10, frame
1.66 → 1.06 ms (−36 %), 1 % low 8.22 → 6.00 ms (−27 %), client-thread root 1.35 → 0.73
ms/frame; B1 base pose −2 % avg / −13 % 1 % low (block entities dominate);
B3 (15 sections) unchanged.

The exact revision-92 artifact was also measured on B6 flight at RD 16: Sodium off/on averaged
2.77/0.96 ms per frame (−65.4%) and 1% low was 11.07/4.61 ms (−58.4%). This is one paired
run and an aggregate pillar comparison, not isolated evidence for every Sodium feature; repeat
before treating the magnitude as stable. See the [revision-92 profile](../profiles/revision92-sodium-B6-2026-10-15/README.md).

### Shulker placement/removal scheduling fix (2026-10-17)

Transparency sorts must not cancel a full rebuild whose result is awaiting upload, or copy its
old block-entity list under a newer task revision. `ChunkRenderContainer` tracks the applied data
revision, and both sort scheduling and submission reject sections with a pending build/newer
rebuild request. The submission check also covers stale sort-queue entries. Sorting resumes
after the latest result is applied. This changes only client render task ownership; no vanilla
hooks, simulation, packets or player actions change. Focused failing-before/passing-after
scheduler proof and packaged-mod placement/removal/movement smoke are recorded in
[`shulker-render-updates-2026-10-17`](../profiles/shulker-render-updates-2026-10-17/README.md).

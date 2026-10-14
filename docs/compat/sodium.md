# Sodium pillar × instance mods — compat matrix (task 0.3a)

Inputs: Vintagium's 43 injection sites (`~/Dev/references/Vintagium/.../mixin`, extracted by
regex) × `docs/compat/instance-mixins.md` (all 638 foreign hooks) × the decompiled mixins of
Tweakeroo, Litematica (forgeflower) for exact `@At` anchors × every mod's `mixins*.json`
(`priority`, `injectors.defaultRequire`).

Facts that shape every resolution:

- **No installed mod `@Overwrite`s a render class.** The only overwrites in the whole set are
  Carpet Client `EntityPlayerSP`, UsefulMod `GuiScreen` / `TileEntityPiston`. Newlight uses
  injects/redirects only (the earlier "Newlight overwrites lighting" note in the catalog was
  wrong — still off-limits for *our* code, but there is no two-overwrite crash risk).
- All configs are `priority 1000`. Same priority ⇒ application order = config registration
  order (LiteLoader mod load order, file name alphabetical: `liteoptimizer` < `litematica` <
  `tweakeroo`), so **our overwrites land first and their injections are applied to our body**.
- `defaultRequire`: Tweakeroo **0** (missing anchors are skipped silently), Litematica /
  MiniHUD / malilib / tellme / connect / chatcalc / itemscroller / schematicpreview **1**
  (missing anchor = crash), Carpet Client / Newlight / UsefulMod / LiteTweaker / VoxelMap /
  BBOutline unset (Mixin default 0).

| vanilla method Vintagium changes | how | foreign hooks on it | resolution |
|---|---|---|---|
| `RenderGlobal.setupTerrain` | **overwrite** | Tweakeroo: `@Inject` at `FIELD frustumUpdatePosX`, `@Inject` after `INVOKE ViewFrustum.updateChunkPositions`, `@Redirect RenderChunk.needsImmediateUpdate` (all inside the body); Tweakeroo `@Inject` after the *call* in `EntityRenderer.renderWorldPass` | body anchors vanish → Tweakeroo `require=0` ⇒ skipped, no crash. Those three implement `rebuildChunksAroundCamera` (free-camera chunk repositioning on vanilla `ViewFrustum`); Sodium positions its render regions from the render-view entity itself, so the feature is inherent. The call-site inject in `renderWorldPass` keeps working (method still called with the same descriptor). **Verify free camera in the checklist.** |
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
| `TileEntityRendererDispatcher.render` | untouched by Vintagium; our `render/tile_entity_*` culling rows inject HEAD | Tweakeroo + UsefulMod inject there (Tweakeroo redirects `getMaxRenderDistanceSquared`) | all HEAD injects; order irrelevant (each may cancel). |

Behaviour that changes under the Sodium pillar even though nothing crashes (write into the
release notes; all restored by `"render": false`):

1. Tweakeroo `rebuildChunksAroundCamera` hooks are inert — free camera must be re-verified.
2. Tweakeroo `updateTransparencyLater`/`forceChunkRendersOnMainThread`-style tweaks are inert.
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

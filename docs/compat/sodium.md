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

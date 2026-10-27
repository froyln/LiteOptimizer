# Minecraft 1.12 optimization methods versus LiteOptimizer

Historical scope note: this research preceded the owner's 2026-10-14 policy change. Its
strict pixel/client-RNG rejection criteria are superseded by `AGENTS.md`: client-only visual
changes may now be reconsidered. Source observations and lack of measured gains remain valid;
shared/server simulation, network compatibility and player actions still require vanilla behavior.

Research date: 2026-10-14. Compared upstream source in `~/Dev/references`, official
repositories/documentation, and the current working tree (base `7c5e1ee` plus the
existing audit fixes). This is method selection, not a benchmark or release pass.
Local upstream revisions are recorded in `catalog.md`; they were not updated here.
Live web pages may describe newer revisions. Comparisons below describe the inspected
1.12 code unless explicitly marked otherwise.

## What counts as better here

A method must preserve game state, RNG consumption, update order, and rendered pixels.
Lower CPU cost, lower retained heap, or fewer frame spikes must be demonstrated on our
workload. A faster Forge modpack implementation is not automatically useful on LiteLoader.
Logic changes in this client mod benefit the integrated server, not a remote server's TPS.
No upstream mod was installed during this research.

## Method comparison

| upstream | how it optimizes | what LiteOptimizer actually uses | decision |
|---|---|---|---|
| FoamFix | Compact property/state lookup structures; model deduplication; cheaper item, entity, property and hopper access; GPU-resident animation frames | `StateTable` replaces per-state neighbor tables with a shared mixed-radix state array; `BakedQuadDedup` shares exact vanilla quads; `ItemStack.isEmpty`, property hash caching and array-indexed entity data reads are registered | Same families, not identical implementations. Keep measured wins. Investigate GPU animation copies only if texture uploads are expensive. |
| VintageFix | FerriteCore-style blockstates; smaller model lists; dynamic model loading/unloading; texture/resource loading improvements; lower allocation and chunk-access cost | Same shared-state-table idea and whole-quad dedup; no dynamic model loader, model-list compaction, primitive VisGraph queue, or general chunk-access patch | Strong source for small allocation fixes. Dynamic resources are a substantial Forge-oriented rewrite; do not assume their large-pack savings transfer to this instance. |
| VanillaFix | Compact blockstates, activity-marked texture animations, optional dynamic resources, crash handling and bug fixes | Shared state tables already cover the central blockstate idea; `SpriteUtil.markSpriteActive` remains a no-op | Do not copy inactive-animation skipping: inspected code does not advance inactive sprites. Crash recovery and bug fixes are separate scope. |
| LoliASM / FermiumASM | Canonicalize strings/vertex arrays; change NBT/recipe structures; trim loading caches; release sprite frame storage; activity-marked animations; many Forge patches | Whole vanilla quads are canonicalized; recipe results are memoized; no general string/NBT canonicalization or launch-cache cleanup | Some memory ideas remain. Recipe map replacement and inactive animations need different parity-safe designs; much of the loading machinery is Forge-specific. FermiumASM is in the same family, not an independent set of gains to add. |
| Vintagium / Relictium | Sodium-family terrain graph, worker meshing, reusable buffers, optimized vertex writing/sorting, biome/light caches, multiple GPU backends | Substantial Vintagium port is present and registered, including worker buffers, terrain graph, biome cache, particle filtering and sorting | Already our renderer foundation. Relictium fixes can be evaluated individually. Shader/compact/multidraw paths are excluded from production because our pixel gate failed. |
| Nothirium / RenderLib | Alternative terrain renderer, asynchronous compile tasks, dynamic VBOs and GL15/20/42/43 backends | Same broad pipeline strategy through Vintagium, not Nothirium's implementation | A second renderer rewrite adds conflicts and maintenance. Compare individual storage/upload ideas only when measured; more advanced GL does not establish better performance on our GPU. |
| FastFurnace | Per-furnace positive/negative recipe cache plus replacement furnace update logic | Shared `(item, metadata)` result memo preserves the original scan on misses and original furnace ticking; invalidates on recipe adds and permanently bypasses caching when the mutable map escapes | Same lookup-saving idea, narrower integration. Shared memo can reuse results across furnaces; upstream's bounded per-furnace cache uses different memory tradeoffs. Neither is universally faster without measurement. |
| FastWorkbench | Remember last matching recipe; reduce crafting searches; replace container, GUI and networking behavior | No crafting cache mixin registered | Candidate only if crafting scans are hot. A last recipe still matching does not prove no earlier-priority recipe now matches; retain vanilla first-match selection and packet behavior. |
| BetterFps | Alternate trig algorithms; hopper pickup suppression; rewritten beacon update/rendering | We keep vanilla math, index inventory entities for hopper queries, and have no BetterFps beacon patch | Reject alternate math and blanket pickup suppression. Inspected FastBeacon updates structure every 200 ticks and effects halfway through; vanilla uses world time modulo 80. Rendering-only beam work is a separate candidate. |
| Carpet 1.12 | Per-chunk collision collection, TNT movement reuse, TE sleeping, random-tick/redstone/lighting changes, individually configurable rules | Per-chunk collision collection and narrow entity indices; no sleeping, TNT-result reuse, or redstone rewrite | Borrow narrow mechanics-preserving methods. Rule labels alone do not prove parity; sleeping requires every wake path, movement reuse requires complete block-change invalidation. |
| StellarCore | Smaller/custom collections, pooling/canonicalization, parallel loading and random ticks, mod-specific patches, cheaper pending-tick list handling | Property hash caches, narrow entity indices, whole-quad sharing, and per-chunk scheduled-tick index | Our pending-tick index avoids the global scan; the inspected StellarCore patch only changes the result list allocation. This is an algorithmic advantage for many scheduled ticks, not a measured speed comparison. Reject parallel random ticks. |
| ParticleCulling | Background visibility checks and conditional particle rendering | Current-frame synchronous frustum test for both normal/lit particles; expanded bounds; particle ticking remains vanilla | Same goal, different timing. A background check can be stale; our approach avoids that delay but particle bounds/renderer side effects still need appropriate scene tests. |
| OptiFine | Chunk-update controls, rendering changes, animation/detail switches, texture features and shaders | Terrain pipeline replacement and settings, but no OptiFine integration | Its official feature list mixes optimization with quality/behavior choices. Disabling effects is not a parity optimization. Closed implementation was not audited or ranked against ours. |
| Newlight / other lighting engines | Replace lighting propagation/storage paths | Lighting deliberately belongs to installed Newlight, not LiteOptimizer | Keep zero overlap. Another lighting implementation is not an additional optimization to stack here. |
| AI-Improvements / Clumps | AI task removal/math caching, or combining XP entities | No production AI mixins registered; item merging only skips a query when vanilla cannot merge a full stack, exempting max-size-1 items for Tweakeroo | AI removal and XP merging change behavior. AI-Improvements' current README explicitly describes that tradeoff; exact 1.12 controller code was not audited here. |

Implementation presence above was checked against `mixins.liteoptimizer.json`, not just
`Option.java`. Several options exist without corresponding production mixins. The AI
master switch is not evidence of implemented AI optimizations.

## Stronger candidates and their limits

1. **VisGraph queue and enum reuse (VintageFix idea).** Vanilla flood fill uses
   `ArrayDeque<Integer>` and calls `EnumFacing.values()` inside the visited-cell loop.
   A primitive FIFO and one private direction array preserve FIFO/neighbor order and
   eliminate reference queue traffic and repeated array clones. Important correction:
   vanilla uses `IntegerCache` (65,535 cached values); section indices are 0–4095, so
   this is not thousands of newly allocated Integer objects per flood. Our outer chunk
   graph already has a reusable queue; the inner vanilla flood fill still remains.
   B3's September profile sampled camera flood fill and ArrayDeque, but a current moving
   camera profile is required before selection. No `VisGraph` row was found in the
   installed foreign-hook inventory; runtime compatibility still needs checking.
2. **Smaller baked-model lists (VintageFix).** Our dedup shares quads, not the seven
   per-model lists and their spare array capacity. Upstream uses exact-size immutable
   lists, shared empties and a compact culled-side representation. This is complementary,
   not a replacement for quad dedup. Require a current retained-heap histogram and
   proof no installed renderer mutates the returned lists; making a list immutable
   can otherwise break callers. Historical catalog evidence suggests modest vanilla savings.
3. **GPU animation frame copies (FoamFix).** Preload frames into a GPU holding texture,
   then use copy-image operations instead of resending pixels from Java at frame changes.
   This is a more promising timing-preserving starting point than freezing hidden sprites.
   It trades VRAM for upload work and requires capability fallback, all mip levels,
   vanilla interpolation, clock/compass handling, GL state restoration and reload cleanup.
   No texture bottleneck has been established on our NVIDIA workload.
4. **Camera-section visibility reuse.** Cache opaque occupancy or exits while inputs are
   unchanged. Potentially removes 4,096 reads plus flood fill. More complex than queue reuse:
   invalidate immediately on block changes, chunk replacement/unload and world changes;
   preserve camera-cell and spectator rules. Copy cached exits before direction filtering,
   because the current caller removes a facing from the returned set.
5. **Block-palette bulk decode (modern Sodium idea, not a 1.12 source port).** Our
   `WorldSlice.copyBlocks` does individual `ExtendedBlockStorage.get` calls. Bulk unpack
   could reduce rebuild cost; modern Sodium offers separate whole-section/bounded decode.
   Adapt bit layout and palette behavior independently. `ClonedChunkSection` currently
   stores the live `ExtendedBlockStorage` reference, so establish snapshot/thread ownership
   before treating it as immutable. The inspection does not prove a reproducible race.
   Sodium 0.5's license in our catalog permits ideas only, not copying its code.

   **Current B3 check (2026-10-14): deferred.** The current worker profile sampled
   `WorldSlice.copyBlocks` in 10/237 self frames, but the nested loop order is already
   contiguous for vanilla's `y << 8 | z << 4 | x` storage layout. The render-thread
   `chunk_update` section averaged 0.203 ms/frame. A direct palette decoder would expand the
   threading/snapshot risk without evidence of a user-visible bottleneck; keep the existing
   contiguous getter loop until a safe snapshot and focused chunk-build gain are demonstrated.

   Neither `VisGraph` nor `ArrayDeque` appears in the same current B3 profile's top-40 frames. Camera flood-fill
   reuse is already implemented and measured separately, so the primitive inner queue is also
   deferred. The remaining model-list candidate is bounded by a historical roughly 2–3 MB
   estimate and can make caller-visible lists immutable; the GPU animation candidate still has
   no measured upload bottleneck. Neither clears the current value/compatibility gate.

These are candidates, not a five-feature implementation plan. Prefer one current B1/B3
profile, choose the cheapest supported target, prove parity and isolated gain, and stop if
the workload does not justify it. No percentage gain is predicted from sample counts.

## Corrections to earlier inventory assumptions

- **Vertex uploader:** decompiled vanilla `WorldVertexBufferUploader.draw` already has
  indexed linear setup/cleanup loops. Valkyrie's `preDraw/postDraw` implementation targets
  Forge hooks; its advertised O(n²) issue is not present in our vanilla target. Defer/reject
  that specific port rather than treating it as a free improvement.
- **Hidden animations:** inspected VanillaFix and LoliASM call `updateAnimation` only for
  active sprites; inactive frame/tick counters therefore do not advance through that path.
  The catalog's blanket timing-parity argument was incorrect. Sprite marking also has to
  cover display-list replay, item/GUI renderers and foreign renderers, not just chunk builds.
- **Chunk cache:** VintageFix's inspected four-entry cache has `CACHE_ENABLED=false`.
  Its active map changes include initial capacity/shrink policy; an upstream cache class
  existing is not evidence that enabling its lookup cache is a proven win.
- **NBT replacement:** StellarCore uses a different hash-map implementation. Matching
  logical contents does not establish vanilla key iteration or serialization order.
  Preserve those orders and floating-point bit patterns before claiming parity for pooling.

## Primary sources

- [FoamFix configuration](https://github.com/asiekierka/FoamFix/blob/0.9.x-1.12.2/src/main/java/pl/asie/foamfix/shared/FoamFixConfig.java), [GPU animation path](https://github.com/asiekierka/FoamFix/blob/0.9.x-1.12.2/src/main/java/pl/asie/foamfix/client/FastTextureAtlasSprite.java).
- [VintageFix options](https://github.com/embeddedt/VintageFix/wiki/Config-options), [VisGraph](https://github.com/embeddedt/VintageFix/blob/main/src/main/java/org/embeddedt/vintagefix/mixin/allocation_rate/MixinVisGraph.java), [model-list storage](https://github.com/embeddedt/VintageFix/blob/main/src/main/java/org/embeddedt/vintagefix/impl/ModelSidesImpl.java).
- [VanillaFix animation dispatch](https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/textures/mixins/client/MixinTextureMap.java), [LoliASM configuration](https://github.com/LoliKingdom/LoliASM/blob/master/src/main/java/zone/rong/loliasm/config/LoliConfig.java).
- [Vintagium](https://github.com/Asek3/sodium-1.12), [Nothirium](https://github.com/Meldexun/Nothirium), [ParticleCulling](https://github.com/bl4ckscor3/ParticleCulling).
- [FastFurnace tile](https://github.com/Shadows-of-Fire/FastFurnace/blob/1.12/src/main/java/shadows/fastfurnace/block/TileFastFurnace.java), [FastWorkbench container](https://github.com/Shadows-of-Fire/FastWorkbench/blob/1.12/src/main/java/shadows/fastbench/gui/ContainerFastBench.java).
- [BetterFps hopper](https://github.com/Guichaguri/BetterFps/blob/1.12/src/main/java/guichaguri/betterfps/patches/block/FastHopper.java), [Carpet 1.12](https://github.com/gnembon/carpetmod112).
- [StellarCore options](https://github.com/NovaEngineering-Source/StellarCore/blob/master/ExampleConfiguration.cfg), [pending-tick patch](https://github.com/NovaEngineering-Source/StellarCore/blob/master/src/main/java/github/kasuminova/stellarcore/mixin/minecraft/worldserver/MixinWorldServer.java).
- [Valkyrie uploader](https://github.com/Red-Studio-Ragnarok/Valkyrie/blob/main/src/main/java/dev/redstudio/valkyrie/mixin/WorldVertexBufferUploaderMixin.java), [OptiFine feature list](https://optifine.net/home), [AI-Improvements](https://github.com/BuiltBrokenModding/AI-Improvements), [Clumps](https://github.com/jaredlll08/Clumps).
- Local ground truth: MCP stable_39 `minecraftSrc-1.12.2-sources.jar`:
  `WorldVertexBufferUploader`, `VisGraph`, `IntegerCache`, `TileEntityBeacon`.
  Local performance leads: `profiles/B3-hoppers-on.top.txt`, dated September 18;
  current decisions/evidence: `PLAN.md`, RC2 and audit-fix profile READMEs.

Research limits: no head-to-head upstream benchmark, current live profile, game session,
or full compatibility test. No product code or installed settings changed.

# Vanilla 1.12.2 code audit

What the decompiled source (`minecraftSrc-1.12.2-sources.jar`, MCP `stable_39` names) actually
does on its hot paths, read method by method. Line numbers refer to that jar. Every finding
is something the code does *every time* the path runs, independent of any profile — the
profile only says which path runs most in a given world. Findings are grouped by how often
the path runs. Each ends with the catalog row that owns it (`docs/catalog.md`).

Legend: **cost** = what is paid per execution; **fix** = the parity-preserving change;
**parity** = why the game state / pixels stay identical.

## A. Per entity, per tick (both sides — the client ticks every entity it sees too)

### A1. `Entity.onEntityUpdate` scans 2×27+ blocks for water and lava — `Entity.java`, `World.java:2216`
`handleWaterMovement()` → `World.handleMaterialAcceleration(bb.grow(0,-0.4,0).shrink(0.001), WATER)`
then `isInLava()` → `World.isMaterialInBB(bb.grow(-0.1,-0.4,-0.1), LAVA)`. Each: 2 `AxisAlignedBB`
allocations, `isAreaLoaded` (chunk-loaded check per corner), then a 3-D loop of
`World.getBlockState` (each = `isOutsideBuildHeight` + `getChunk` hash lookup + `Chunk.getBlockState`).
For a 0.25-wide item: 2×2×2..3×3×3 blocks ×2 materials ≈ 30–50 chunk hash lookups per item per tick.
**fix:** per-chunk-section count of `Material.WATER`/`LAVA` blocks (maintained in `Chunk.setBlockState`,
kept outside `ExtendedBlockStorage` because Newlight overwrites that class); if every section the box
touches has count 0 → return false without scanning. Otherwise run vanilla's loop unchanged.
**parity:** the loop only reads; skipping it when it provably finds nothing changes no state, and
`modifyAcceleration`/`vec3d` stay untouched exactly when vanilla would leave them untouched.
→ `world/entity_fluid_checks`

### A2. `Entity.move` → `World.getCollisionBoxes` does a chunk hash lookup per block — `World.java:1437,1500`, `getCollisionBoxes(Entity, AABB, boolean, List)`
Per move: `Lists.newArrayList()`, `bb.expand`, then for each (x,z) column `isBlockLoaded` (hash
lookup), and for each block `worldborder.contains` + `World.getBlockState` (hash lookup again) +
`addCollisionBoxToList`. A 1×1 entity moving normally touches 3×3 columns × ~4 rows = 36 lookups + 9.
Then `getEntitiesWithinAABBExcludingEntity(bb.grow(0.25))` scans entity sections for boats/shulkers.
Sneaking players redo the whole scan per axis step (`:75,90,105`). Particles (`Particle.move`) pay
the block part too, up to 16384 × 8 layers.
**fix:** masa's Carpet version (`CollisionBoxesOptimizations.java`, 113 lines, LGPL-3): resolve
the chunk once per (cx,cz), iterate `chunk.getBlockState` directly, stop at
`chunk.getTopFilledSegment()+15`, same `xIsEdge/zIsEdge` skip rules, same x→z→y order so the list
order is identical. Entity part: per-chunk list of `canBeCollidedWith` entities (TISCM
`optimizedHardHitBoxEntityCollision`) so the section scan is empty in normal chunks.
**parity:** same blocks visited in the same order → same boxes in the same order; world-border
branch kept verbatim. → `world/block_collisions`, `world/hard_hitbox_entity_list`

### A3. Entity flags go through a `ReentrantReadWriteLock` and a boxed map — `EntityDataManager.java:28-29`, `getEntry`
`Entity.getFlag(int)` → `dataManager.get(FLAGS)` → `getEntry` → `lock.readLock().lock()` +
`entries.get(Integer.valueOf(id))` (`HashMap<Integer,…>`) + unboxed `Byte`. `isSneaking`,
`isSprinting`, `isInvisible`, `isGlowing`, `isBurning`, `isSilent`, `getAir`, item-entity
`getItem`, … are all called several times per entity per tick on both sides, and by the render
thread per frame.
**fix:** store entries in an array indexed by `DataParameter.getId()` (ids are dense, assigned at
class-init) and drop the lock — vanilla only ever touches a `dataManager` from the thread that
owns the entity (server thread / client thread); the render thread reads (`isInvisible`,
`isSneaking` in renderers) but never writes, and the lock never protected against a torn read
of a reference anyway. FoamFix `fasterEntityDataManager`, Lithium `entity.data_tracker.*`.
**parity:** same values returned; `dirty`/`isDirty` bookkeeping preserved for sync packets.
→ `memory/entity_data_manager`

### A4. `EntityLivingBase.onUpdate` compares equipment NBT every tick — `EntityLivingBase.java` (`onUpdate`, `EntityEquipmentSlot.values()` loop)
Server side, per living entity per tick: `EntityEquipmentSlot.values()` (array clone) and for each of
6 slots `ItemStack.areItemStacksEqual(cached, current)` → for stacks with NBT a full
`NBTTagCompound.equals` (map compare). Mob farms with armored zombies/skeletons pay this for every mob.
**fix:** compare identity + count + damage first, and only fall back to `areItemStacksEqual` when the
`ItemStack` instance changed; or track equipment changes at the setter (`setItemStackToSlot`) as Lithium
does (`entity.equipment_tracking`).
**parity:** the sync packet is sent under exactly the same condition (stack not equal).
→ `world/equipment_change_check` (new)

### A5. `EntityLivingBase.collideWithNearbyEntities` allocates a predicate and reads a game rule by string — `EntityLivingBase.java`
Per living entity per tick: `EntitySelectors.getTeamCollisionPredicate(this)` (new predicate object),
`world.getEntitiesInAABBexcluding(...)` (new list), and when the list is non-empty
`world.getGameRules().getInt("maxEntityCramming")` (string-keyed `TreeMap` lookup — `GameRules.rules`
is a `TreeMap<String, Value>`, so a `String.compareTo` chain per call).
**fix:** cache the predicate per entity (it only depends on `this` and team, which changes rarely —
re-create when the team changes); cache the game-rule `Value` object once and read its int.
**parity:** identical inputs, identical decisions, RNG call order untouched.
→ `world/cramming_predicate_cache` (new)

### A6. `EntityLiving.despawnEntity` finds the closest player by scanning all players — `EntityLiving.java`
Per mob per tick: `world.getClosestPlayerToEntity(this, -1)` → linear scan of `playerEntities`
computing squared distances. Singleplayer = 1 player, cheap; note only.

### A7. `EntityItem.searchForOtherItemsNearby` runs even when the stack can never merge — `EntityItem.java:126,188,200-262`
Every 25 ticks (or whenever the item crossed a block boundary) each item queries
`getEntitiesWithinAABB(EntityItem, bb.grow(0.5,0,0.5))` (new AABB, new list, section scan) and calls
`combineItems` on every neighbour. `combineItems` provably returns false for a full stack (both
branches end in `count + count > maxStackSize`).
**fix:** skip the query when `getItem().getCount() >= getMaxStackSize()` and `getMaxStackSize() > 1`
(the `> 1` keeps Tweakeroo's shulker-box stacking tweak working).
**parity:** no RNG, no side effect in the skipped path. → `world/item_merging`

## B. Per tile entity, per tick

### B1. `World.updateEntities` TE loop — `World.java:1733`
Per ticking TE per tick: `isBlockLoaded(pos)` (hash lookup), `worldBorder.contains(pos)`, a capturing
lambda for the profiler (`func_194340_a(() -> …)`, allocated even when profiling is off),
`isInvalid()` twice. On removal: `iterator.remove()` + `loadedTileEntityList.remove(tileentity)`
(O(n) `ArrayList.remove(Object)`), and at the top `tickableTileEntities.removeAll(toBeRemoved)` +
`loadedTileEntityList.removeAll(toBeRemoved)` — `ArrayList.removeAll(ArrayList)` is O(n·m) with
`equals`. Added TEs: `loadedTileEntityList.contains(te)` per added TE, O(n).
**fix:** Carpet `optimizedTileEntityRemoval` (identity set + batched removal at the end of the loop,
same survivors in the same order); an identity-hash membership set beside `loadedTileEntityList` for
the `contains`. The per-TE `isBlockLoaded` can use the chunk the TE was last found in.
**parity:** same TEs ticked in the same order; same TEs removed. → `world/tile_entity_removal`

### B2. A hopper that has nothing to move still resolves its target chest every tick — `TileEntityHopper.java:169-181,585-604`, `BlockChest.java:507-548`
When not on cooldown (i.e. **every tick while idle**): `getInventoryForHopperTransfer` →
`getInventoryAtPosition` → `new BlockPos` + `getBlockState` + `getTileEntity` + for a chest
`BlockChest.getContainer(world,pos,true)` → 4 neighbour `getBlockState` and, for each neighbouring
chest, `isBlocked(neighbour)` → `isBelowSolidBlock` + **`isOcelotSittingOnChest` = an entity query**
(`getEntitiesWithinAABB(EntityOcelot, new AABB)`) + `new InventoryLargeChest`. Then `isInventoryFull`
scans all 54 slots. Then `pullItems` → `getSourceInventory` repeats the lookup for the block above,
or, when there is no inventory above, `getCaptureItems` = an `EntityItem` query every tick. When the
block has no TE inventory (e.g. hopper pointing into a solid block), the fallback runs
`getEntitiesInAABBexcluding(HAS_INVENTORY)` — another entity query per tick.
**fix:** cache the resolved block inventory (`IInventory` + the `InventoryLargeChest` pair) per hopper
and per direction, invalidated by `Chunk.setBlockState`/`World.setTileEntity`/`removeTileEntity` at
the six positions the lookup touched (Lithium `block.hopper` design, Carpet `optimizedInventories`
by skyrising). **Keep** the entity queries exactly when vanilla would run them — the minecart fallback
consumes `world.rand.nextInt(list.size())` (`:626`) only when an entity inventory is found, and
`isOcelotSittingOnChest` reads live entities — so cache only the block half; the ocelot check can be
skipped only when the chunk section contains no `EntityOcelot` (cheap check on
`entityLists[y].getByClass`).
**parity:** RNG consumed under the same conditions; the same inventory instance is returned as long
as the cache is invalidated on every block/TE mutation at those positions.
→ `world/hopper_inventory_cache`

### B3. `TileEntityChest.update` ticks every chest for a lid animation — `TileEntityChest.java`
Per chest per tick: `checkForAdjacentChests()` (cheap after the first), 3 int fields, and every 200
ticks a player-in-range entity query when `numPlayersUsing != 0`. Cheap individually; with 10k
chests it is 10k `update()` dispatches + the B1 overhead per TE. Lithium sleeps the chest when
`numPlayersUsing == 0 && lidAngle == 0`.
**fix:** not in the first batches — sleeping needs the wake-up plumbing (B2's invalidation hooks);
B1's per-TE overhead reduction covers most of it. → `world/tile_entity_sleeping` (deferred)

### B4. `TileEntityFurnace.canSmelt` searches all recipes every tick while lit — `TileEntityFurnace.java`, `FurnaceRecipes.getSmeltingResult`
`getSmeltingResult` iterates the whole `smeltingList` map with `compareItemStacks` per entry.
**fix:** cache `(input item, meta) → result` per furnace, invalidated when slot 0 changes (FastFurnace,
MIT). **parity:** same result, no RNG. → `world/furnace_recipe_cache`

## C. Per block change / scheduled tick

### C1. `WorldServer.tickUpdates` — `WorldServer.java` (`tickUpdates`, `updateBlockTick`)
Per scheduled tick: `TreeSet.first()` + `TreeSet.remove()` + `HashSet.remove()` + `ArrayList.add`,
then `isAreaLoaded` + `getBlockState`. Per schedule request: `new NextTickListEntry` (even for
duplicates), `isBlockLoaded`, `HashSet.contains` (hash = `position.hashCode()` only, so many entries
at one position collide), `TreeSet.add`. Everything is O(log n) with a 65536/tick cap.
**fix:** Lithium `world.tick_scheduler`: bucket entries by scheduled time in a
`Long2ObjectMap<ArrayList>` (or ring of lists), keep `(time, priority, id)` ordering by sorting each
bucket with the same comparator when it is drained, and a per-position `Long2ObjectMap` for the
`contains`/`isUpdateScheduled` checks.
**parity:** identical order requires the same `tickEntryID` tiebreak and the same 65536 cap semantics
(“take the first N in order, stop at the first entry in the future”). Proven by the harness on B3.
→ `world/tick_scheduler`

### C2. `WorldServer.getPendingBlockUpdates(Chunk, boolean)` walks the whole list per chunk — `WorldServer.java:789-802`, called by `AnvilChunkLoader.writeChunkToNBT`
Two full iterations (tree + this-tick list) filtering by chunk bounds, for **every chunk saved**
(autosave every 900 ticks saves all dirty chunks; unload saves too). Cost = dirty chunks × scheduled
ticks; with 100k scheduled ticks and 500 dirty chunks that is 5·10⁷ comparisons in one tick.
**fix:** per-chunk bucket (`Long2ObjectMap<List<NextTickListEntry>>`) maintained alongside C1's
structure; `getPendingBlockUpdates` returns that bucket sorted with the same comparator.
**parity:** the list is only serialized (order inside the NBT list must match: sort it). Removal
semantics for `remove == true` (unload) preserved. → `world/pending_ticks_chunk_lookup`

### C3. `World.setBlockState` neighbour notification allocates 12 `BlockPos` and does 12 chunk lookups — `World.java` (`notifyNeighborsOfStateChange`, `updateObservingBlocksAt`)
`pos.west()/east()/…` allocate; `neighborChanged` → `getBlockState` per neighbour (hash lookup); then
`updateObservingBlocksAt` repeats the six lookups just to test `== Blocks.OBSERVER`.
**fix:** one `MutableBlockPos`, resolve the chunk once for the 6 neighbours (they share the chunk unless
on a boundary), and reuse the already-fetched neighbour state for the observer test. The
`neighborChanged` **call order** (W,E,D,U,N,S) and the state read timing (state read right before
each call, since earlier neighbours may change later ones) must stay exactly as vanilla: read the
state immediately before each `neighborChanged`, not all six up front.
**parity:** same calls, same order, same states seen. → `world/blockpos_garbage`

### C4. `WorldServer.updateBlocks` — `WorldServer.java` (`updateBlocks`)
Per loaded chunk per tick: an anonymous `AbstractIterator` (`PlayerChunkMap.getChunkIterator`),
5 profiler sections, `chunk.enqueueRelightChecks()` (lighting — Newlight territory, untouched),
`chunk.onTick`, 2 `rand` calls; per random tick `new BlockPos`. The LCG advance order is the
random-tick contract — never change which sections are visited or how many LCG steps each takes.
**fix:** only the `new BlockPos` per random tick (pass a mutable pos to `randomTick` is *not* safe:
blocks store the pos — keep the allocation) → nothing worth doing here beyond the iterator. Note only.

## D. Per chunk save / load

### D1. `AnvilChunkLoader.writeChunkToNBT` builds all NBT on the server thread — `AnvilChunkLoader.java`
Block data packing, both light arrays copied, every TE `writeToNBT`, every entity `writeToNBT`, plus
C2. Only the IO is on `ThreadedFileIOBase`.
**fix:** C2 first. Then snapshot block/light arrays on the main thread and pack/compress off-thread
(TISCM does this on 1.13). TE/entity NBT must stay on the main thread.
→ `world/pending_ticks_chunk_lookup`, `world/chunk_save_serialization`

### D2. Client chunk load recomputes skylight for the column — `Chunk.java:624,718` (`generateSkylightMap` from `read`)
Every `SPacketChunkData` for a full chunk → `generateSkylightMap()` over 16×16×256. Lighting; Newlight
overwrites `SPacketChunkData`/`Chunk` paths — **do not touch**. Note only.

## E. Per frame (client)

### E1. `RenderGlobal.setupTerrain` rebuilds the visible-chunk graph whenever the camera moves at all — `RenderGlobal.java` (`setupTerrain`, `getVisibleFacings`)
`displayListEntitiesDirty` is set by any sub-block position or rotation change → every frame while
moving: `new ArrayList` for `renderInfos`, `new ArrayDeque`, one `ContainerLocalRenderInformation`
per visited render chunk (RD16 ≈ 33×33×16 ≈ 17k objects), `new BlockPos` per chunk in the
spectator/outside branch, `EnumFacing.values()` per visited chunk, then a fresh
`Sets.newLinkedHashSet()` for `chunksToUpdate`. Worst: `getVisibleFacings(cameraPos)` builds a
**new `VisGraph` from 4096 `chunk.getBlockState` calls and flood-fills it every frame** the camera moved,
just to know which faces of the camera's own section are reachable.
**fix:** reuse `renderInfos`/queue/info objects (pool indexed by render-chunk index); cache
`getVisibleFacings` per (section, camera block) and invalidate when that `RenderChunk` is marked for
update; Valkyrie's `ViewFrustum`/`getRenderChunkOffset` bit tricks.
**parity:** same BFS, same order of `renderInfos` → same draw order → same pixels.
→ `render/view_frustum_setup_terrain`, `render/setup_terrain_allocations`, `render/camera_visgraph_cache` (new)

### E2. `RenderGlobal.renderEntities` does a chunk hash lookup per visible render chunk per frame — `RenderGlobal.java` (`renderEntities` loop over `renderInfos`)
For each of the ~17k `renderInfos`: `world.getChunk(pos)` (Long2ObjectMap lookup) +
`getEntityLists()[y>>4]` + `isEmpty()`. Almost all are empty. Then per entity `shouldRender`
(frustum) and `renderEntityStatic`.
**fix:** cache the `Chunk` reference on the `RenderChunk` (set on `setPosition`, cleared on chunk
unload via `ChunkProviderClient.unloadChunk` hook), or keep a per-chunk “has any entity” flag.
**parity:** same iteration order, same entities → same pixels. → `render/entity_iteration_per_chunk` (new)

### E3. `RenderGlobal.renderEntities` renders every TE of every visible render chunk with only a distance test — `RenderGlobal.java:708-726`, `TileEntityRendererDispatcher.java:118`
Per TE per frame: `getDistanceSq` vs `getMaxRenderDistanceSquared` (64² by default), then the full
TESR (chests: `ModelChest` display list + GL state; signs: font rendering; banners: many layers).
No frustum test, no occlusion test.
**fix:** per-TE AABB frustum test; async occlusion (raycast against opaque blocks) with a per-tick
cache — same design for entities (EntityCulling idea, own implementation).
**parity:** pixels identical when the culled TE/entity is fully occluded/off-screen; the harness's
screenshot diff is the proof. → `render/tile_entity_frustum`, `render/tile_entity_occlusion`, `render/entity_culling`

### E4. `TextureMap.updateAnimations` uploads every animated sprite every frame — `TextureMap.java:274`
`listAnimatedSprites` loop → `TextureAtlasSprite.updateAnimation()` → `TextureUtil.uploadTextureMipmap`
(a `glTexSubImage2D` per mip level) whenever the frame index changed — for water, lava, fire,
portal, prismarine, magma, sea lantern, command block, … regardless of visibility.
**fix:** mark sprites used during chunk builds / item renders since the last frame; skip the upload
(not the frame-counter advance) for unmarked ones (VanillaFix, MIT).
**parity:** frame counters advance identically; the texture is re-uploaded with the correct frame the
moment the sprite is visible again. → `render/animated_textures_visible_only`

### E5. `ParticleManager.renderParticles` draws all particles; `Particle.move` runs full block collision — `ParticleManager.java`, `Particle.java` (`move`)
No frustum test per particle. Per particle per tick `world.getCollisionBoxes(null, bb.expand(…))` (A2's
block scan + list allocation) for `canCollide` particles.
**fix:** frustum-cull in `renderParticles` (ParticleCulling, MIT); A2's collision fix applies to
particles automatically. **parity:** pixels: off-screen particles draw nothing. → `render/particle_culling`

### E6. `ModelRenderer.render` issues translate/rotate/push/pop per part per frame — `ModelRenderer.java` (`render`)
Per model part: `translate` ×2–4, up to 3 `rotate`, `push/popMatrix`, `callList`. A biped is ~6 parts,
armor layers repeat it; a mob farm with 500 visible mobs ≈ 20k GL matrix calls per frame.
**fix:** Valkyrie (MIT): compute the part matrix on the CPU and load it once (`glMultMatrix`), same
rotation order Z→Y→X. **parity:** identical transform → identical pixels (verify float order on B4).
→ `render/entity_model_matrix`

### E7. `WorldVertexBufferUploader.draw` — `WorldVertexBufferUploader.java`
The post-draw disable loop re-scans the element list per element (O(n²) in the number of vertex
elements). Small n, but it runs for every immediate-mode draw (GUI, particles, entities, TESRs).
**fix:** Valkyrie's index tracking. → `render/vertex_buffer_uploader`

### E8. `EntityRenderer.getMouseOver` — `EntityRenderer.java` (`getMouseOver`)
Per frame: `entity.rayTrace` (block raytrace with `Vec3d` garbage), `Predicates.and(...)` + an
anonymous predicate allocated per frame, `getEntitiesInAABBexcluding` (list), per entity
`bb.grow(...)` + `calculateIntercept` (`Vec3d` garbage). Small absolute cost; the raytrace part is
shared with A-side `canEntityBeSeen`. → `world/raytrace_allocations`

### E9. `ClippingHelper.isBoxInFrustum` — `ClippingHelper.java:18`
6 planes × 8 corners `dot` with `double` math per chunk/entity/TE frustum test.
**fix:** p/n-vertex test (Forge PR 9407 port in Vintagium, LGPL-3; or own). **parity:** must never
reject a box vanilla accepts (a conservative test that accepts a superset is pixel-identical; one that
rejects more is not). → `render/frustum_fast_test`

### E10. `BufferBuilder.sortVertexData` — `BufferBuilder.java:66`
Translucent re-sort: `float[]` distances, then `Integer[]` indices sorted with a boxing comparator,
then a `BitSet` permutation. Runs on every `RESORT_TRANSPARENCY` task (camera moved across a
translucent chunk).
**fix:** primitive index sort with the same key and a stable algorithm → same quad order.
→ `render/translucency_sort`

## F. Per chunk rebuild (client worker threads)

### F1. `RenderChunk.rebuildChunk` — `RenderChunk.java` (`rebuildChunk`), `ChunkCache.java`
Per rebuild: `new CompiledChunk`, `new VisGraph` (BitSet 4096 + queue), `Sets.newHashSet()`,
`BlockPos.getAllInBoxMutable` iterator, `BlockRenderLayer.values()` ×2, then two more
`Sets.newHashSet` copies + two `removeAll` for the global-TE diff. `ChunkCache` (`:281`) is a
3×3 `Chunk[][]`, not a snapshot — every `getBlockState` is a `Chunk.getBlockState` on live data;
`getLightForExt` for blocks with `useNeighborBrightness` (stairs, slabs, glass…) does
`EnumFacing.values()` + 6 `pos.offset` allocations + 6 `getLightFor`.
**fix:** VintageFix `chunk_rendering`/`allocation_rate` bits (simpler iterator, reuse sets, mutable
pos in `getLightForExt`); keep the live-read semantics (a snapshot would change *which* state a
rebuild sees relative to vanilla — not a parity issue for state, but a behaviour difference in what
frame shows a change; keep vanilla's).
→ `render/chunk_build_allocations`

### F2. `BlockModelRenderer.renderModelSmooth` — `BlockModelRenderer.java:61` and `AmbientOcclusionFace.updateVertexBrightness`
Per block: `new float[12]`, `new BitSet(3)`, `new AmbientOcclusionFace()` (two more arrays inside),
`EnumFacing.values()`. Per rendered face: `shouldSideBeRendered` (neighbour lookup + `pos.offset`
allocation in `Block.shouldSideBeRendered`), `getPackedLightmapCoords` (2 light lookups), and in
`updateVertexBrightness` **~13 `getBlockState`** (4 AO values + 4 translucency checks + up to 4
corners + center) plus `getAmbientOcclusionLightValue`/`isTranslucent` on each, plus
`centerPos.offset(direction)` allocations. Adjacent blocks re-read the same neighbours: a full 16³
section of AO blocks performs ~10⁵ block-state reads for 4096 blocks.
**fix:** per-rebuild cache of (blockstate, AO value, translucency, packed light) for the 18³ region,
filled lazily; reuse the `AmbientOcclusionFace`/arrays per worker thread (Sodium's light-data cache
idea; Vintagium `core/pipeline` has it for 1.12.2 under LGPL-3).
**parity:** the same floats from the same inputs → identical vertex colors → identical pixels.
→ `render/ao_face_allocation`, `render/chunk_light_cache` (new)

### F3. `BiomeColorHelper.getColorAtPos` — `BiomeColorHelper.java`
Per grass/leaves/water face: 9×9 = 81 `getBiome` (chunk lookup + biome array) + 81 color calls,
averaged. → `render/biome_color_cache`

## G. Per client tick

### G1. `WorldClient.updateBlocks` → `refreshVisibleChunks` rebuilds a `HashSet<ChunkPos>` every tick — `WorldClient.java:135`
(2·RD+1)² `new ChunkPos` + hashing per tick (RD16: 1089), then `previousActiveChunkSet.retainAll`.
**fix:** rebuild only when the player's chunk changed (VintageFix), keeping the
`size()==size() → clear()` cycle semantics. **parity:** the mood-sound/light scan visits the same
chunks in the same order. → `render/client_visible_chunks` (new)

### G2. `WorldClient.tick` — `loadedEntityList.contains(entity)` per queued spawn — `WorldClient.java:95`
O(n) list scan per entity spawned from the queue (up to 10 per tick). Note only.

## H. Everywhere

### H1. `ItemStack.isEmpty()` does a map lookup — `ItemStack.java` (`isEmpty`), `Item.java:119`
`this.item != Item.getItemFromBlock(Blocks.AIR)` → `BLOCK_TO_ITEM.get(AIR)` (`IdentityHashMap`)
on **every** `isEmpty()` — inventories, hoppers (B2's 54-slot scans), rendering, containers.
**fix:** compare against a cached `Items.AIR` reference (FoamFix `fasterAirItemLookup`).
**parity:** identical result (the map value for `Blocks.AIR` is constant after init).
→ `memory/itemstack_is_empty` (new)

### H2. `EnumFacing.values()` clones — `RenderChunk` ×6, `BlockModelRenderer` ×6, `EntityLivingBase` ×4, `SetVisibility` ×4, `FaceBakery` ×4, `World` ×3, `EntityLiving` ×3, `BlockRedstoneWire` ×3, pistons, fire…
Each call clones the 6-element array. → `render/enum_facing_values`

### H3. `World.getBlockState` → `getChunk` → `ChunkProviderServer.getLoadedChunk` — `World.java`, `ChunkProviderServer.java`
Every block read is `isOutsideBuildHeight` + `ChunkPos.asLong` + `Long2ObjectOpenHashMap.get` +
`chunk.unloadQueued = false` (a write!) + `Chunk.getBlockState` (bounds + `ExtendedBlockStorage`
lookup). All the per-block loops above (A1, A2, C3, F2) pay it; a last-chunk cache per thread
(Lithium `world.chunk_access`) removes most of the hash lookups but must keep the
`unloadQueued = false` side effect (it is what keeps a chunk loaded while being accessed).
→ `world/chunk_access_cache`

### H4. `WorldEntitySpawner.findChunksForSpawning` — `WorldEntitySpawner.java`
Per tick: `new ChunkPos` ×289 per player + `HashSet` membership; per creature type
`worldServerIn.countEntities(class)` = a full scan of `loadedEntityList` with `isAssignableFrom`
(4 scans of every entity per tick — with 5k items on the ground that is 20k checks per tick before
any spawning logic runs).
**fix:** per-class counters maintained in `onEntityAdded/Removed` (Lithium `collections.entity_by_type`).
**parity:** counting only; the spawn RNG sequence is untouched. → `world/entity_type_counts` (new)

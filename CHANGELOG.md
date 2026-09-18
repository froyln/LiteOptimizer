# Changelog

## 0.3.0 — Sodium I (2026-09-18)

The render pillar: Vintagium (Sodium 0.4 for 1.12) ported to LiteLoader, no Forge, switch
`render/sodium`. Sodium's section graph, worker-thread meshing and one-VBO-per-section renderer,
but every frame is **pixel-identical to vanilla** at a fixed pose (screenshot diff, 0 differing
pixels on a large base, a redstone array and a terrain flight; `docs/methodology.md` § render
parity): vanilla's own meshing algorithms and vertex layout, vanilla's draw path per section
(fixed-function, matrix stack, seam matrix), vanilla's chunk graph search including its quirks
(first-path culling, off-centre `ViewFrustum` grid, double-drawn beacon beams).

Measured, uncapped frame rate: terrain flight (B6, RD 10) frame 1.66 → 1.06 ms (−36 %), 1 % low
8.22 → 6.00 ms (−27 %), client-thread work per frame halved (1.35 → 0.73 ms); base pose (B1, block
entities dominate) −5 %; 15-section redstone world unchanged. Opt-in in
`liteconfig/common/liteoptimizer-sodium.json`: `use_chunk_multidraw` (GL 4.3 backend, faster,
a sub-pixel off), `use_compact_vertex_format`, `use_block_face_culling`, `use_fog_occlusion`.

Also: `render/particle_culling` (frustum), `render/translucency_sort` (fast stable sort for
`BufferBuilder`), `render/frustum_fast_test`, `render/vertex_format_init`. Bench harness:
screenshot at a fixed tick, uncapped FPS, `--fly`, `--no-entities`, section profiler and
per-section vertex dumps (`tools/vtx-diff.py`).


## 0.2.0 — Lithium I (2026-09-18)

Twelve game-logic / memory optimizations, every one proven bit-identical to vanilla by the
parity harness (6000 ticks on item, hopper, furnace and witch-farm worlds) and switchable in
`liteconfig/common/liteoptimizer.json`:

- `world/tile_entity_removal` — O(n) tile-entity list maintenance in `World.updateEntities`
- `world/pending_ticks_chunk_lookup` — per-chunk scheduled-tick index: no more whole-list scans on every chunk save (the autosave stutter)
- `world/structure_lookup_index` — spawn attempts no longer scan every structure start in the world (witch farms: −30 % MSPT)
- `world/block_collisions` — chunk resolved once per column instead of per block
- `world/hard_hitbox_entity_list` — entity collisions only look at boats/shulkers (item farms: −39 % MSPT)
- `world/entity_fluid_checks` — water/lava scans skipped for sections without fluids
- `world/item_merging` — full stacks skip the merge search
- `world/entity_type_counts` — incremental mob-cap counting
- `world/furnace_recipe_cache`, `world/raytrace_allocations`
- `memory/entity_data_manager` — lock-free, array-indexed entity data
- `memory/itemstack_is_empty` — no map lookup per `isEmpty()`

Measured (integrated server MSPT, off → on): item farm 1.52 → 0.93 ms, hopper hall
3.78 → 3.25 ms, witch farm 8.3 → 5.8 ms. Frame time unchanged (no render work yet).

## 0.1.0 — Foundation

Config plugin, parity harness (`tools/parity.sh`), bench recorder (`tools/bench.sh`).

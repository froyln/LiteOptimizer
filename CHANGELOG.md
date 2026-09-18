# Changelog

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

# Benchmark results

`tools/bench.sh <world> [warmup] [seconds]` — off then on, same save, same pose; summary from
`tools/bench-summary.py`. Frame time is client-side (the instance's FPS cap applies: ~140 fps
here), MSPT is the integrated server. Run-to-run noise: see the vanilla-vs-vanilla rows.

| date | world | build (optimizations on) | frames avg / 1% low (ms) off → on | MSPT avg / max (ms) off → on | notes |
|---|---|---|---|---|---|
| 2026-09-18 | B3-hoppers (`8_5`) | 8: tile_entity_removal, pending_ticks_chunk_lookup, itemstack_is_empty, entity_data_manager, item_merging, block_collisions, furnace_recipe_cache, entity_type_counts | 7.15 / 8.99 → 7.15 / 8.89 | **3.78 / 10.2 → 3.25 / 79.9** (−14 % avg) | 20 s warmup, 40 s. The on-run max is one spike (autosave lands inside the window on one run only — 900-tick period vs 800-tick window); compare avg. FPS at the cap. |
| 2026-09-18 | B2-items (`cobble farm for 1_11`) | 11 (adds item_merging, block_collisions, furnace_recipe_cache, entity_type_counts, entity_fluid_checks, raytrace_allocations, hard_hitbox_entity_list) | 7.14 / 8.30 → 7.14 / 8.00 | **1.52 / 3.9 → 0.93 / 2.6** (−39 % avg) | 20 s / 40 s |
| 2026-09-18 | B4-mobs (`Heinz' Witchfarm`) | 11 | 7.18 / 15.9 → 7.17 / 14.5 | 6.98 / 61 → 7.61 / 40 (+9 %, within ±4 % noise ×2) | off-vs-off noise on this world: MSPT ±3.8 %. `--only entity_fluid_checks`: +4.6 % (a witch farm is all water: the pre-check never skips, only costs) → pre-check reduced to one chunk lookup |
| 2026-09-18 | B4-mobs | 12 (+ structure_lookup_index, from the JFR profile) | 7.24 / 20.4 → 7.25 / 22.3 | **≈7.2 → 5.79** (−30 % avg) | JFR (`docs/profiles/B4-mobs-{off,on}.top.txt`): before, 23 % of server samples were `MapGenStructure.getStructureAt` under `isSwampHut` from the spawner; after, the spawner's remaining cost is `canCreatureTypeSpawnAtLocation` + `WorldBorder.contains` |

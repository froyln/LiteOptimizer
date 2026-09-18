# Benchmark results

`tools/bench.sh <world> [warmup] [seconds]` — off then on, same save, same pose; summary from
`tools/bench-summary.py`. Frame time is client-side (the instance's FPS cap applies: ~140 fps
here), MSPT is the integrated server. Run-to-run noise: see the vanilla-vs-vanilla rows.

| date | world | build (optimizations on) | frames avg / 1% low (ms) off → on | MSPT avg / max (ms) off → on | notes |
|---|---|---|---|---|---|
| 2026-09-18 | B3-hoppers (`8_5`) | 8: tile_entity_removal, pending_ticks_chunk_lookup, itemstack_is_empty, entity_data_manager, item_merging, block_collisions, furnace_recipe_cache, entity_type_counts | 7.15 / 8.99 → 7.15 / 8.89 | **3.78 / 10.2 → 3.25 / 79.9** (−14 % avg) | 20 s warmup, 40 s. The on-run max is one spike (autosave lands inside the window on one run only — 900-tick period vs 800-tick window); compare avg. FPS at the cap. |

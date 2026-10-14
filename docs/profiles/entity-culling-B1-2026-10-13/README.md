# Entity-culling isolation — B1, 2026-10-13

Result: **no demonstrated gain at this pose with non-hanging entities suppressed**. Entity culling remains ineligible for current
release certification; this replaces attribution from the old combined -89% figure.

Artifact: working tree based on `091d4e1` including the production parity-settings fix;
installed litemod SHA-256 `b9e5d1f346175b253190d513eab696776c1cd5e77eb8cad819fbeb8de5f2a94c`.
The full installed litemod manifest is `mods.sha256`. Source fixture: copied
`~/Dev/OptimizationMod-1.12.2-worlds/B1-storage`, never played in place.
Pose: `-1980.5841684379052 31.809877259292847 712.0017676442468 -101.02180480957031 42.53301239013672`.
Screenshot dimensions: 892×952. Runtime: Prism's configured legacy Java, NVIDIA,
isolated isolated Sway/WayVNC. Sodium parity settings are normalized by the new build;
`sodium-settings.json` records the saved input settings (before normalization).

Command, executed inside isolated Sway:

```
tools/bench.sh run/eval/entity-culling-B1 20 40 --compare entity_culling --profile --no-entities
```

20 s warmup, 40 s recording, uncapped FPS. Runner exited 0. Both configs enable Sodium,
model display lists and all other known optimization switches; the separate Sodium
`use_entity_culling` input remains true in both runs; their only difference is
`entity_culling=false/true`. See the saved config JSONs. This measures the incremental cost
with the rest of the mod enabled, not vanilla versus the entire mod.

| metric | culling off | culling on |
|---|---:|---:|
| frame average | 3.24 ms | 3.31 ms |
| slowest 1% frame average (runner's 1% low metric) | 11.40 ms | 12.04 ms |
| frame count | 12,342 | 12,087 |
| integrated MSPT average | 3.81 ms | 4.11 ms |
| entity rendering section | 0.171 ms/frame | 0.189 ms/frame |
| block-entity rendering section | 1.872 ms/frame | 1.860 ms/frame |

Frame average is +2.1% slower; one pair cannot establish a repeatable regression, and it
provides no positive gain. Most sampled section time is TESR rendering. A different pose or
entity workload would be needed to support this feature; repeating this unchanged scene is
not justified by a positive signal.

## Render comparison and limits

The runner's `--no-entities` preserves item frames and paintings while suppressing moving
entities. It fixes partial ticks and animated sprite/torch behavior. The PNGs therefore cover
the captured hanging-entity/terrain/TESR scene, not general moving/glowing/nametag entity behavior.

Full RGB comparison: **52 differing pixels**, all inside `(88,4)`–`(135,51)` in MiniHUD's
changing date/time/TPS lines. Pixels below the top 96 rows: **0 differences**. Both captures
were inspected; the full screenshot is not a 0-pixel pass. The top-96 exclusion also omits
scene content there and must not be generalized into complete render parity.

Configs and screenshots are retained here; raw CSVs/profile summaries permit recalculation.
The runner restored instance options, JVM settings, LiteOptimizer JSON, the prior `_bench` save,
and saved Sodium JSON. The fresh jar remains installed. No original world blocks were changed.

## Harness verification

`python3 tools/check-bench-config.py` passes: off/on differ only in the selected source key,
renderer/display-list dependencies remain enabled, legacy modes work, stale comparison keys
are rejected. `bash -n tools/bench.sh` and `git diff --check` pass.
Java 8 `./gradlew build --offline` exited 0 in 5 s. Review's stale-key finding was corrected;
final source/evidence review found no material inaccuracies.

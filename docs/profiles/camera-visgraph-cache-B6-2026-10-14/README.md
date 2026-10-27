# Camera-section visibility cache — B6 flight, 2026-10-14

Three isolated OFF/ON pairs used the B6 flight world, `--fly 0.5`, render distance 16,
20 seconds warmup, and 40 seconds capture. Every option except `camera_visgraph_cache` was
enabled in both modes. The candidate artifact SHA-256 was
`40e8afe90388336159cfc5a3ed934c10f48ec76927b62886edd30f60edc6288d`.

| Pair | Average frame time off → on | Gain | 1% low off → on |
|---|---:|---:|---:|
| 1 | 0.97 → 0.95 ms | 2.1% | 4.29 → 4.27 ms |
| 2 | 0.99 → 0.95 ms | 3.9% | 4.47 → 4.03 ms |
| 3 | 0.99 → 0.96 ms | 3.4% | 4.45 → 4.10 ms |

Median average-frame gain was 3.4%; median 1% low gain was 7.7%. These runs are near the
project's 3% noise threshold, so treat the result as a small improvement. The vanilla profiler's
`chunk_graph_rebuild` section fell from 0.083–0.088 to 0.043–0.046 ms/frame across the pairs
(about 48–51%). The final pair had 690 entries in each captured solid render list. MSPT variation
was inconsistent across pairs and is not claimed as a server improvement. Maximum frame times were
hundreds of milliseconds in both modes and remain noise.

The feature caches the exact six exits returned by the camera-section `VisGraph`, keyed by camera
block. It invalidates when a rebuild is scheduled for that section, when the section loads or
unloads, and when the section's rebuilt visibility data is published. Cache hits return a fresh
mutable set because the graph caller removes a view-facing exit. No block/entity iteration, state,
RNG, packet, or player-action code changes.

The final OFF/ON screenshots had 161 differing pixels, all in the top-left changing HUD text;
the gameplay image had zero differing pixels. The historical 6,000-tick hash recorded in `PLAN.md`
has no saved fixture identity and is not used as current parity evidence. Revision 58's cumulative
OFF/ON and OFF/OFF run passed on the stable `test` save; see
`../revision58-parity-2026-10-15/README.md`. This is a client optimization and does not reduce
remote server TPS.

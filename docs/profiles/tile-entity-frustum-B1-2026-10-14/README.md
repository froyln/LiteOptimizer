# Tile-entity frustum culling — B1, 2026-10-14

This measurement isolates `tile_entity_frustum` in the B1 storage world at the fixed pose used by
`pose.txt`, with `--no-entities`, 20 seconds warmup, and 40 seconds capture. All other LiteOptimizer
options were held enabled in both runs. The measured render-profile artifact was
`4b6aa445dac0dddeecff1c9fd464d72b580af349f65c2c917fb333af5e8bd8e2`; the later validation build
`4ff25bd363a482d18cf54a27061eb07eebce1cd1ddc18f0cc7e7fea30ad032a6` only adds canonical ordering
to the development parity dump and does not change production rendering code.

| Metric | Off | On | Change |
|---|---:|---:|---:|
| Frames captured | 6,610 | 9,821 | — |
| Average frame time | 6.05 ms | 4.07 ms | −32.7% |
| 1% low frame time | 23.65 ms | 9.54 ms | −59.7% |
| Average integrated-server MSPT | 4.10 ms | 3.93 ms | −4.1% (not attributed to a client render feature) |
| `blockentities` profile section | 4.192 ms | 2.629 ms | −37.3% |

The off run had a 734 ms maximum frame while the on run's maximum was 23 ms, so isolated tail
latency is noisy and these maxima are not a claimed improvement. The screenshots differed by 277
pixels overall, all in the changing top-left HUD text; the gameplay crop `(0, 80, 1916, 1001)` had
zero differing pixels for this fixed scene. Culling is still permitted to omit block-entity
rendering when the conservative bounds miss the current frustum.

The feature changes only client block-entity draw dispatch. It does not change block-entity ticking,
server state, packets, or player actions. This profile's 6,000-tick B1 OFF/ON dump matched, but a
later B1 OFF/OFF control changed the final `World.rand` seed, so B1 is not deterministic enough for
that result to count as strict parity evidence. Use the stable `test` save for state parity; the
exact revision-58 cumulative OFF/ON and OFF/OFF run passed there. A dedicated remote vanilla-server
join was not run in this measurement.

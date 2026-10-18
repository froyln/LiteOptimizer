# Benchmark evidence

## Retained raw measurements

These CSVs contain client frame durations in nanoseconds (`frame_ns`) and sampled integrated-server
milliseconds per tick (`mspt_ms`). Blank MSPT fields are not zero ticks. The samples were collected
with the same instrumentation in each OFF/ON pair. Instrumentation overhead is not subtracted.

| Dataset | Date / candidate | Fixture | Controlled comparison |
|---|---|---|---|
| [Terrain RD16](terrain-rd16/) | 2026-10-15 / revision 95, 0.6.0-rc2 | B6 flight, render distance 16 | Only `sodium` differs; 20 s warmup, 40 s recording, 0.5 blocks/tick forward flight. |
| [Terrain RD10](terrain-rd10/) | 2026-10-15 / revision 95, 0.6.0-rc2 | B6 flight, render distance 10 | Only `sodium` differs; same warmup, recording duration and speed. |
| [Item frames](item-frames/) | 2026-10-13 / 0.6.0-rc2 | B1 storage, fixed pose, item frames retained, moving entities suppressed | Only `model_display_lists` differs; 20 s warmup, 40 s recording. |

Terrain config snapshots and the tested artifact SHA-256 are retained with the data. The item-frame
runtime artifact SHA-256 was `843d887cd841188932474fc3b15d9d8a07a800b05fd40144a6130314dd04209e`;
a later version-string correction did not change its rendering implementation. These are historical
candidate benchmarks, not measurements of a new UI-only build. No aggregate all-defaults-on
versus bare vanilla result has been established for the current candidate.

All three pairs used the technical LiteLoader mod stack. The reports do not retain a reliable
hardware/driver inventory for these exact captures, so no CPU, GPU or driver model is asserted.
The B1 frustum report records a 1916 × 1001 image extent; resolution for the retained terrain
captures is not established here. VSync/FPS-cap, OS and Java patch versions are not fully recorded
for these exact pairs. Do not treat these results as a standardized cross-machine benchmark.

The full benchmark worlds are not distributed. These CSVs allow verification of the arithmetic,
not reproduction of the complete game scene from a fresh checkout.

## Recalculate

From the repository root, with Python 3:

```bash
python3 tools/bench-summary.py docs/benchmarks/terrain-rd16/off.csv docs/benchmarks/terrain-rd16/on.csv
python3 tools/bench-summary.py docs/benchmarks/terrain-rd10/off.csv docs/benchmarks/terrain-rd10/on.csv
python3 tools/bench-summary.py docs/benchmarks/item-frames/off.csv docs/benchmarks/item-frames/on.csv
```

- Mean frame time: arithmetic mean of `frame_ns`, divided by 1,000,000 to obtain milliseconds.
- Average FPS: 1,000 divided by mean frame time in milliseconds.
- Slowest 1% mean: sort all frame times, take entries from `floor(frame_count × 0.99)` to the end,
  and compute their mean. This is not a 99th-percentile cutoff or an FPS value.
- Time reduction: `(OFF − ON) / OFF × 100`.
- FPS increase: `(ON − OFF) / OFF × 100`.

Use unrounded CSV means when calculating percentages; displayed README timings are rounded.
For example, RD10 is a 22.4% reduction using raw means, while its rounded values can produce a
slightly different result. Reducing time by 59.7% corresponds to increasing FPS by 148.2%, because
FPS is the reciprocal of frame time. These percentages must not be interchanged.

The summary tool prints signed time deltas, so a negative percentage denotes an improvement.
Its `1% low` output is in **milliseconds**, not conventional 1%-low FPS.

## Historical measurements without retained raw CSVs

These measurements were transcribed from the existing project reports/catalog. Their provenance
is weaker than the raw pairs above; they are disclosed separately so readers can distinguish
recalculable results from historical summaries.

### Block-entity frustum culling — October 14, 2026

B1 storage, fixed pose, moving entities suppressed, other optimization settings held enabled,
20 seconds warmup and 40 seconds recording. OFF/ON captured 6,610 / 9,821 frames. Average frame
time 6.05 → 4.07 ms (−32.7%); slowest 1% mean 23.65 → 9.54 ms (−59.7%). The `blockentities`
profiler section fell 4.192 → 2.629 ms/frame (−37.3%). Maximum frame times were 734 → 23 ms;
this single extreme comparison is noisy and is not claimed as an improvement. Sampled MSPT fell
4.10 → 3.93, but this client-render feature is not credited with that simulation change.

Measured artifact: `4b6aa445dac0dddeecff1c9fd464d72b580af349f65c2c917fb333af5e8bd8e2`.
The report records zero differing pixels in the gameplay crop at this pose, with changing HUD
text accounting for the overall difference. This is a rendering omission optimization, not a
promise of exact pixels in every scene. The B1 OFF/OFF state control was nondeterministic and
its OFF/ON state comparison is not counted as strict parity proof.

### Camera visibility cache — October 14, 2026

B6 forward flight, render distance 16, 0.5 blocks/tick, 20 seconds warmup and 40 seconds recording.
Only the cache option differs; other candidate options were enabled. Reported pairs:

| Pair | Mean frame time OFF → ON | Reported reduction | Slowest 1% mean OFF → ON |
|---|---:|---:|---:|
| 1 | 0.97 → 0.95 ms | 2.1% | 4.29 → 4.27 ms |
| 2 | 0.99 → 0.95 ms | 3.9% | 4.47 → 4.03 ms |
| 3 | 0.99 → 0.96 ms | 3.4% | 4.45 → 4.10 ms |

The report gives median reductions of 3.4% for mean frame time and 7.7% for slowest 1% mean.
Percentages used the original unrounded samples; those are not retained here. The graph-rebuild
profiler section fell from 0.083–0.088 to 0.043–0.046 ms/frame, approximately 48–51%. Overall frame
gain is small and close to the project's 3% noise threshold. MSPT changes were inconsistent;
maximum frames remained hundreds of milliseconds in both modes. Measured artifact:
`40e8afe90388336159cfc5a3ed934c10f48ec76927b62886edd30f60edc6288d`.

### Memory and simulation — September 18, 2026 catalog/history

- Blockstate property storage: about 10.5 MB fewer Guava map/table objects at the B1 pose;
  old-generation occupancy about 14 MB lower after two forced garbage collections.
  `ImmutableMapEntry` count about 152k → 16k; `SingletonImmutableBiMap` 68k → 0;
  `SparseImmutableTable` 4.7k → 0. These are overlapping measurements of one change.
- Baked-quad deduplication: 43,690 baked quads → 29,214 unique quads, a calculated 33.1% reduction;
  reported heap reduction approximately 2.2 MB. No FPS claim.
- Property hash caching: isolated B3 hopper run, 3.78 → 3.07 MSPT (calculated 18.8% reduction);
  historical report also records frame mean −21% and slowest 1% mean −50%, without raw frame
  timings retained here.
- Hopper entity lookup: isolated B3 hopper run, 3.75 → 3.22 MSPT (calculated 14.1% reduction).
- Structure lookup index: historical B4 mob-farm report says “−30%” while displaying approximately
  7.2 → 5.79 MSPT. Those timings imply approximately 19.6%, and neither the exact baseline nor
  isolated raw pair is established. No numeric structure-lookup speedup is asserted in the README.

## Correctness scope

Shared/server changes must preserve state, RNG consumption, update order and side effects.
The revision-101 stable fixture comparison ran 6,000 ticks at render distance 2 with all options
OFF twice and then ON. All three state dumps matched SHA-256
`9494ea25e3dee2acba33a46502beb2369ea15a1ccc1797f0ba7130c729ee6341`.
Subsequent pre-publication production changes concerned the client render upload queue and UI,
not those world/memory implementations. This is recorded fixture coverage, not a proof for every
possible mod/world combination. The storage fixture is unsuitable for strict parity because its
unchanged OFF/OFF control diverged; higher-distance loading variance was also observed.

Render changes are client-only and may have documented presentation differences. They must not
change movement, inventory actions, interaction reach or server-bound packet semantics. Exact
pixel comparison is a fixed-scene check only. Full-stack gameplay checks and an unmodified vanilla
server complement the state harness; the README records the remaining compatibility limitations.

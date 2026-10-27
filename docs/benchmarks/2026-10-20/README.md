# Whole-mod benchmark · October 20, 2026

These runs compare LiteOptimizer's release-default settings with every optimization switch off.
The mod, dev measurement hooks, and the same technical LiteLoader stack remained installed in both
modes. This is not bare Minecraft versus LiteOptimizer.

## Frame-time runs

Each workload has three OFF/ON pairs. Runs alternated order: OFF then ON, ON then OFF, OFF then ON.
Every run used a 30-second warmup and 60-second recording, VSync off, 1920×1080, and an FPS limit
setting of 260. Recorded rates exceeded 260. No profiler or vertex dump ran during the frame tests.
Bars in the charts show the median of three run measurements; whiskers show the lowest and highest
run. Percentage labels show the median of the three paired percentage changes, calculated from
unrounded results.

| Workload | Render distance | Fixture and movement | Options changed |
|---|---:|---|---|
| B1 storage | 10 | Fixed storage-scene pose; entities suppressed | All optimization switches OFF vs release defaults ON |
| B6 flight | 16 | Forward flight at 0.5 blocks/tick | All optimization switches OFF vs release defaults ON |

The B1 FPS effects varied from +61.9% to +95.3%, because the first ON run was slower than the next
two. B6 FPS effects ranged from +48.7% to +54.1%. These are paired scene measurements, not an
estimate for all worlds or machines.

The recorded client frame durations are in nanoseconds (`measurement.csv`, column `frame_ns`).
The `mspt_ms` column contains sampled integrated-server tick times. Samples can miss ticks; these
values are not remote-server TPS. Instrumentation is present in both modes and its overhead is not
subtracted.

Per-run folders contain the raw CSV, effective optimization config, renderer config, and a compact
`result.json` summary. The top-level `summary.json` contains the calculated pair effects and
per-mode ranges. `environment.json` records system, Java, GPU, resolution, render distance,
measurement duration, and other settings. The `*-fixture.sha256` files identify the exact source
world archives, which are not distributed. `artifact.sha256` and `source-commit.txt` identify the
mod binary and source revision.

## Isolated feature checks

### Structure lookup

Three B4 pairs changed only `structure_lookup_index`, with other release-default options enabled.
The median paired FPS change was −0.3% (range −0.6% to +0.3%), which is within run variation.
Sampled integrated-server MSPT fell by a median 15.6% across the pairs (range −16.8% to −11.4%).
This is one workload and sampled tick metric, so it is not a general server-speed claim. Earlier
project notes reported a structure gain but had inconsistent displayed timings; this run does not
support a rendering gain claim.

### Memory features

One OFF/ON pair was collected for each memory feature, with forced garbage collection and class
histograms. These are preliminary and do not establish stable total-heap or frame-rate changes.

- `blockstate_property_storage`: FPS changed +0.3%; mean frame time −0.3%. The heap histograms
  show changes in Guava map/table classes, but a single forced-GC pair is too noisy to claim a
  total heap percentage.
- `baked_quad_dedup`: FPS changed +1.0%; mean frame time −1.0%. The recorded model-load count was
  43,690 baked quads and 29,214 unique quads, 33.1% fewer instances. That count is not a total
  heap saving.

The two memory pair folders retain the complete class histograms as `heap-histogram.txt`. Their
recording-window frame and MSPT metrics are not used as general performance claims because the
heap procedure includes forced GC.

## Excluded setup attempts

Several initial B1 setup/pilot attempts were excluded before analysis because automated screenshots
overlapped recording, cached launcher arguments differed from the intended run, or the runner
failed while collecting output after a completed recording. Exclusions were recorded locally with
their reasons; none are included in the 22 accepted results or summary. Accepted runs passed the
settings and artifact audit.

## Recalculate and regenerate charts

From the repository root, with Python 3 and matplotlib installed:

```bash
python3 tools/bench-summary.py docs/benchmarks/2026-10-20/results/B6-full-p1/off/measurement.csv docs/benchmarks/2026-10-20/results/B6-full-p1/on/measurement.csv
python3 tools/plot-benchmarks.py
```

Percent changes use `(ON − OFF) / OFF × 100` for FPS and `(OFF − ON) / OFF × 100` for time metrics.
The charts use the medians of three runs for bar lengths and the run minimum-to-maximum range for
whiskers. Percentages use paired changes rather than treating thousands of frames as independent
repetitions.

# Revision 92 Sodium pillar — B6 flight, 2026-10-15

The exact revision-92 artifact (SHA-256 `3fa65abf14ad25a7850986e00e2c18bed7b252abc390bc2eb9ba9a172e5705e1`) was measured in the B6 elytra-flight fixture at render distance 16. Command: `tools/bench.sh /home/froyln/Dev/OptimizationMod-1.12.2-worlds/B6-flight 20 40 --compare sodium --fly 0.5 --profile`. Other options were held constant; the saved configs differ only in `sodium` (`false`/`true`).

| Mode | Frames | FPS | Average frame | 1% low | Max frame |
|---|---:|---:|---:|---:|---:|
| Sodium off | 14,423 | 360.6 | 2.77 ms | 11.07 ms | 325.8 ms |
| Sodium on | 41,665 | 1,041.6 | 0.96 ms | 4.61 ms | 276.5 ms |

Average frame time fell 65.4%; 1% low frame time fell 58.4%. This is one paired run, so treat the magnitude as preliminary pending a repeat pair. The captured integrated-server MSPT also changed, but it is not evidence of remote-server TPS gain and is not claimed. This aggregate result does not isolate or validate every Sodium sub-optimization.

Raw CSV, profiler summaries, chunk listings, and exact option snapshots are stored beside this report.

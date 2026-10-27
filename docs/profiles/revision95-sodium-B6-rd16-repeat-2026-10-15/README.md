# Revision 95 Sodium B6 flight repeat, render distance 16

The exact revision-95 artifact (SHA-256 in `artifact-sha256.txt`) was tested in the disposable full-stack Prism clone on the B6 flight fixture. This matches the revision-92 benchmark's render distance and settings: 20-second warmup, 40-second recording, 0.5 blocks/tick forward flight, and the same profile instrumentation. The saved OFF/ON configs differ only in `sodium`.

| Mode | Frames | FPS | Average frame | 1% low | Max frame |
|---|---:|---:|---:|---:|---:|
| Sodium off | 11,677 | 291.9 | 3.43 ms | 11.04 ms | 96.1 ms |
| Sodium on | 28,980 | 724.5 | 1.38 ms | 5.25 ms | 81.4 ms |

Average frame time fell 59.7%; 1% low frame time fell 52.5%. This repeats the direction of the revision-92 pair (65.4% and 58.4% reductions). The exact revision-95 code is thus measured twice on B6 with Sodium isolated as the only config change; the additional render-distance-10 pair is in [`revision95-sodium-B6-rd10-2026-10-15`](../revision95-sodium-B6-rd10-2026-10-15/README.md). These aggregate measurements do not isolate individual Sodium features. MSPT is the integrated server and is not evidence of remote-server TPS.

The clone's render distance was restored to 10 after this run. CSVs, profiles, rendered chunk lists, config snapshots, and the ON log are saved beside this report.

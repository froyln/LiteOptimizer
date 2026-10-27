# Revision 95 Sodium B6 flight benchmark, render distance 10

The exact revision-95 artifact (SHA-256 in `artifact-sha256.txt`) was tested in the disposable full-stack Prism clone on the B6 flight fixture. Sodium was the only optimization option changed: `false` in `off.config.json`, `true` in `on.config.json`. Both runs used render distance 10, a 20-second warmup, a 40-second recording window, 0.5 blocks/tick forward flight, and the same profile instrumentation.

| Mode | Frames | FPS | Average frame | 1% low | Max frame |
|---|---:|---:|---:|---:|---:|
| Sodium off | 42,310 | 1,057.7 | 0.95 ms | 5.46 ms | 134.8 ms |
| Sodium on | 54,521 | 1,363.0 | 0.73 ms | 3.27 ms | 116.5 ms |

Average frame time fell 22.4%; 1% low frame time fell 40.2%. The B6 profile confirms the measured scene exercised terrain, entities and chunk updates. The benchmark runner used the clone-local properties file because Prism cached JVM arguments; it waited for the target Java process to exit before beginning the next mode. The ON log is preserved; the OFF CSV, profile, chunk list and config snapshot are preserved alongside it.

This is a valid repeat pair at render distance 10, but it is not a direct replication of the revision-92 pair at render distance 16. Together the pairs show a Sodium gain in two runs/settings; they do not isolate each Sodium sub-optimization. MSPT is the integrated server and is not evidence of remote-server TPS.

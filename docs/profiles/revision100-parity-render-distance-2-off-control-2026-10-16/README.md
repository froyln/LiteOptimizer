# Revision 100 fixed-distance parity repeat control

The exact revision-100 artifact (SHA-256 `af32613c211a13d7e89216809a6b2e0bfb360a193b7f967c4b777c2f9b5ee86f`)
was run twice with all LiteOptimizer options disabled for 6,000 integrated-server ticks on the
stable `test` save at render distance 2. The disposable full-stack Prism clone ran both sessions.

The OFF/OFF determinism control **failed**: all dumped chunks match except chunk `(-120, 44)`.
The first dump has total `cd4b8d21…`; the second has total `9494ea25…`, matching the known
revision-94 fixed-distance baseline. The full dumps are [off.txt](off.txt) and [off2.txt](off2.txt).

This reproduces the intermittent single-chunk outlier despite the fixed-distance fixture. It does
not establish an optimization parity failure, because both runs had the same optimizations disabled;
it does prevent this revision-100 attempt from serving as a valid determinism or OFF/ON gate. A
later verbose OFF run matched the revision-94 baseline at `9494ea25…` and recorded per-tick state,
but Prism did not acknowledge its repeated run before the harness timeout. See the [verbose
reference profile](../revision100-parity-verbose-off-control-2026-10-16/README.md). The clone's
original render distance (10) was restored, and the harness removed its temporary save and run
configuration.

# Revision 58 cumulative state parity — 2026-10-15

The exact revision 58 artifact (`c2232ba38f8b13554eddb4f18f4dd871bf708c808dddd1f6a2452f0651bbb760`)
was installed in the disposable full-stack Prism clone. The Java 8 offline build and four focused
checks passed. The latest revision 60 build has SHA-256
`b94e8754c257b037d2674859d5ae7c52e901dddd6598c2e3f163a9d3a86e9243`; archive comparison confirms
that only `litemod.json`'s automatic revision number changed (58 → 60). Revision 60 is now
installed byte-for-byte in the disposable clone, but has not been relaunched; see
[`payload comparison`](revision60-payload-compare.json) and
[`clone install identity`](revision60-clone-install.json).

On the stable `test` parity save, 6,000-tick OFF/ON and OFF/OFF runs produced byte-identical dump
files. Each file has SHA-256 `ec40cde8cf712ecd6b80a06d98d524bc1dc7ebc141a0c976edf5dbf60877912b`
and ends with total hash
`9fa11a43d28a3e5cf6939c25c1cede3ff1d61b4cae5e572e2eaacbdccec03e21`. The second OFF run is the
vanilla determinism control. Dumps: [OFF](off-6000.txt), [ON](on-6000.txt),
[second OFF](off2-6000.txt).

The archived [ON log](2026-10-15-6.log.gz) shows all release-default optimizations active and the
6,000-tick run completing; the [OFF log](2026-10-15-7.log.gz) records the matching all-off control.
The [second OFF log](revision58-off2-latest.log) records the repeated control. All three are from
the full installed technical-mod stack and report a successful `ParityTester` join. No archived
phase contains a Mixin application error.

The B1 storage smoke save is not a valid deterministic parity fixture: a 600-tick OFF/OFF control
already differed in final `World.rand` seed. Chunk/entity/tick state otherwise matched; verbose
traces showed varying loaded-chunk timing and random-call counts even with the optimizer disabled.
This is fixture nondeterminism, not a mod-on/mod-off regression. Do not weaken the RNG comparison or
report B1 as a parity pass; use B1 for client rendering/interaction measurements only.

This closes cumulative state parity only for the stable `test` save and exact code payload. It does
not close the current vanilla-server interaction gate or the installed-mod feature checklist.

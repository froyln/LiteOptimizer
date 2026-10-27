# Revision 86 full-stack state parity — 2026-10-15

Built with Java 8 offline `./gradlew build`; the release `.litemod` SHA-256 is
`fbe8ea0019561b4c0edc89baf956b15296e8c39cd175c44b6e825f08f463b934`, manifest revision 86.
The same archive was staged byte-for-byte in the disposable Prism instance
`1.12.2 test ai parity rev44`; its installed technical mod stack remained enabled. Prism launched
the clone with its offline `ParityTester` profile. The owner's separate `1.12.2 test ai` process
was left running and untouched.

Command:

```sh
PARITY_INSTANCE='1.12.2 test ai parity rev44' \
PARITY_PRISM_ROOT='/home/froyln/.local/share/PrismLauncher' \
tools/parity.sh '/home/froyln/.local/share/PrismLauncher/instances/1.12.2 test ai parity rev44/minecraft/saves/test' 6000
```

The harness copied the stable `test` save to `_parity` for each run, used its default seed 1, and
produced matching all-optimizations-OFF and all-optimizations-ON state dumps after 6,000 ticks.
Both dump files have SHA-256
`ec40cde8cf712ecd6b80a06d98d524bc1dc7ebc141a0c976edf5dbf60877912b`; both end in total hash
`9fa11a43d28a3e5cf6939c25c1cede3ff1d61b4cae5e572e2eaacbdccec03e21`. The harness reported
`PASS`. Both logs record LiteOptimizer loaded, `ParityTester` joined, and the run starting with
6,000 ticks. OFF disabled the full optimization set; ON enabled all config keys. The harness
restored the clone config and removed `_parity` after completion.

Evidence: [OFF dump](off-6000.txt), [ON dump](on-6000.txt), [OFF client log](off-client.log.gz),
[ON client log](on-client.log). This closes cumulative integrated-world state parity for revision
86 on this stable fixture. It does not prove individual optimization gains, render behavior,
vanilla-dedicated-server interaction, or the full installed-mod feature checklist.

# Revision 87 integrated-world state parity — 2026-10-15

The exact revision 87 archive ran 6,000 ticks with all optimizations off and with the production
optimization keys enabled on the stable `test` save. The deterministic dumps are byte-identical.

## Artifact and command

- Artifact: `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, manifest revision 87.
- SHA-256: `d726a3465ba3049a8cd2045ed5b3eb1fa0ff0d8f9c1b4af5d59dbbb78aa75126` (build and staged
  clone archive match).
- Clone: `1.12.2 test ai parity rev44`, with the full technical-mod stack installed.
- Command:

  ```sh
  PARITY_INSTANCE='1.12.2 test ai parity rev44' \
  PARITY_PRISM_ROOT='/home/froyln/.local/share/PrismLauncher' \
  tools/parity.sh '/home/froyln/.local/share/PrismLauncher/instances/1.12.2 test ai parity rev44/minecraft/saves/test' 6000
  ```

The OFF and ON dumps both have SHA-256
`ec40cde8cf712ecd6b80a06d98d524bc1dc7ebc141a0c976edf5dbf60877912b` and end in total hash
`9fa11a43d28a3e5cf6939c25c1cede3ff1d61b4cae5e572e2eaacbdccec03e21`. The runner reported
`PASS`. The ON config was normalized through LiteOptimizer's release gate; the log lists the
intentionally disabled, not-yet-qualified options. The runner restored the clone config and removed
its temporary `_parity` save.

This closes cumulative integrated-world parity for revision 87 on the stable fixture. It does not
prove each optimization's individual gain, current-revision vanilla-server interactions, or the
remaining full installed-mod feature matrix.

## Evidence

- `off-6000.txt`, `on-6000.txt`: matching state dumps.
- `off-client.log.gz`, `on-client.log`: each run's exact client log.

# Revision 57 clone startup smoke — 2026-10-15

Build: `build/libs/liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, manifest revision 57,
SHA-256 `90625aefe0c4acedbc42be3525a81691f184c3e8c88715b78e06d66190377f59`. The archive installed
in Prism clone `1.12.2 test ai parity rev44` had the same hash.

With a temporary empty config (missing keys use their defaults), LiteLoader initialized the full
technical mod stack and LiteOptimizer logged the nine evidence-backed settings active: Sodium,
structure lookup index, baked quad dedup, blockstate property storage, model display lists,
tile-entity frustum, camera visgraph cache, property hashcode cache, and hopper entity lookup. No
LiteOptimizer mixin-application failure appeared in `minecraft/logs/latest.log`.

The disposable `B1-storage-smoke` world was selected. The log records an integrated 1.12.2 server
starting and `LiteOptSmoke` joining at `(-1969.5, 37.256, 700.5)`. The server saved its chunks after
the window lost focus. The clone process was then stopped and its previous config restored.

This proves current-artifact startup with measured defaults and an integrated-world join only. It
does not prove state parity, remote vanilla-server behavior, visual correctness, per-option gain, or
the installed-mod feature checklist.

# Revision 98 build verification

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` completed successfully
on 2026-10-15 (`BUILD SUCCESSFUL`, 8 seconds). The produced
`liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod` has SHA-256
`cd0b0951dd78187fafc43081446938fff649f63a0c81af12c4fe1861fb209170` and manifest revision
98. It was copied byte-for-byte to the disposable full-stack Prism clone's LiteLoader mods
directory.

Focused checks passed: `check-audit-regressions.py`, `check-sodium-options.py`,
`check-chunk-build-revisions.py`, `check-tweakeroo-bridge.py`, shell syntax checks for the
benchmark/parity/profile/install scripts, and `git diff --check`.

The exact revision-98 archive then launched with the full technical mod stack, initialized LiteOptimizer, and joined the disposable B1 world at `(-1969.5, 37.256, 700.5)`. The archive hash in the clone matches the build. The log shows no LiteOptimizer Mixin application failure. Evidence: [`fullstack-startup.log`](fullstack-startup.log) and [`fullstack-b1-join.png`](fullstack-b1-join.png).

This proves current-artifact startup and B1 world join only. Vanilla-server interaction and the remaining full-stack feature checks are still open; ItemScroller remains unverified.

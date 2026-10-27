# Revision 101 build

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0 on 2026-10-16. The reobfuscated LiteLoader artifact is `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, SHA-256 `93bcdac9797fa11abbafcd9af3e3216a91201846041e8a3f09df0b0168d8de6e`, manifest revision `101`.

An archive comparison against the exact revision-100 artifact found identical entry names and identical contents everywhere except `litemod.json`'s `revision` field. Thus revision-100 vanilla-server runtime interactions apply to the same code payload; revision 101's new evidence is recorded separately for fixed-distance parity and the full-stack B1/Carpet UI smoke.

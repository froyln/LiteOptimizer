# Revision 104 build

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0 on 2026-10-16. The reobfuscated LiteLoader artifact is `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, manifest revision `104`; its SHA-256 is recorded in [`artifact-sha256.txt`](artifact-sha256.txt).

Audit regressions, Tweakeroo bridge present/absent checks, chunk-build revision filtering, Sodium options, and `git diff --check` passed. Compared with the exact revision-102 artifact installed in the disposable full-stack Prism clone, all 729 entries are present with identical names and only `litemod.json` differs. The exact revision-102 code payload launched with the full stack and passed the vanilla-server interaction and chunk-streaming checks; revision-101 parity and revision-100 trade/reconnect evidence also carry forward to this build. The full installed-mod feature matrix remains open. Archive comparison: [`archive-compare-rev102.json`](archive-compare-rev102.json).

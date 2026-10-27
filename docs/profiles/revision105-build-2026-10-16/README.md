# Revision 105 build

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0 on 2026-10-16. The reobfuscated LiteLoader artifact is `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, manifest revision `105`; its SHA-256 is in [`artifact-sha256.txt`](artifact-sha256.txt).

The audit regression, Tweakeroo bridge present/absent, chunk-build revision, Sodium option, profile checksum, and `git diff --check` validations passed. Compared with the exact revision-104 artifact used for the B1 smoke, all 729 entries have identical names and only `litemod.json` differs. Thus revision-104 full-stack/Litematica evidence covers the unchanged code/resource payload, while revision 105 is the latest verified build. The full installed-mod feature matrix remains open. See [`archive-compare-rev104.json`](archive-compare-rev104.json).

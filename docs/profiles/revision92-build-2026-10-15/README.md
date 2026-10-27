# Revision 92 build and full-stack B1 smoke

- Build: `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` — passed.
- Artifact: `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`.
- SHA-256: `3fa65abf14ad25a7850986e00e2c18bed7b252abc390bc2eb9ba9a172e5705e1`.
- The artifact installed in the disposable full-stack Prism clone has the same SHA-256.
- Compared with the tested revision-91 archive, all 729 entries match except the manifest revision.
- LiteLoader initialized LiteOptimizer with the technical mod stack. The log reports the nine evidence-backed defaults enabled and no LiteOptimizer mixin application failure.
- The exact artifact reached the Minecraft main menu, loaded the disposable `B1-storage-smoke` world, joined its integrated server at the saved test position, and rendered the scene; see `b1-rendered.png`.

This is startup and visible-world evidence only. It does not complete the installed-mod feature matrix, the remaining vanilla-server action cases, the Tweakeroo dirty-block edit with main-thread rendering enabled, or per-candidate gain verification.

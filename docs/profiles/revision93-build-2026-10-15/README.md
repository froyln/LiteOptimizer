# Revision 93 build and full-stack B1 smoke

- Build: `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` — `BUILD SUCCESSFUL` (8 s).
- Artifact: `build/libs/liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, manifest revision `93`, SHA-256 `bf6744c36e52f210b521b51e8a1f6b06effd34294d31b3844dfc62a05330a4e1`. The copy installed in the disposable Prism clone has the same hash.
- Runtime: launched the exact artifact with the full installed technical mod stack in the isolated Sway/WayVNC session, reached the main menu, joined disposable `B1-storage-smoke`, and rendered the scene at the saved position. The captured scene is `full-stack-b1.png`; the full game log is `latest.log`.
- The startup log reports LiteOptimizer initialized and logs eight enabled options; `camera_visgraph_cache` is also enabled in the runtime config, for nine validated candidates total. The sixteen candidates still awaiting release evidence are disabled. The log also contains the foreign Mixin redirect warnings seen on earlier full-stack revisions, plus missing optional config warnings for MaLiLib and SchematicPreview in this clone. No `InvalidMixin`, `MixinApplyError`, or fatal error appears. The revision-93 archive has 729 entries and differs from the installed revision-92 artifact only in `litemod.json` revision; the revision-92 payload was previously verified against revision 91.

This proves exact-artifact startup and B1 rendering with the installed stack, not the unfinished per-mod feature matrix or the vanilla-server interaction gate.

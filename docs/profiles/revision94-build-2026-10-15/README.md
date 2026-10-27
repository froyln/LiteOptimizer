# Revision 94 build and full-stack B1 smoke

- Build: `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` — `BUILD SUCCESSFUL` (6 s).
- Artifact: `build/libs/liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, manifest revision 94, SHA-256 `faa2ba14b109f35c698c86d078c68dcd36e271ae4729aac0dca894c371986b68`.
- The artifact copied into the disposable full-stack Prism clone has the same SHA-256. Archive contains 729 entries.
- Compared with the installed revision-92 archive, all 729 entries match except the manifest revision field. This carries forward the revision-92 vanilla-server checks to the revision-94 code payload.
- Runtime: exact revision 94 launched with LiteOptimizer enabled and TellMe disabled as in the target profile. It reached the main menu and joined/rendered the disposable `B1-storage-smoke` copy in the isolated Sway/WayVNC session. Screenshot and full client log are saved here.
- LiteOptimizer initialized with the release-gated options; no `InvalidMixin`, `MixinApplyError`, or fatal startup error appears. Known foreign redirect warnings and missing optional MaLiLib/SchematicPreview config warnings remain visible.

This confirms latest-artifact startup and B1 rendering with the target mod profile; it does not close the remaining per-mod feature matrix or optimization gain evidence.

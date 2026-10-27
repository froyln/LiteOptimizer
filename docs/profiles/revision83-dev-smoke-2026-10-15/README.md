# Isolated development-client smokes — 2026-10-15

A Java 8 offline `./gradlew build` exited 0 before the smoke. The exact litemod archive hash for
the world-join run was not preserved. The isolated ForgeGradle `runClient` used LiteLoader's
bundled Mixin 0.7.5 via `/tmp/liteoptimizer-runclient-mixin075.init.gradle`; LiteOptimizer logged
`Successfully added mod` and its normal startup status.

The client joined the disposable `New WorldLiteOptSmokeiteOptSmoke` world at 06:43:46 and reached
a rendered first-person terrain scene. No `NoSuchFieldError: loadedChunks` or client crash followed.
This exercises the MCP-layout `renderEntitiesDevelopmentLocals` callback and the prefixed mixin-owned
chunk tracking fields. Mixin emitted one `CAPTURE_FAILSOFT` warning for
`renderEntitiesProductionLocals`, whose narrower production LVT does not match the MCP layout.
The production-layout callback must still be verified against the current reobfuscated artifact.

This was an isolated development client with LiteOptimizer, not the owner's full mod stack. It does
not prove strict parity, exact-current vanilla-server interaction, measured gain, or release
readiness. A follow-up dev-mapped revision 85 archive (SHA-256
`e83aa92764a2742b8061d137fbfe3eaf340a871db797c7264b8a0106f91ed107`) was launched via
`runClient`; LiteOptimizer initialized and no `NoSuchFieldError: loadedChunks` appeared. That follow-up
did not join a world. The current reobfuscated revision 86 release archive (SHA-256
`fbe8ea0019561b4c0edc89baf956b15296e8c39cd175c44b6e825f08f463b934`) is staged in the disposable
full-stack clone, but the clone still needs an authorized account for runtime checks; no credentials
were copied.

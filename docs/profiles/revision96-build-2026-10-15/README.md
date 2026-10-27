# Revision 96 exact build smoke, 2026-10-15

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` succeeded and produced revision 96, SHA-256 `fa9e65d623f53d1c44618b2721555511107537f011f25b83638c227e1ff9298c`. The exact artifact was copied to the disposable full-stack Prism clone with the same SHA-256. It initialized LiteOptimizer and the installed technical mod stack, reached the Minecraft title screen, then exited cleanly. `latest.log` contains the launch and shutdown; the only logged asset/runtime warnings in this smoke are the clone's known missing mod textures and narrator library.

The archive comparison against the tested revision-95 artifact found the same entry list and identical contents except `litemod.json`'s revision field. The revision-95 exact artifact passed the B6 benchmark pairs in the adjacent revision-95 profiles; those measurements carry across the metadata-only revision change. Revision 96 itself was not joined to a world in this smoke.

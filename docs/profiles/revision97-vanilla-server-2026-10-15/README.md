# Revision 97 vanilla-server follow-up — 2026-10-15

The exact revision-97 artifact (`4a80a24fc6beb166872e30dcee58d982d8c6757c877220ad8d7e3fd639f71882`) joined the disposable local Mojang 1.12.2 server, which had no mods or plugins. The full technical mod stack was present on the client.

Server-side observations:

- `Kisde` joined at the previously saved position `(1207.7157, 4.0, 517.7199)` and rendered the flat overworld.
- After resetting `stat.walkOneCm`, client W movement raised the score to 1,338 cm.
- With movement settled, an RCON relative teleport of 0.25 blocks was accepted at `(1208.1302, 4.0, 530.8785)`. The server emitted no `moved wrongly` warning. Further client movement raised the score to 1,950 cm, confirming play continued after the correction packet.
- The disposable server completed `save-all` and stopped cleanly.

`client.log`, `server.log`, and `overworld.png` preserve the session. The world is a disposable validation copy. This session verifies join, rendering, movement, and a small server-issued correction on revision 97; it does not retest block placement/breaking, potion use, trading, inventory transfer, or portal travel, which have exact revision-96 evidence. Revision 97 differs from revision 96 only in the manifest revision number.

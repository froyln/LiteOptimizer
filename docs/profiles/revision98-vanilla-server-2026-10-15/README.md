# Revision 98 vanilla-server smoke follow-up

The exact revision-98 artifact (SHA-256
`cd0b0951dd78187fafc43081446938fff649f63a0c81af12c4fe1861fb209170`) was installed in the
disposable full-stack Prism clone and joined an unmodified Mojang 1.12.2 server on loopback.
The server log records `LiteOptTest` joining at `(100.5, 4.0, 106.54)`. This is the same
artifact previously recorded in [revision 98 build verification](../revision98-build-2026-10-15/README.md).

After setting the vanilla `stat.walkOneCm` scoreboard to zero, holding W increased it to 876,
confirming ordinary client movement reached the server. A later settled 0.0625-block RCON
position correction from X=100.75 to X=100.8125 produced no `moved wrongly` or `moved too
quickly` warning and the client rendered at the corrected location. An earlier 0.25-block
correction immediately after joining did produce `moved wrongly`; it is recorded as a failed
attempt, not a pass. The earlier large teleport/fall attempt is also invalid correction evidence.

This follow-up also proves exact-revision block breaking: the server's `minedirt` statistic
reached 1 and an RCON query confirmed the target became air. The temporary break was restored
with RCON. Stone placement was attempted with a selected stone stack but failed: the target
remained air and `stat.useItem.minecraft.stone` stayed at 0. Placement and the broader
player-action checks remain pending; older revision results are carry-forward evidence, not
retests of this exact archive. The world/server are disposable. See `interaction-results.txt`,
`server.log`, `client.log`, `client-block-break.png`, and `current-scene.png`.

# Revision 35 exact-build follow-up interactions

The tested build and installed LiteLoader archive both have SHA-256
`4c59feacd4b90a9d255893431b59617628db6c1460a1c73824e5b770c3f88645` (revision 35).
The client connected to the unmodified Mojang 1.12.2 server jar with SHA-256
`fe1f9274e6dad9191bf6e6e8e36ee6ebc737f373603df0946aafcded0d53167e`.
All world mutations below were confined to `/tmp/liteoptimizer-vanilla-rev33-gate-2026-10-14`,
a disposable flat server world.

- **Server correction and chunk transition:** while continuously connected, RCON teleported the
  player to `(0.5, 4.0, 0.5)` and then `(260.5, 4.0, 0.5)`. After `save-all flush`, player NBT
  recorded Dimension `0` and Pos `[260.5, 4.0, 0.5]`. The client remained connected and rendered
  the destination; see [capture](revision35-correction-chunk-followup.png),
  [client log](revision35-vanilla-client-followup.log), and [server log](revision35-vanilla-server-followup.log).
- **Potion use:** the server replaced hotbar slot 0 with a Swiftness potion, switched the player
  to Survival, and the client consumed it. Saved player NBT recorded active effect ID `1`
  (amplifier `0`, duration `3366` ticks at the first flush) and `minecraft:glass_bottle` in slot 0.
  See the [post-use capture](revision35-potion-consumed-followup.png) and the same server/client
  logs above. The NBT values were read from the disposable server's saved player data.
- **Natural portal activation/travel:** RCON built a 4-by-5 obsidian frame, cleared its interior,
  and placed vanilla fire in the opening. Server block checks confirmed portal blocks at
  `(11, 5, 0)` and `(12, 6, 0)`. The connected client entered the portal, received the server's
  `We Need to Go Deeper` advancement, and saved player NBT recorded Dimension `-1` and position
  `(-2.5, 21.0, 11.5)`. See [portal capture](revision35-natural-portal-nether-followup.png)
  and the server/client logs.
- **Litematica:** in the disposable B1 copy, the exact revision 35 client loaded
  `6gt_Crafting_Station` and visibly rendered its translucent placement/selection overlay; see
  [capture](revision35-litematica-placement-overlay.png). The world blocks were not changed.
  Layer hotkeys did not produce a clear, attributable visual change, so layer switching and
  all-layer rendering remain unverified. Verifier and paste also remain unverified on this exact
  artifact.

An exploratory teleport to Y=64 in the Nether led to a fall death in the disposable server test;
this was after the portal and potion observations. The client respawned and the final same-session
correction check was repeated safely at overworld surface Y=4. The server was flushed and stopped
cleanly through RCON.

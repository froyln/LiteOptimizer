# Revision 100 vanilla-server smoke

The exact revision-100 artifact (SHA-256 `af32613c211a13d7e89216809b6a2e0bfb360a193b7f967c4b777c2f9b5ee86f`)
was launched in the disposable Prism clone with the full technical mod stack and connected to the
unmodified Mojang 1.12.2 server on localhost. The server log records the join at `(100.5, 4.0,
100.5)`. Client startup logs show the release-default options enabled and LiteOptimizer loaded.
The server kicked the client, which returned to the server list and successfully rejoined; RCON
confirmed the player online afterward.
The client opened a vanilla Fletcher trade GUI and completed a string-for-emerald trade, earning
`What a Deal!` with the vanilla `stat.tradedWithVillager` score at 1. It also consumed a server-
provided Swiftness potion; the potion-use score reached 1, a glass bottle remained, and Speed
appeared client-side. See `villager-trade-complete.png` and `potion-used.png`.
The client moved a 64-stick stack from hotbar slot 2 to the first main-inventory slot; RCON matched
the resulting player NBT and confirmed the former hotbar slot was empty. See `inventory-transfer.png`.

After resetting the vanilla `stat.walkOneCm` objective, two seconds of forward movement increased
the server-side score by 180 cm. In Survival, the server provided five stone, the client right-
clicked a prepared ground surface, and RCON confirmed stone at `(110, 4, 112)` and the vanilla
`stoneuse` score at 1. The disposable world contains the temporary test fixture and is not a user
save. `client.log`, `server.log`, and `scene.png` capture the session.

An RCON correction to `(110.5625, 4.0, 110.5)` was reflected in the client's F3 coordinates as
`110.563 / 4.000 / 110.500`. The server log has no movement warning at this correction; an earlier
10-block fixture teleport did produce a `moved too quickly` warning. The client mined a server-
placed stone block with a diamond pickaxe; RCON confirmed it became air and the screen showed
cobblestone drops. See `correction.png`, `break-drops.png`, and `server.log`.

The disposable server world contains only temporary test fixture changes and is not a user save.

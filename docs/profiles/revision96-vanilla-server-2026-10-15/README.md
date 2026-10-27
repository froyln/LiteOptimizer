# Revision 96 vanilla-server smoke — 2026-10-15

The exact revision-96 `.litemod` (SHA-256 in `artifact-sha256.txt`) connected from the full technical-mod Prism profile to a local, unmodified Mojang 1.12.2 server. The server was an offline-mode disposable clone under `run/vanilla-server-rev96`, with no server mods or plugins; its world was copied from an earlier disposable fixture. The original fixture was left untouched.

Observed checks:

- Server logged `Rev96Vanilla` joining at `(1217.5, 4.0, 504.5)`.
- After resetting the server's `walk` scoreboard, movement through the client increased it to 2632 cm (26.32 m).
- Survival stone placement reached the server: the server-side `stoneuse` statistic became 1, and `testforblock 1217 5 505 stone` returned `Successfully found the block at 1217, 5, 505`.
- The test dirt and placed stone were removed, both coordinates were verified as air, and `save-all` completed before the server stopped.

A direct VNC-held attack did not break the test block, so this run does not add breaking evidence. Breaking is covered by the earlier exact-payload vanilla checks in the compatibility checklist. This smoke only extends the exact revision-96 artifact's evidence for join, movement, and placement; it does not close the complete vanilla interaction or installed-mod release gates.

`client.log` and `server.log` preserve the session. The client log includes a MaLiLib config-override NPE also observed in earlier baselines; it did not prevent server join or these checks.

## Exact-artifact repeat session

A second session used the same SHA-256 artifact and another disposable copy of the local vanilla server. Server-side observations:

- `walk` rose to 1,316 cm after W movement in Survival.
- The client broke a server-placed dirt block; RCON `testforblock 1217 5 516 air` succeeded afterward.
- A server-given Swiftness potion was consumed; scoreboard `potion` reached 1 and RCON matched a glass bottle in hotbar slot 0.
- The client opened a vanilla farmer GUI and completed a pumpkin-for-emerald trade; `stat.tradedWithVillager` became 1 and the server awarded `What a Deal!`.
- Moving five emeralds from hotbar slot 0 to main-inventory slot 9 through the client GUI was confirmed by server NBT selector: `testfor @a[name=LiteOptRev96] {Inventory:[{Slot:9b,id:"minecraft:emerald",Count:5b}]}` found the player.
- A server-created portal block inside an obsidian frame transferred the player to Nether coordinates `(156.5, 50.0, 73.5)` and the vanilla server awarded `We Need to Go Deeper`; the screenshot shows the Nether after leaving the portal. The fire-based portal-frame activation attempt did not form the portal, so this does not prove natural frame activation on rev96.

The server rejected an abrupt RCON teleport across 18 chunk columns with vanilla `moved too quickly` warnings. Do not count that attempt as server-correction or distant-chunk-streaming proof. Earlier exact-payload checks cover client placement; this repeat adds breaking, potion use, completed trade, inventory transfer, and dimension travel. The client disconnected cleanly and server `save-all` and shutdown completed.

`server-rev96-repeat.log`, `client-rev96-repeat.log`, and `nether-travel.png` preserve the repeat session. All mutations were made in the disposable server clone.

## Exact-artifact follow-up session

The same SHA-256 artifact was launched again in the full technical-mod profile and joined the
unmodified local Mojang server. The client rendered the flat overworld; after resetting a
server-side `stat.walkOneCm` objective, four seconds of client W movement produced a server score
of 1,752 cm. This confirms movement reached the server in this session. A one-block RCON teleport
was accepted, but the server then logged `moved wrongly`; it is explicitly not counted as clean
correction evidence. No world blocks were changed. `client-rev96-followup.log`,
`server-rev96-followup.log`, and `rev96-followup-movement.png` preserve this session. The server
score objective and the disposable clone's world are test-only state.

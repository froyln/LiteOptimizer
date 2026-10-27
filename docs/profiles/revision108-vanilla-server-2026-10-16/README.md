# Revision 108 vanilla-server interaction check — 2026-10-16

The exact revision-108 artifact (SHA-256 in `artifact-sha256.txt`) loaded with all 17 LiteLoader mods and connected twice to the unmodified Mojang 1.12.2 server on loopback. The server jar hash is in the same file. `client.log` records LiteOptimizer loading revision 108 with the expected release defaults; `server.log` records both joins. `connected-world.png` is a capture from the isolated WayVNC desktop.

On the first connection, the client moved normally in Survival and the server-side `walkOneCm` objective rose to 876 cm. The client then accepted a small server correction from X=107.869 to X=107.9315 (0.0625 block); F3 showed the corrected coordinate rounded to 107.932, and RCON's exact-position selector matched the server player. The server recorded no `moved wrongly` or `moved too quickly` warning. Reconnect placed the player at the corrected saved position. `rcon-results.txt` records this initial movement, correction, and block fixture.

Placement passed on this artifact. After a clean client launch with the disposable clone's `Use Item/Place Block` binding set to P, holding P placed stone at `(107, 4, 111)`. The new `place108` objective rose from zero to one; RCON found the stone there, and the screen showed the hotbar stack decrease from five to four. See `rcon-placement.txt` for authoritative server results and `placement-and-break.png` for the resulting client view.

A left-hold then broke a dirt block in the same disposable fixture: `minedirt` rose from four to five and the client view showed a new hole. The break was not at the queried grass support `(107, 3, 111)`, which remained grass, and its exact coordinates were not established, so this is evidence of a successful break action rather than an exact-block assertion. `break-action.png` records the result. The server saved the disposable world after the check; the placed stone and break fixture have not been restored.

Breaking of the placed stone, inventory transfer, entity interaction, potion, portal/dimension travel, chunk streaming, and the full installed-mod feature matrix have not been repeated on revision 108. This remains partial current-artifact evidence and does not close either release gate.

This is partial current-artifact evidence only. It does not close the vanilla-server gate or full installed-mod compatibility checklist.

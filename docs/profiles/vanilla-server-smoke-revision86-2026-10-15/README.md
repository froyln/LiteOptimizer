# Revision 86 vanilla dedicated-server smoke — 2026-10-15

The exact current release artifact was staged in the disposable full-stack Prism clone and connected to a local, unmodified Mojang 1.12.2 dedicated server. Core networking and several interactions passed; this is not the complete release matrix.

## Build and server identity

- LiteOptimizer: `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, SHA-256 `fbe8ea0019561b4c0edc89baf956b15296e8c39cd175c44b6e825f08f463b934` (build and staged clone files match).
- Server: official Mojang 1.12.2 server jar, SHA-256 `fe1f9274e6dad9191bf6e6e8e36ee6ebc737f373603df0946aafcded0d53167e`; loopback only, disposable flat world, port 25567.
- Clone: `1.12.2 test ai parity rev44`, with the installed technical-mod stack. The owner's primary instance and world were not used.

## Confirmed on revision 86

- The full-stack client joined the vanilla dedicated server. It disconnected and rejoined; the server log records both joins, and the second login restored the saved test position.
- Client W input increased the server's `stat.walkOneCm` scoreboard from 0 to 1,316. Continued movement later raised it to 2,126.
- With a server-given stone stack, client right-click placed stone at `(72,4,290)`; RCON `testforblock` found stone there. A client left-hold broke it; RCON found air at that coordinate. The long hold also broke the grass block below in this disposable world.
- RCON teleported the player to `(768.5,70,768.5)`; the client rendered the remote flat terrain. This exercised server position correction and distant chunk streaming; the run created region file `r.1.1.mca`.
- A server-given Swiftness potion was consumed through the client. The server's `stat.useItem.minecraft.potion` score is 1, and the client displayed the returned glass bottle and Speed effect.
- A server-spawned farmer opened the vanilla trading GUI. `stat.talkedToVillager` is 1. No trade transaction completed (`stat.tradedWithVillager` is 0), so only entity interaction and GUI opening are confirmed.
- Entering a server-created portal moved the client to the Nether; vanilla awarded `We Need to Go Deeper`. The advancement notification and Nether view are in `nether-travel.png`.

## Remaining checks and limits

- A controlled non-RCON correction and a completed villager trade remain untested on revision 86.
- This does not establish every installed-mod feature or measured performance gains for each shipped candidate.
- After the portal test, `save-all flush` logged vanilla `ConcurrentModificationException` failures while saving chunks amid dimension chunk activity. The server was stopped cleanly afterward, but this disposable world save is not certified clean. This is a test-server limitation; the dimension transition itself is independently evidenced by the vanilla advancement.
- The full-stack client log contains MaLiLib's `ConfigOverrideUtils` NPE on world entry, matching the previously documented no-LiteOptimizer baseline issue.

## Evidence files

- `client-latest.log`: exact clone client's launch, joins, disconnect/reconnect, and server connection log.
- `server-latest.log`: vanilla server login, RCON scoreboard checks, teleport, advancement, save warning, and shutdown records.
- `remote-chunk-view.png`: client view after remote terrain streaming.
- `villager-trade.png`: opened vanilla farmer trading GUI.
- `nether-travel.png`: Nether view and server advancement notification.

The disposable test world is under `run/vanilla-server-rev86/world`; it is not a user world.

# Revision 33 vanilla-server follow-up

Artifact: `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, SHA-256
`66e5f1d20f6477ff96fbf719f989a148bcee8698311498ceb3b7ebd9facc9004`. The same installed
archive was used throughout this check. Server jar is the unmodified Mojang 1.12.2 jar,
SHA-256 `fe1f9274e6dad9191bf6e6e8e36ee6ebc737f373603df0946aafcded0d53167e`.

- The client joined the disposable vanilla server. A W-key input changed F3 Z from `-501.500`
  to `-492.869` while X stayed `406.500`; capture: `revision33-vanilla-movement.png`.
- In Creative, a client right-click placed stone at `(3, 4, 0)`. RCON `testforblock 3 4 0
  minecraft:stone` returned “Successfully found the block.” Client left-click removed it, and
  `testforblock 3 4 0 minecraft:air` returned the same confirmation. Captures:
  `revision33-vanilla-stone-place.png`, `revision33-vanilla-stone-break.png`.
- RCON placed the player at X `260.5`; after save/restart, vanilla logged the reconnect at
  `(260.5, 4.0, 0.5)` and the client rendered the streamed flat world. This establishes the
  persisted destination and chunk-column change; the RCON teleport itself is a test-fixture
  action, not a client-originated movement action.
- A server-summoned vanilla shepherd villager opened its trade GUI via client right-click;
  capture: `revision33-vanilla-villager-trade.png`.
- Client disconnected and server exited via RCON `stop` after `save-all flush`.

The known MaLiLib config-override `NullPointerException` appears on remote-world entry in the
client log; it is a MaLiLib stack trace already observed in the no-LiteOptimizer baseline. No
LiteOptimizer exception was observed. The session did not test a potion use or portal transition.

Logs: `revision33-vanilla-followup-client.log` and
`revision33-vanilla-followup-server.log`.

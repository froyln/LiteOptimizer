# Revision 90 vanilla-server trade smoke — 2026-10-15

This is a disposable local vanilla 1.12.2 dedicated server and disposable Prism clone. The
clone contains the full installed technical mod stack and the revision-90 LiteOptimizer
artifact. The player died to a nearby zombie during the first approach; the test was reset to
day, a vanilla farmer was summoned with normal generated offers, and the trade itself was done
in Survival.

Artifact SHA-256: `d73230a41b5cf0dec1d1059d67e577cb18c8da8c2e5b1d55e342f42bf2d280d2`.

The farmer offered 13 pumpkins for one emerald. The server gave the player 64 pumpkins, then
the client selected that offer and completed a trade. The server awarded `What a Deal!`; its
`stat.tradedWithVillager` scoreboard objective read 1. This confirms the complete transaction,
not only opening the GUI. Relevant server output is preserved in [server-events.txt](server-events.txt);
the raw source log is `run/vanilla-server-rev88/logs/latest.log`.

The same session also verified reconnect on this exact artifact: the vanilla server kicked
`VanillaTester`, the client returned to the server list, and pressing Enter on the saved server
entry rejoined at `(1010.5, 4.0, 1000.5)`. The client log records `Connecting to localhost,
25565` at 10:46:14 and the vanilla server records the login and join at the same time.

This closes the villager transaction and reconnect checks for the revision-90 code payload. A
later potion attempt did not yield a timely server-side ActiveEffects match, so potion use is
still unverified here. The full interaction and installed-mod compatibility gates remain open;
dimension travel and most per-mod feature rows still need current-payload evidence.

# Revision 88 build smoke — 2026-10-15

`./gradlew build --offline` succeeded on Java 8 and produced manifest revision 88 with SHA-256
`60d5b1c309fede6deb082b8e16cf2fd3bba285a812b717945db25e7f807f8972`. The exact file was copied
to the disposable full-stack Prism clone, and the installed and build archive hashes match.

A later required rebuild produced manifest revision 90 with SHA-256
`d73230a41b5cf0dec1d1059d67e577cb18c8da8c2e5b1d55e342f42bf2d280d2`; its Java 8 offline build
log is `build-current-rev90.log`. Comparing the revision-90 archive with the exact
installed revision-88 archive shows all 729 entries match except `litemod.json`'s revision field.
Thus the runtime interactions below exercised the same code/resources, though the revision-90
manifest itself was not restarted in-game.

Compared with revision 87, all 729 archive entries are byte-identical except `litemod.json`, whose
only change is the manifest revision value (`87` → `88`). Revision 87 already passed the B1
full-stack runtime, Tweakeroo Free Camera, main-thread F3+A reload, and 6,000-tick parity checks.
Revision 88 itself loaded with the full installed stack, joined a fresh vanilla 1.12.2 server, and
rendered its flat terrain.

The client moved from `(1218.500, 4.0, 503.500)` to `(1218.500, 4.0, 512.131)`; the vanilla
`stat.walkOneCm` scoreboard recorded 876 cm. A server-console teleport then moved the client to
`(1000.500, 4.0, 1000.500)`, with remote terrain visible. Vanilla logged a transient “moved too
quickly” warning for the stale pre-teleport movement packet, after which the client's F3 coordinates
showed the commanded destination. The client then placed stone at `(1012, 4, 1000)`; the server's
`testforblock` confirmed it. Breaking it was also confirmed server-side. Holding the break input
long enough to clear the stone continued into two grass blocks, so that destructive follow-through
is visible in the disposable world and screenshot. A server-summoned farmer's trade GUI opened and
accepted emeralds into its input, but its result slot stayed empty and the server
`stat.tradedWithVillager` score remained unset; the trade transaction therefore failed this check.
The client disconnected and the disposable server saved and shut down cleanly. Current-revision
potion, dimension travel, and reconnect interactions remain unchecked.

Evidence: `client-latest.log` records full-stack startup; `full-stack-startup.png` shows the title
screen; `client-vanilla-server.log` and `server-latest.log` record the join, movement, trade attempt,
server block queries, and clean disconnect; the `vanilla-server-*.png` captures show startup,
movement, server correction, and the post-break disposable-world state. The broader release gates
remain open in `PLAN.md`.

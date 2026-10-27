# Network probe — 2026-10-24

## Result: no packet optimization justified

LiteOptimizer 0.6.0-rc2 connected from the isolated MultiMC instance to a disposable dedicated
vanilla 1.12.2 server on loopback port 25566. The server jar SHA-256 was
`fe1f9274e6dad9191bf6e6e8e36ee6ebc737f373603df0946aafcded0d53167e`, matching the existing
vanilla fixture. The server accepted protocol 340 login, sent initial world/chunks, and the client
rendered the scene. The server log records repeated logins, teleport/movement commands, and clean
disconnects; active play also included chat delivery. The owner's server on port 25565 was not used.

A 60-second JFR was captured during active play. Its self-sample top was dominated by
`RawInput.lambda$getMouse$1()` (5,747 samples), followed by Sodium chunk graph iteration (25 samples)
and chunk rendering (20 samples). Netty/packet methods did not appear as sampled hotspots. This is
a bounded local profile, not proof that packet processing can never be expensive. Raw artifacts:
`network-active-play-vanilla-jdk8-2026-10-24.jfr` and
`network-active-play-vanilla-jdk8-2026-10-24.top.txt`.

No network source, packet IDs, serialization, Netty pipeline, or player-action timing code changed.
There is no changed packet representation to compare byte-for-byte. Vanilla login, world transfer,
chat, and movement were exercised without protocol changes.

Decision: do not port Krypton or add a network micro-optimization. Reopen only if a repeatable profile
puts packet processing on the critical path; any future candidate must preserve wire bytes and
player-action timing.

# Revision 90 vanilla-server potion smoke — 2026-10-15

The exact revision-90 LiteOptimizer artifact (SHA-256
`d73230a41b5cf0dec1d1059d67e577cb18c8da8c2e5b1d55e342f42bf2d280d2`) was loaded in the
full-stack disposable Prism clone and connected to a local unmodified vanilla 1.12.2 server.
The server put the player in Survival and gave a vanilla Swiftness potion. The player held use
until the client showed `Glass Bottle` and a Speed effect icon. The vanilla server's
`stat.useItem.minecraft.potion` objective returned score 1. This verifies ordinary server-given
potion use with the current code payload.

`server-events.txt` retains the server start, login, potion grant, and score-check log records.
The client item name and effect icon were observed live but not saved as a screenshot before the
isolated desktop ended, so the server-side statistic is the primary retained evidence. The server
was a disposable local test world; no owner world was used.

# Revision 92 vanilla-server inventory transfer

- Client artifact: revision 92, SHA-256 `3fa65abf14ad25a7850986e00e2c18bed7b252abc390bc2eb9ba9a172e5705e1` (same code payload as revision 91; only manifest revision differs).
- Server: local Mojang Minecraft 1.12.2 dedicated server, no mods; player was set to Survival by RCON for this check.
- In the Nether, moved a server-given glass bottle from the hotbar into the main inventory and then back to the hotbar. Screenshots: [before](inventory-before.png), [in main inventory](inventory-in-main.png), [returned to hotbar](inventory-returned-to-hotbar.png).
- RCON `testfor VanillaTester {Inventory:[{id:"minecraft:glass_bottle"}]}` returned `Found VanillaTester` after the transfers. RCON `save-all` returned `Saved the world`; the client disconnected cleanly and the server stopped with normal world/player/chunk saves.

This closes the vanilla-server inventory slot-transfer smoke for the current code payload. It does not close other vanilla-server action cases or the installed-mod feature matrix.

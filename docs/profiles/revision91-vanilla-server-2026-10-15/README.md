# Revision 91 vanilla-server interaction follow-up — 2026-10-15

The exact current artifact, SHA-256
`fb13cd36b04e3213faf2b9c7f0755bb4d9d879b235f256cb85523eac421799c9`, was installed
byte-for-byte in the disposable full-stack Prism clone and joined a fresh local Mojang 1.12.2
dedicated server with no server-side mod.

Current-artifact checks passed:

- Survival potion use: the server-side `stat.useItem.minecraft.potion` scoreboard reached 1;
  the potion was consumed and returned as a glass bottle. See [potion-use.png](potion-use.png).
- Natural portal travel: the vanilla server awarded `We Need to Go Deeper`; RCON then confirmed
  `testfor VanillaTester {Dimension:-1}`. The player was visibly in the Nether.
- Inventory screen opened and closed in the Nether, showing the player inventory and potion bottle;
  see [inventory-open-in-nether.png](inventory-open-in-nether.png). No slot transfer was attempted,
  so inventory movement remains unverified.

The server was a fresh disposable flat world under `run/vanilla-server-rev91`; the player used
Survival mode. These checks establish basic current-artifact compatibility with vanilla server
potion and portal behavior and inventory GUI presentation. They do not complete the installed-mod
matrix or prove every network action.

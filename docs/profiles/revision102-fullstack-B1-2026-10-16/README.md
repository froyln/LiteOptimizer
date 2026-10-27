# Revision 102 full-stack B1 load — 2026-10-16

The exact build artifact (SHA-256 in `artifact-sha256.txt`) was installed in the disposable full-stack Prism clone. It started with the installed technical mods/resource packs, initialized LiteOptimizer with its release-default options, reached the title screen, and loaded/rendered `B1-rev102-compat-smoke`, a fresh copy of `B1-storage-smoke`. The source save was not opened or changed.

This verifies exact revision-102 startup and B1 rendering only. It does not close the vanilla dedicated-server gate or the installed-mod feature matrix. `latest.log` records the full-stack startup; `b1-world.png` shows the rendered scene.

In the same disposable world, MiniHUD Configs opened with its `H,C` hotkey. On the Structures tab, `Structure Bounding Boxes` was toggled OFF and restored ON; after cleanly saving and quitting, the clone config retained `structureMainToggle: true`. The B1 scene contains no nearby matching generated structure, so this verifies the config UI and toggle only, not visible structure-box rendering. Captures: [before](minihud-structures-before.png), [OFF](minihud-structures-off.png), [restored](minihud-structures-restored.png).

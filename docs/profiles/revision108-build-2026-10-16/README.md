# Revision 108 build verification — 2026-10-16

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0 (`BUILD SUCCESSFUL` in 9 seconds; 11 actionable tasks). The artifact SHA-256 is recorded in `artifact-sha256.txt`.

The archive contains 728 entries. Compared with the exact revision-107 artifact installed in the disposable full-stack Prism clone, all 728 entry names match and the only differing entry is `litemod.json` (the automatic revision field). Revision 107's B1 full-stack startup, world-join, and Sodium chunk-upload render smoke therefore covers the same code/resources as this build; it does not cover this artifact's manifest identity or the vanilla-server gate.

Focused checks passed: audit regression checks (states, persistence, recipes, worker task/stop/count), Tweakeroo bridge presence/absence, chunk build revision filtering, Sodium option registration, profile checksum validation, and `git diff --check`.

This is build and payload-equivalence evidence only. Revision 108 has not been launched. Its exact-artifact vanilla-server interaction checks and the full installed-mod feature matrix remain open.

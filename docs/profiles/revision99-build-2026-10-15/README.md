# Revision 99 build

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` completed successfully in
7 seconds. The output is revision 99, SHA-256 recorded in `archive-compare.json`.

A byte-for-byte archive comparison with the exact revision-98 vanilla-server test artifact found
only `litemod.json` differs: the revision field changed from 98 to 99. All 728 other entries
(including classes, resources and mixin configuration) are identical. Revision 98 interaction
evidence therefore validates the same code payload, though the exact revision-99 archive was then launched with the full technical stack and joined the
unmodified vanilla server; server-side movement scored 876 cm after reset. See
`../revision99-vanilla-server-2026-10-15/`. Position correction and block actions on revision 99
remain separate release checks.

# Revision 110 build — 2026-10-16

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` completed successfully in 8 seconds. The produced `.litemod` SHA-256 is recorded in `artifact-sha256.txt`; the same archive was installed in the disposable full-stack Prism clone with an identical hash.

Archive comparison against the exact revision-108 test artifact found the same entries and byte-identical code/resources; only `litemod.json`'s automatic revision changed from 108 to 110. Revision-108 interaction evidence therefore covers the same executable payload, while `revision110-vanilla-server-2026-10-16/` records a fresh exact-revision-110 load, full-stack startup, vanilla-server join, and server-counted movement.

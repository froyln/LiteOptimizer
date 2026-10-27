# Revision 100 build verification

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` completed successfully in 10 seconds.
The built litemod has SHA-256 `af32613c211a13d7e89216809a6b2e0bfb360a193b7f967c4b777c2f9b5ee86f`.
The output manifest revision is 100. The same artifact was copied into the disposable full-stack
Prism clone and its installed hash matched the build output.

Comparing its zip entry contents to the prior revision-99 test artifact found identical entry
names and identical contents for all entries except `litemod.json`, whose revision changed from 99
to 100. The full-stack startup log records the release-default optimization set enabled and
LiteOptimizer initialized successfully. The client joined the unmodified vanilla 1.12.2 server.
See `../revision100-vanilla-server-2026-10-15/` for interaction evidence.

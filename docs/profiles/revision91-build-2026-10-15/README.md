# Revision 91 build — 2026-10-15

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` completed successfully
in 8 seconds. The release artifact is
`build/libs/liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod` with SHA-256
`fb13cd36b04e3213faf2b9c7f0755bb4d9d879b235f256cb85523eac421799c9`.

The revision-91 archive has 729 entries, exactly matching the tested revision-90 installed
archive except `litemod.json`'s revision field. The artifact was copied into the disposable
full-stack Prism clone for follow-up runtime checks; no source code changed after the previous
runtime-tested payload.

The revision-91 artifact (same payload except manifest revision) was then launched in the disposable
full-stack Prism clone. LiteLoader initialized LiteOptimizer with all nine validated defaults
active and the pending simulation options disabled; B1-storage-smoke loaded and rendered. The
captured `b1-rendered.png` records that current-artifact smoke. No B1 blocks were changed in this
check.
The current revision-91 B1 session also accepted Tweakeroo's F3+A chunk reload; the same storage
scene rendered after the reload (`b1-after-f3a.png`). The dirty-section block-edit/rebuild and
runtime OFF→ON→OFF setting transition remain open.

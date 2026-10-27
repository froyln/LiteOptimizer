# Shulker placement/removal render update fix — 2026-10-17

## Cause and change

A transparency resort used `cancelRebuildTask()` to create a newer task revision even when a
full section rebuild had not uploaded. The sort copied the previously uploaded render data,
including its old block-entity list. The upload revision filter then discarded the full rebuild.
This can omit a newly placed shulker or retain a removed one until another section rebuild.

Track the last applied render-data revision. Reject new sort requests while a rebuild/sort is
awaiting upload, and recheck at sort submission to catch stale queue entries and preserve newer
pending rebuild requests. Normal sorting resumes after `setData()` applies the latest result.
No new mixin, option or server/world behavior changes.

## Focused proof

`python3 tools/check-chunk-build-revisions.py` failed before the fix with
`camera sort accepted while block-entity rebuild awaits upload`, then passed after the fix.
The harness extracts real container/scheduling/filter method bodies. It covers pending uploads,
stale sort-queue entries, accepted current rebuilds, rejected stale builds and sorting resumption.
Its synthetic block-entity data is not a real Minecraft rendering test.

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0 in 18 seconds in the
working checkout. A separate public-checkout build also exited 0. Archive entry comparison
confirms equal runtime code/resources; only automatic litemod revision metadata differs.
The exact installed/tested SHA-256 is retained in `artifact-sha256.txt`.

## Packaged-mod gameplay smoke

The new artifact ran with the technical LiteLoader mod stack in a separate disposable Prism
root and a copy of the stable `test` save. Sodium, tile-entity frustum and translucency sorting
were enabled; Tweakeroo chunk builds on main thread was false (asynchronous worker path).
The fixture used a stone platform at Y3 and purple shulkers at (0,4,-2), with stained glass
at Z-1 in the same chunk section (and an earlier glass wall at Z3).

Normal Use placed a visible purple shulker; its inventory opened. Attack removed the model
without another block update (`shulker-placed.png`, `shulker-broken.png`). A second shulker stayed
visible after backward camera movement beyond the one-block resort threshold; it then disappeared
when attacked while moving backward (`shulker-moving-placed.png`,
`shulker-before-moving-break.png`, `shulker-moving-broken.png`). Small purple fragments immediately
after breaking are vanilla particles. No F3+A or extra block edit was used to repair visibility.
The inherited Use binding was F12; initial right-click attempts performed no Use action. Early
fixture commands affected unloaded chunks or lost leading text through short VNC key events;
those attempts are not counted as checks. Persistent VNC input and verified command responses
were used for the successful checks. The disposable world was saved and Minecraft quit cleanly;
the isolated desktop was stopped and its temporary account copy removed.

The original intermittent symptom was not reproduced visually on the old build. Deterministic
scheduling failure plus patched gameplay smoke establish a matching cause and focused fix;
this is not proof against every possible shulker rendering defect. No remote-server or full
compatibility rerun, performance claim, or gameplay-state parity rerun is made for this client
scheduler correction. Original worlds and instance settings were not modified.

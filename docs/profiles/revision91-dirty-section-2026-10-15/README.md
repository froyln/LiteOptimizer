# Revision 91 dirty-section and Tweakeroo toggle smoke — 2026-10-15

The exact revision-91 artifact (`fb13cd36b04e3213faf2b9c7f0755bb4d9d879b235f256cb85523eac421799c9`) ran in the full-stack Prism clone, in the disposable `B1-storage-smoke` copy. The clone's vanilla data and the owner's original world were not used.

A glowstone marker was placed into the previously empty block at `(-1968, 38, 700)` with Tweakeroo's `Chunk Render On Main Thread` OFF. The client displayed the marker, and vanilla `/testforblock` confirmed the block at that position. The marker remained visible after F3+A chunk reload and subsequent rendering; captures are `marker-placed.png` and `marker-visible-and-block-query.png`. This exercises a real block update and resulting section rebuild on the normal asynchronous path.

In the same live session, Tweakeroo's main-thread setting was changed OFF → ON → OFF through its config UI. F3+A was run once while ON and once after returning to OFF. Both reloads returned to the same rendered B1 scene (`f3a-live-on.png`, `f3a-live-off.png`), with no crash or logged exception. Config was left at its original `tweakChunkRenderOnMainThread=false`; the accidentally changed `tweakChatPersistentText` setting was restored to `true`.

This is runtime compatibility evidence, not a gain benchmark. It does not isolate a single-block edit while the main-thread setting is ON. The marker remains only in the disposable clone's B1 copy. The test's LAN cheats were enabled only for that local copy; no vanilla dedicated server or player-action behavior was involved.

The exact artifact is the revision-91 build documented in [revision91-build](../revision91-build-2026-10-15/README.md). Client-side event excerpt: [client-events.txt](client-events.txt).

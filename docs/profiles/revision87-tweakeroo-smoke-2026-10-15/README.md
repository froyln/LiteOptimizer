# Revision 87 Tweakeroo Free Camera smoke — 2026-10-15

Revision 86 exposed a Sodium compatibility bug: Tweakeroo marks its Free Camera render pass as
spectator so the visibility graph can traverse opaque camera sections, but LiteOptimizer's
replacement culler received only vanilla's spectator argument. Revision 87 adds an optional
Mixin bridge for that signal and passes it into Sodium's culler.

## Artifact and setup

- Artifact: `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, manifest revision 87.
- SHA-256: `d726a3465ba3049a8cd2045ed5b3eb1fa0ff0d8f9c1b4af5d59dbbb78aa75126`.
- The archive staged in Prism clone `1.12.2 test ai parity rev44` has the same hash.
- The full installed technical-mod stack loaded; LiteOptimizer reported `render/sodium: enabled`,
  and the disposable `B1-storage-smoke` world joined. No LiteOptimizer mixin-application error
  appeared in the client log.

## Result

Tweakeroo's `Ctrl+Alt+F` Free Camera toggle showed ON. Holding W for 0.5 seconds moved the render
view from the saved player location around `(-1969.5, 36.9, 700.5)` to `(-1971.875, 36.09864,
705.687)`, with the B1 structure visible. The player remained at X/Z `(-1969.5, 700.5)`. Toggling
Free Camera OFF showed the OFF toast and returned the view to the player at X/Z `(-1969.5,
700.5)` without a world rejoin. No blocks were changed.

The camera starts at the player's location, so its first active frame can show the player model
close to the camera. The short W movement clears that overlap; this is Tweakeroo's expected
co-located start, not a missing-terrain result. Free Camera movement/return passes on revision 87.

For the main-thread path, the disposable clone's Tweakeroo config set `tweakChunkRenderOnMainThread`
to `true` before launching the exact revision-87 artifact. B1 loaded and rendered, F3+A was sent,
and the debug overlay closed with the scene still rendered. The game stayed responsive and exited
through Save and Quit. This is a chunk reload smoke; it does not prove a dirty-section rebuild or a
live OFF→ON→OFF toggle. The config was restored to its original OFF value after the test.

## Evidence

- `world-before.png`: full-stack B1 view before the test.
- `freecam-moved.png`: Free Camera ON, F3 coordinates after movement, rendered structure visible.
- `freecam-off-return.png`: OFF toast and restored player X/Z.
- `chunk-reload-main-thread.png`: rendered B1 scene after F3+A with the setting enabled at startup.
- `client-chunk-reload.log`: exact revision 87 launch, world load, save and quit.
- `client-latest.log`: exact revision 87 client launch and B1 world join.

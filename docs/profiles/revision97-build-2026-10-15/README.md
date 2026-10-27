# Revision 97 build and runtime smoke — 2026-10-15

`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` completed successfully in 6 seconds. The artifact is `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`, SHA-256 `4a80a24fc6beb166872e30dcee58d982d8c6757c877220ad8d7e3fd639f71882`, with manifest revision `97`. The same archive hash was copied to the disposable full-stack Prism clone.

An entry-by-entry comparison against its revision-96 clone artifact found exactly one changed archive entry: `litemod.json` (revision metadata); all other entries matched. The revision-97 clone log records LiteLoader selecting revision 97 and LiteOptimizer initializing with the expected release-default-disabled keys. It reached the title screen and then joined the local vanilla server; see `fullstack-startup.log` and the dedicated [vanilla-server follow-up](../revision97-vanilla-server-2026-10-15/README.md).

This verifies the current source build and revision-97 artifact startup. It does not close the full installed-mod feature matrix or replace the optimization-specific evidence in `docs/catalog.md`.

## Model Gap Fix baseline result

The full-stack revision-97 log records `Failed to select mixin config: mixins.modelgapfix.json`
with an NPE in Mixin 0.7.5 `MixinConfig.onSelect`. I temporarily removed only the LiteOptimizer
archive from the disposable clone and reproduced the same Model Gap Fix warning in
`modelgapfix-no-liteoptimizer-baseline.log`, with no LiteOptimizer initialization in that run.
The exact revision-97 archive was restored and launched again afterward. This is a pre-existing
Model Gap Fix/Mixin configuration failure, not evidence that its visual fix works; its focused
render check remains open.

## Litematica B1 interaction

On the exact revision-97 artifact, Litematica's `M` menu opened in the disposable B1 copy. Its
schematic browser found and previewed `6gt Crafting Station` (93 blocks, 6×5×5); loading it
created a placement and rendered the translucent placement overlay in the scene. I removed both
transient placement entries after the visual check, confirmed the overlay disappeared, then used
Save and Quit. No world blocks were changed. `litematica-placement-overlay.png` and
`litematica-client.log` record the full-stack scene and clean save/quit. This covers browser,
schematic load, placement creation and overlay rendering.

In the same revision-97 B1 session, I opened Litematica's Material List for the loaded schematic.
It displayed 91 distinct material items (100% missing at the current unaligned placement), and
the visible list populated with the schematic's block types. Screenshot:
[`litematica-material-list.png`](litematica-material-list.png). I removed the temporary
placement, confirmed the overlay disappeared, and used Save and Quit; the client log records
world/chunk save and integrated-server shutdown in `litematica-material-list-client.log`.
No blocks were placed or modified. Litematica's
layers, verifier, paste and remaining browser features are still open.

## MiniHUD B1 light-level overlay

In the same exact revision-97 full-stack B1 copy, MiniHUD's Renderers config enabled Light Level
Overlay. The floor visibly displayed green block-light numbers. I captured the rendered scene,
turned the option back OFF, verified `lightLevel=false` in the clone config, then saved and quit.
No blocks were changed. Captures: [`minihud-light-level-on.png`](minihud-light-level-on.png)
and [`minihud-light-level-config-on.png`](minihud-light-level-config-on.png); the final session log
is `minihud-light-level-client.log`.

## ChatCalc

In the same full-stack B1 copy, I opened chat, typed `2+2=`, and pressed Tab. ChatCalc changed the
input to `2+2=4` as expected. I closed the chat without sending the text and saved/quit the world;
the result is visible in [`chatcalc-result.png`](chatcalc-result.png), with the session log in
`chatcalc-client.log`.

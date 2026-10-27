# Vanilla 1.12.2 server smoke — 2026-10-14

This is partial interaction evidence for the release gate, not a release certification.

## Setup

- Dedicated Mojang Minecraft 1.12.2 server, no mods or plugins, bound to `127.0.0.1:25567`.
- `online-mode=false` for the offline test account; creative mode and a disposable flat world.
- Prism instance `1.12.2 test ai`, with its full installed technical mod stack.
- Candidate tested for server interactions: `liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`,
  revision 20, SHA-256 `2b61f1824f46b4ebc865679ecbd6fad404100422e61ca7ec7b80781321c9eee5`.
- Current rebuilt candidate: revision 21, SHA-256
  `161e419ca8ddc143371f0dd5182144ed4ef1c146dd5ae867ea8e9b48fa6d1d86`. The only difference
  between the tested and rebuilt archives is `litemod.json`'s revision field; all other packaged
  entries match byte-for-byte. Revision 21 was installed and loaded with the full mod stack; see
  [`rebuilt-candidate-startup.log`](rebuilt-candidate-startup.log).
- Revision 22 was built and installed after the smoke below, SHA-256
  `0b92be88329fc2abc7be9c8579a8af23ffe58e734de2f02da871ebf0f7e4e616`. Its 726 archive entries
  match revision 21 except for the `litemod.json` revision number. It loaded with the full stack
  and joined the disposable B1 copy; see
  [`revision22-full-stack-b1.log`](revision22-full-stack-b1.log) and
  [`revision22-b1-join.png`](revision22-b1-join.png).
- Server jar: SHA-256
  `fe1f9274e6dad9191bf6e6e8e36ee6ebc737f373603df0946aafcded0d53167e`.

## Results

With LiteOptimizer loaded, `TestBot` joined and reconnected after a server restart, moved across
chunk boundaries, followed a server teleport correction, received inventory items, placed a
stone block, and broke it. The server confirmed the block existed after placement and was air
after breaking. The client stayed connected without a crash. These observations satisfy only
those individual smoke items.

Overworld-to-Nether travel is verified: after the frame-and-fire fixture failed to activate,
a portal block was placed in the disposable world and the player was moved into it. The vanilla
server awarded `We Need to Go Deeper`; client and server logs are in
[`rebuilt-candidate-vanilla.log`](rebuilt-candidate-vanilla.log) and
[`vanilla-dimension-travel.log`](vanilla-dimension-travel.log). This verifies the dimension-change
network path, while natural portal-frame activation remains untested.

Natural portal activation and travel were then verified on revision 22 against a fresh, unmodified
Mojang 1.12.2 dedicated server (jar SHA-256 matches the server hash above). In a new flat
`portal-world`, RCON built a standard 4×5 obsidian frame and placed Flint and Steel in hotbar slot
0; the client used the item on the frame through the isolated VNC session. RCON confirmed portal
blocks at `(1,5,0)`, `(2,6,0)`, and `(1,7,0)`. Entering the activated portal awarded
`We Need to Go Deeper` on the vanilla server. The server log records the advancement and the
revision 22 client screenshot shows the Nether arrival and toast:
[`revision22-natural-portal-activated.png`](revision22-natural-portal-activated.png),
[`revision22-natural-portal-nether.png`](revision22-natural-portal-nether.png), and
[`revision22-natural-portal-server.log`](revision22-natural-portal-server.log). The built and
installed client artifacts both had SHA-256
`0b92be88329fc2abc7be9c8579a8af23ffe58e734de2f02da871ebf0f7e4e616`.

Entity interaction passed on the rebuilt revision 21 client against a fresh vanilla server on
port 25568: after joining, the client right-clicked a server-spawned villager and visibly opened
the trading GUI. The server log records the connection, teleport, and villager spawn; the captured
GUI is [`entity-trade-screen.png`](entity-trade-screen.png), and the log is
[`entity-trade-server.log`](entity-trade-server.log). An earlier summon targeted an unloaded
location and did not contribute to this result; the nearby summon and trade GUI are the evidence.

## Baseline comparison

To diagnose the MaLiLib `ConfigOverrideUtils.tryApplyOverridesFromServer` NPE, the same instance
and server were run with the LiteOptimizer `.litemod` temporarily removed. The no-mod client
joined and reproduced the same NPE at `ConfigOverrideUtils.java:149`; this exception is a baseline
MaLiLib issue and is not evidence against LiteOptimizer. The exact no-mod client log is
[`no-liteoptimizer-baseline.log`](no-liteoptimizer-baseline.log). The server log from that run is
[`server.log`](server.log).

The startup mixin-conflict messages were also compared with and without LiteOptimizer. The same
11 warning lines (7 distinct conflicts after repeated messages) appear in each log; no new
conflict warning was introduced by the mod. The latest full-stack B1 join log is
[`rebuilt-candidate-b1.log`](rebuilt-candidate-b1.log).

The fresh revision 22 launch again reached the title screen and joined the disposable B1 copy.
The vanilla server interaction checks used revision 21; the archive comparison above shows the
same packaged code and resources in revision 22, with only its manifest revision counter changed.
On revision 22, Options → Video Settings → LiteOptimizer Options opened, all four settings tabs
rendered, and Done returned to Video Settings and the parent Options menu. No settings were
changed. Captures: [`revision22-video-settings.png`](revision22-video-settings.png),
[`revision22-sodium-options.png`](revision22-sodium-options.png), and the adjacent Quality,
Advanced, and Performance screenshots.
MiniHUD Info Lines was visibly present in the disposable B1 copy, toggled off with its `H` hotkey,
then toggled back on. The off/on captures are
[`revision22-minihud-off.png`](revision22-minihud-off.png) and
[`revision22-minihud-on.png`](revision22-minihud-on.png); no world blocks were changed.
The MiniHUD `H,C` config shortcut did not open after two VNC input sequences. A later revision 23
smoke enabled the light-level toggle through its config file and visibly confirmed the overlay;
see the revision 23 evidence below. The config was restored afterward.

The Carpet Client main menu and Carpet Client Options page also open on revision 22 in the B1
singleplayer copy. The page reports server version `null`, as expected without a Carpet server;
this confirms menu compatibility only and does not revalidate server rule synchronization or
server-dependent hooks. Capture: [`revision22-carpet-client-options.png`](revision22-carpet-client-options.png).

Revision 22 was installed during the vanilla interaction smoke; its SHA-256 matched the build
output at that time. The latest Java 8 offline build succeeded and produced revision 23, SHA-256
`94eb0764551a680937251133600c7c98993aef0096cfe87bb0725223a7a824a3`. All 726 archive entries
match revision 22 except `litemod.json`'s revision number. Revision 23 was installed with an
identical hash, then freshly launched with the full technical mod stack and joined the disposable
B1 copy. MiniHUD's light-level toggle was enabled in its config before launch; green light-level
numbers were visibly rendered over the test floor. The capture is
[`revision23-minihud-lightlevel.png`](revision23-minihud-lightlevel.png), and the exact session log
is [`revision23-full-stack-b1.log`](revision23-full-stack-b1.log). The temporary MiniHUD config
change was restored after a clean game shutdown. This is a focused overlay smoke, not a pass of
MiniHUD's other features or the complete compatibility table.

The exact revision 23 LiteOptimizer Options UI also opened from Video Settings. General, Quality,
Advanced, and Performance tabs rendered. Apply followed by Done returned to Video Settings without
changing a setting; `liteoptimizer-sodium.json` kept SHA-256
`0feda1f6b39d09d2c32c91690e65fac0bb66ade591807a734a24be345bd1a1cd`. The Advanced tab exposes no
GL43, compact-vertex, or block-face-culling controls. Captures:
[`revision23-options-general.png`](revision23-options-general.png),
[`revision23-options-quality.png`](revision23-options-quality.png),
[`revision23-options-advanced.png`](revision23-options-advanced.png), and
[`revision23-options-performance.png`](revision23-options-performance.png). The session log is
[`revision23-options-smoke.log`](revision23-options-smoke.log).

The seven distinct startup mixin-conflict warnings on the revision 23 full-stack client match
the no-LiteOptimizer baseline: no warnings were added or removed. The normalized comparison and
log hashes are in [`revision23-mixin-warning-comparison.json`](revision23-mixin-warning-comparison.json).

The Java 8 offline build produced revision 24 (SHA-256
`03ddc07cb65c2442b80a1ffea04bced8b5e20ccd47092953cc1943468f425800`). The exact artifact was
installed, loaded LiteOptimizer with no disabled options, and joined the disposable B1 world.
Its seven distinct foreign mixin-conflict warnings still match the no-mod baseline. Evidence:
[`revision24-full-stack-b1.log`](revision24-full-stack-b1.log) and
[`revision24-mixin-warning-comparison.json`](revision24-mixin-warning-comparison.json).

ChatCalc was present and initialized on the second fresh revision 23 launch. In the disposable B1
world the chat accepted `2+2=`, but sending Tab through WayVNC did not append a result. Since the
virtual Tab action was not independently verified at the game input boundary, this remains an
inconclusive compatibility check rather than a confirmed mod failure. Session log:
[`revision23-chatcalc-attempt.log`](revision23-chatcalc-attempt.log).

On the next revision 23 B1 launch, Litematica loaded `6gt_Crafting_Station_1.12.litematic`,
showed its 150-volume/93-block preview, and created a placement. Its bounds were visible in-world;
the transient placement was removed from the disposable copy before shutdown. This confirms
current-artifact load and placement creation, while translucent/ghost block rendering and layer
switching remain unverified. The session log is
[`revision23-litematica-current.log`](revision23-litematica-current.log).

The revision 24 artifact (`03ddc07cb65c2442b80a1ffea04bced8b5e20ccd47092953cc1943468f425800`)
was first launched with the full technical client stack against a clean copy of the unmodified Mojang
1.12.2 server on `127.0.0.1:25569` (server jar hash above). The first launch exposed that
`tools/test-in-game.sh` used a 19-character offline username; vanilla rejects names over 16
characters. The helper now uses `LiteOptTest`. The next build produced revision 25, SHA-256
`4bac2055096578bfce40405278b8dc1c2f40321e72b494e76c52c3288ec546c7`; the helper installed this
artifact, LiteOptimizer loaded with no disabled options, and the server recorded `LiteOptTest`
joining. The client rendered the flat overworld, visible villagers, and a portal. Exact-run logs:
[`revision25-vanilla-client.log`](revision25-vanilla-client.log)
and [`revision25-vanilla-server.log`](revision25-vanilla-server.log).

On the same revision 25 connection, a two-second forward input moved the server-reported player
position from `(206.5, 4.0, -284.5)` to `(206.5, 4.0, -281.3)`. For block placement, the server
confirmed `(206,4,-281)` was air, a client right-click placed stone there, and the server then
confirmed stone. For breaking, a client left-click removed a server-placed stone at `(206,4,-281)`;
the server confirmed air. Captures of placement and breaking are
[`revision25-place-before.png`](revision25-place-before.png),
[`revision25-place-after.png`](revision25-place-after.png),
[`revision25-block-break-before.png`](revision25-block-break-before.png), and
[`revision25-block-break-after.png`](revision25-block-break-after.png). The disposable server
transcript with the coordinate checks is
[`revision25-interactions-server.log`](revision25-interactions-server.log).

The full-stack client joined and rendered the vanilla overworld, moved, placed and broke a block,
and disconnected/reconnected successfully on the same revision 25 client. The reconnect is in
[`revision25-interactions-server.log`](revision25-interactions-server.log) and the captured view is
[`revision25-reconnect.png`](revision25-reconnect.png). In a fresh run of the same exact revision
25 client against a disposable copy of the unmodified vanilla server world, right-clicking a
server-spawned villager opened the trade GUI; capture:
[`revision25-vanilla-trade.png`](revision25-vanilla-trade.png). For dimension travel, the vanilla
server placed portal blocks inside an obsidian frame and the player entered them. The server
awarded `We Need to Go Deeper` and the client received the advancement, confirming travel to the
Nether. This test verifies the dimension-change network path; natural frame ignition on revision
25 remains untested. See [`revision25-vanilla-final-server.log`](revision25-vanilla-final-server.log),
[`revision25-vanilla-final-client.log`](revision25-vanilla-final-client.log), and
[`revision25-dimension-transition.png`](revision25-dimension-transition.png). Historical revision
21/22 natural portal activation passes remain supporting evidence only. The earlier failed-name
connection evidence is in
[`revision24-vanilla-client-attempt.log`](revision24-vanilla-client-attempt.log) and
[`revision24-vanilla-server-attempt.log`](revision24-vanilla-server-attempt.log). These results
still do not close the vanilla-server release gate.

The final Java 8 offline build produced revision 26, SHA-256
`d4a4f11b29b90f8b4a0b845e72130fcb84dd74efc0a343238851276d7d1b82b5`. The installed and
runtime-tested revision 25 artifact has SHA-256
`4bac2055096578bfce40405278b8dc1c2f40321e72b494e76c52c3288ec546c7`; archive comparison found
only `litemod.json`'s revision field differs. Details:
[`revision26-payload-compare.json`](revision26-payload-compare.json).

The latest Java 8 offline build produced revision 27, SHA-256
`e6db069a72c759f1c9d77abecda49e62d17fb50bfa9558dc197dc6091215fe9e`. Compared with the
runtime-tested revision 25 artifact, the only changed archive entry is `litemod.json`'s revision
field; see [`revision27-payload-compare.json`](revision27-payload-compare.json). The focused
settings and audit regression checks, shell syntax check, and `git diff --check` passed. The game
was not relaunched on revision 27 because its packaged code/resources are byte-identical to
revision 25.

The revision 25 artifact was then smoke-tested in the disposable B1 singleplayer copy with the
full instance stack. Its installed SHA-256 was still
`4bac2055096578bfce40405278b8dc1c2f40321e72b494e76c52c3288ec546c7`. LiteOptimizer Options
opened from Video Settings; Litematica Configs opened with its configured `M,C` hotkey;
Tweakeroo Configs opened with `X,C`; MiniHUD Configs opened with `H,C`. MiniHUD Info Lines HUD
was toggled off and back on with `H`. Captures are
[`revision25-options-general.png`](revision25-options-general.png),
[`revision25-litematica-config.png`](revision25-litematica-config.png),
[`revision25-tweakeroo-config.png`](revision25-tweakeroo-config.png),
[`revision25-minihud-config.png`](revision25-minihud-config.png),
[`revision25-minihud-off.png`](revision25-minihud-off.png), and
[`revision25-minihud-on.png`](revision25-minihud-on.png). The session log is
[`revision25-b1-compat.log`](revision25-b1-compat.log). No LiteOptimizer or mod settings were
changed and no blocks were modified in the original B1 save. This is a focused GUI/HUD smoke;
schematic layer/translucent rendering, resource reload, and the remaining installed-mod feature
checks remain open. The Resource Packs screen was opened and closed with no selection changes,
and the B1 world rendered normally afterward; captures are
[`revision25-resource-reload-options.png`](revision25-resource-reload-options.png) and
[`revision25-resource-reload-world.png`](revision25-resource-reload-world.png). This did not
force a reload because the selection was unchanged. The F3+T attempt hit the existing `vncdo`
`ord()` parser error before the chord was sent, so an explicit resource reload remains untested.
The captured session log is [`revision25-resource-reload.log`](revision25-resource-reload.log).

The latest build, revision 28, differs from the tested revision 25 only in the manifest revision
field ([archive comparison](revision28-payload-compare.json)). The checks below used the installed
revision 25 code/resources against a fresh unmodified vanilla 1.12.2 server;
the server console teleported `LiteOptTest` first to `(0.5, 4.0, 0.5)`, then to
`(260.5, 4.0, 0.5)`. The second destination crosses 16 chunk columns from the first. The client
rendered the flat world after both transitions; after server shutdown, the saved player NBT `Pos`
was `(260.5, 4.0, 0.5)`, confirming the client accepted the final server-authoritative position.
Server log: [`revision27-server-correction-chunk.log`](revision27-server-correction-chunk.log).
Client log: [`revision27-client-correction-chunk.log`](revision27-client-correction-chunk.log).
Captures: [`before correction`](revision27-server-correction-before.png),
[`after correction`](revision27-server-correction-after.png),
[`after chunk streaming`](revision27-chunk-stream.png). This checks server correction and a
long-distance chunk transition; the remaining feature matrix is open.

On the same client payload and vanilla server, a 4×5 obsidian frame was built in the disposable
world and vanilla fire was placed inside it. `testforblock` then confirmed portal blocks at both
ends of the interior. Contact with the active portal awarded `We Need to Go Deeper`; the saved
player NBT records Dimension `-1` and Nether position `(22.5, 20.0, 6.5)`. The portal contact was
initiated by a server teleport into the portal volume, after which vanilla handled the dimension
transition. Server/client logs, final player data, and captures:
[`server`](revision27-natural-portal-server.log),
[`client`](revision27-natural-portal-client.log),
[`saved player data`](revision27-natural-portal-playerdata.txt),
[`active portal`](revision27-natural-portal-before.png),
[`portal transition`](revision27-natural-portal-travel.png).

Inventory use passed on the same runtime-tested code/resources: the vanilla server switched the
player to Survival and supplied a Swiftness potion in hotbar slot 0. The client consumed it, showed
the Speed effect with 2:22 remaining and returned the glass bottle; the saved player NBT contains
active effect id 1 (`minecraft:speed`, amplifier 0). Evidence: [server log](revision28-potion-server.log),
[client log](revision28-potion-client.log), [saved effect data](revision28-potion-playerdata.txt),
[used item](revision28-potion-used.png), and [inventory/effect screen](revision28-potion-inventory.png).

Resource reload passed on the same runtime-tested payload. In Options → Resource Packs, the
available Soartex Fanver pack was temporarily selected and applied, then deselected and applied;
the client completed both reloads and recreated its texture atlases. The selected list after
restoration matches the original four packs. The log includes missing-texture warnings during both
reloads, so this confirms reload completion and restored selection, not perfect third-party pack
assets. Captures and log: [original selection](revision28-resource-packs-before.png),
[restored selection](revision28-resource-packs-restored-selection.png),
[reload log](revision28-resource-reload-client.log). The F3+T shortcut itself remains untested.

World shutdown was compared with and without LiteOptimizer after one earlier LAN-hosted run had
reported an entity-movement packet NPE during exit. The original failing log was overwritten before
it could be archived, so its trigger is unknown. Fresh no-mod and LiteOptimizer runs each exited
cleanly both without LAN hosting and after opening a LAN world and waiting before Save & Quit; none
of the three archived control logs contains a shutdown `FATAL` or `Error executing task`. Both LAN
logs contain the same startup `mixins.modelgapfix.json` selection NPE, also present in the no-mod
baseline; it is unrelated to shutdown and is not attributed to LiteOptimizer. This is a
non-reproduction, not proof that the original trigger is resolved. Logs:
[`no-mod LAN exit`](revision28-exit-baseline-lan-client.log),
[`LiteOptimizer LAN exit`](revision28-exit-optimizer-lan-client.log),
[`no-mod exit without LAN`](revision28-exit-baseline-client.log).

Revision 30 was built with Java 8 and installed by `tools/test-in-game.sh`. Its SHA-256 matched
the build output exactly (`0ff571658524157b2f1ce128a34769a668245fb1d255b848a38c0e38a0fdefe2`);
the exact archive loaded with the full instance stack, reported no disabled LiteOptimizer options,
reached the title screen, and exited with code 0. The log and identity record are
[`revision30-full-stack-title-exit-client.log`](revision30-full-stack-title-exit-client.log) and
[`revision30-payload-compare.json`](revision30-payload-compare.json); the Prism exit status is in
[`revision30-title-exit.png`](revision30-title-exit.png). This is startup/shutdown smoke only;
revision 25 remains the runtime-tested payload for the vanilla-server interactions above, and the
remaining compatibility checklist is open.

After that check, a Java 8 offline rebuild was packaged by LiteLoader as revision 31. The build and
installed archives match at SHA-256
`9f1721dad769c00796c473473e42b33ff53c6111a848191b4ccef4be5af83c24`; this exact archive loaded
with the full instance stack and logged `disabled: none`. Startup evidence:
[`revision31-full-stack-startup-client.log`](revision31-full-stack-startup-client.log) and
[`revision31-title.png`](revision31-title.png). It then joined a fresh disposable unmodified vanilla
1.12.2 server made from Mojang's jar (SHA-256
`fe1f9274e6dad9191bf6e6e8e36ee6ebc737f373603df0946aafcded0d53167e`). The server log records the
initial join, successful reconnect after a server restart, movement from Z=341.5 to Z=345.3, and
server teleports from X=0.5 to X=260.5 across 16 chunk columns; the client rendered the destination.
See [`revision31-vanilla-server.log`](revision31-vanilla-server.log),
[`revision31-vanilla-client.log`](revision31-vanilla-client.log),
[`revision31-vanilla-server-joined.png`](revision31-vanilla-server-joined.png), and
[`revision31-corrected-chunk-stream.png`](revision31-corrected-chunk-stream.png).

On the same session, the client visually placed and broke a server-authoritative stone block. The
vanilla server activated a standard obsidian frame after fire was placed inside it; portal contact
awarded `We Need to Go Deeper`. After `save-all flush`, player NBT recorded `Dimension: -1` and
`enteredNetherPosition`, confirming the Nether transition. The client reconnected at the saved
Nether location and rendered the Nether ceiling/floor at Y=128. Evidence:
[`revision31-playerdata.txt`](revision31-playerdata.txt),
[`revision31-nether-render.png`](revision31-nether-render.png), and the logs above. This completes
the current artifact's direct vanilla-server join, reconnect, movement, correction/chunk-streaming,
block interaction, and dimension-transition checks. The full installed-mod feature checklist
remains open; historical revision 25/28 feature checks are still not treated as proof for revision
31.

The subsequent Java 8 offline build produced revision 32, SHA-256
`f66fb6ef025a5bfc42667ba0504e128c83f7883a582503273e4607bc4aca4f8e`, and the same archive
hash was installed in the Prism instance. ZIP comparison confirms all 726 entries match revision
31 except `litemod.json`'s revision number. A fresh full-stack startup log records every enabled
optimization and `LiteOptimizer ... loaded; disabled: none`:
[`revision32-full-stack-startup-client.log`](revision32-full-stack-startup-client.log) and
[`revision32-payload-compare.json`](revision32-payload-compare.json). This validates startup on the
current payload. Revision 31 interaction results carry forward because its code and resources are
byte-identical; the full installed-mod feature table remains open.

Revision 32 also launched the exact installed archive into the disposable
`B1-revision31-render-check-2026-10-14` world. LiteOptimizer loaded with `disabled: none`; the B1
scene rendered with the full mod/resource-pack stack. The log and capture are
[`revision32-full-stack-b1.log`](revision32-full-stack-b1.log) and
[`revision32-b1-render.png`](revision32-b1-render.png). Tweakeroo, Litematica, and MiniHUD config
screens each opened in that session without a crash; screenshots are
[`Tweakeroo`](revision32-tweakeroo-config.png), [`Litematica`](revision32-litematica-config.png),
and [`MiniHUD`](revision32-minihud-config.png). No config settings were deliberately changed and
no blocks were placed or broken.
These are menu/render checks only; the remaining feature table is still open.

The same revision 32 archive also joined a fresh run of the unmodified Mojang 1.12.2 server
(jar SHA-256 above) at `localhost:25565`. The server logged `LiteOptTest` joining at
`(261.5, 128.0, 0.5)` and the client rendered the destination. Exact current-build evidence:
[`revision32-vanilla-client.log`](revision32-vanilla-client.log),
[`revision32-vanilla-server.log`](revision32-vanilla-server.log), and
[`revision32-vanilla-server-render.png`](revision32-vanilla-server-render.png). This is a current
artifact join smoke; the movement, block, and dimension interaction checks remain the revision 31
results carried forward by the byte-identical code/resources comparison above. The dedicated
server was stopped through RCON, and the full client session then exited with code 0; see
[`revision32-vanilla-title-exit.png`](revision32-vanilla-title-exit.png).

Revision 32's B1 follow-up exercised installed-mod render paths on the same exact archive. In
Litematica, `6gt_Crafting_Station` loaded, created a placement, rendered its overlay, and produced
a 91-item material list; the placement was removed and the schematic unloaded without changing
world blocks. MiniHUD Info Lines was enabled temporarily, visibly rendered, then restored to OFF.
Tweakeroo's `Chunk Render On Main Thread` was enabled for an F3+A chunk reload, then restored to
OFF. The game log records the reload and a clean save-and-quit to the title screen. Captures and
the complete follow-up client log are [`Litematica overlay`](revision32-b1-litematica-overlay.png),
[`material list`](revision32-litematica-material-list.png),
[`MiniHUD HUD`](revision32-minihud-info-on.png),
[`Tweakeroo setting`](revision32-tweakeroo-main-thread-enabled.png),
[`chunk reload`](revision32-tweakeroo-chunk-reload.png),
[`clean exit`](revision32-b1-clean-exit.png), and
[`client log`](revision32-full-stack-b1-followup-client.log). The vanilla-server interaction gate
and full installed-mod feature matrix remain open.

The Java 8 offline rebuild produced revision 33, SHA-256
`66e5f1d20f6477ff96fbf719f989a148bcee8698311498ceb3b7ebd9facc9004`; the installed archive has
the same hash. All 726 entry contents match revision 32 except `litemod.json`'s revision field
([payload comparison](revision32-to-33-payload-compare.json)). Revision 33 loaded with the full
instance stack and `disabled: none`, rendered the disposable B1 save, then joined a fresh
unmodified Mojang 1.12.2 server at `localhost:25565`; the server log records `LiteOptTest` joining
at `(406.5, 4.0, -501.5)`. The client disconnected and exited with code 0, and the server
stopped cleanly. Evidence: [startup/client log](revision33-full-stack-client.log),
[B1 render](revision33-b1-render.png), [server join](revision33-vanilla-server-joined.png),
[vanilla server log](revision33-vanilla-server.log), and [title exit](revision33-title-exit.png).
The client log also records the known MaLiLib config-override NPE on remote world entry; it is
outside LiteOptimizer and matches the previously observed no-mod baseline. F3+T did not produce a
resource reload in this input session, so that shortcut remains unverified. Revision 32's B1
feature observations carry forward because code/resources are identical. The full vanilla-server
interaction and installed-mod feature gates remain open.

Revision 33 exact-artifact interaction follow-up: the client moved forward on the vanilla server
from Z=-501.500 to Z=-492.869. A client right-click placed stone at `(3, 4, 0)` and a client
left-click broke it; vanilla RCON block queries confirmed stone and then air at that position. A
server-summoned villager opened the trade GUI through client interaction. The server accepted a
test teleport to X=260.5, saved the player, and on restart logged the rejoining player at
`(260.5, 4.0, 0.5)` while the client rendered the destination. This confirms saved-position
reconnect/chunk rendering; it does not certify a server correction during the same connection.
The test used the unmodified Mojang 1.12.2 jar above. Both client and server disconnected cleanly;
the server was flushed and stopped through RCON. The client log contains the known MaLiLib
config-override NPE also seen in the no-LiteOptimizer baseline. Potion use and natural portal
travel were not repeated on revision 33. Evidence: [interaction notes](revision33-interactions.md),
[client log](revision33-vanilla-followup-client.log), [server log](revision33-vanilla-followup-server.log),
[movement](revision33-vanilla-movement.png), [placed block](revision33-vanilla-stone-place.png),
[broken block](revision33-vanilla-stone-break.png), and [villager trade GUI](revision33-vanilla-villager-trade.png).

Revision 34 was built and installed after the revision 33 interaction pass. Built and installed
archives match at SHA-256
`3808b92e36261a365ced1b4f9b3ed9c5d9d12a5333e0e206bad148ab4a9905f9`; all archive payloads
match revision 33 except `litemod.json`'s revision field ([comparison](revision33-to-34-payload-compare.json)).
The exact revision 34 launched with the complete technical mod stack, logged `disabled: none`,
and joined/rendered the disposable B1 world. Evidence: [full-stack log](revision34-full-stack-b1.log)
and [B1 capture](revision34-b1-render.png). The exact archive then joined and rendered the vanilla
server; the direct evidence and limits are recorded below. The full release checklist remains open.


Revision 34 was also connected to the same fresh, unmodified Mojang server after its full-stack B1
smoke. The exact artifact joined at the player's persisted test position `(260.5, 4.0, 0.5)` and
rendered the flat world and server-summoned villager; the server log records the login and a clean
RCON `save-all flush`/`stop` followed. Evidence: [rev34 client log](revision34-vanilla-client.log),
[server log](revision34-vanilla-server.log), and [joined-world capture](revision34-vanilla-server-joined.png).
The client log contains the known MaLiLib config-override NPE on remote world entry, also seen in
the no-LiteOptimizer baseline. Revision 34's code/resources match revision 33 except for the
manifest revision; [the comparison record](revision33-to-34-payload-compare.json) preserves the
actual pre-install archive hashes and differing entry. Therefore revision 33's movement, block, and
trade interactions carry forward, but revision 34 has not repeated potion use or natural portal
travel, and this test did not check a server correction during the active connection. The full
installed-mod feature matrix remains open.


The final Java 8 offline build produced revision 35. The built and installed archives match at
SHA-256 `4c59feacd4b90a9d255893431b59617628db6c1460a1c73824e5b770c3f88645` with no differing
entries ([identity record](revision35-payload-identity.json)). Revision 35 launched with the full
instance stack, reported `disabled: none`, and joined/rendered the disposable B1 copy; exact
evidence is [startup/world log](revision35-full-stack-b1.log) and [B1 capture](revision35-b1-render.png).
The exact revision 35 also joined the unmodified Mojang server at the saved test position
`(260.5, 4.0, 0.5)` and rendered the world. The server was flushed and stopped cleanly through RCON.
Evidence: [client log](revision35-vanilla-client.log), [server log](revision35-vanilla-server.log),
and [capture](revision35-vanilla-server-joined.png). No source changes were made between the
revision 34 and 35 runtime checks. The known MaLiLib config-override NPE occurred on remote world
entry and has also been observed in the no-LiteOptimizer baseline. Exact-rev35 interaction
follow-up is recorded below; most of the installed-mod feature matrix remains open.

## Revision 35 exact-build interaction follow-up

On the exact revision 35 archive, a same-connection RCON correction from X=0.5 to X=260.5 was
saved to player NBT and rendered by the client. A Swiftness potion supplied by the server was
consumed in Survival; saved NBT held effect ID 1 and the returned glass bottle. Vanilla fire placed
inside a newly built obsidian frame activated the portal; server-side block checks and the
`We Need to Go Deeper` advancement confirm activation and travel, while saved NBT records Dimension
-1. The full procedure, exact server/build hashes, logs, and captures are in
[`revision35-interactions.md`](revision35-interactions.md).

The exact revision 35 Litematica check also loaded `6gt_Crafting_Station` and rendered its
translucent placement/selection overlay in disposable B1 without changing blocks. Toggling that
placement OFF removed the overlay; toggling it ON restored it. The placement was then returned to
OFF, its state at the start of this check; no world blocks changed. Captures:
[`revision35-litematica-placement-off.png`](revision35-litematica-placement-off.png)
and [`revision35-litematica-placement-on-toggle.png`](revision35-litematica-placement-on-toggle.png).
Layer hotkeys had no clear visible effect, so all-layer rendering, layer switching, verifier, and
paste remain open. Most of the installed-mod feature matrix is still unverified.

On that same exact revision 35 full-stack B1 session, MiniHUD Info Lines toggled ON with `H` and
back OFF. Configs → Renderers enabled the Light Level Overlay and visibly rendered green light
numbers over the floor; it was then turned OFF. No world blocks were changed. Captures:
[`revision35-minihud-info-on.png`](revision35-minihud-info-on.png),
[`revision35-minihud-info-off.png`](revision35-minihud-info-off.png),
[`revision35-minihud-lightlevel-on.png`](revision35-minihud-lightlevel-on.png), and
[`revision35-minihud-lightlevel-off.png`](revision35-minihud-lightlevel-off.png). Spawn sphere on B1
and the remaining MiniHUD feature checks remain open.

The same revision 35 session exercised Tweakeroo's default `Ctrl+Alt+F` Free Camera hotkey. The
ON toast appeared and the camera view detached; the same chord toggled the saved setting back to
`false`. After saving and quitting to title, rejoining the same disposable B1 copy placed the
client camera at the saved player position with the toggle still false. No blocks were changed.
Captures: [ON](revision35-tweakeroo-freecam-on.png) and [OFF/rejoin](revision35-tweakeroo-freecam-off.png).
The rejoin log records the player at the same `(-1969.5, 37.256, 700.5)` position:
[`latest.log` snapshot](revision35-tweakeroo-rejoin.log).

ChatCalc was checked again on revision 35 after inspecting its installed bytecode: the add-result
path expects Tab on a chat word ending in `=`. The chat field visibly contained `2+2=` and no
result appeared after the VNC Tab action. No chat message was sent. Since Tab delivery could not
be independently confirmed at the game input boundary, keep this compatibility item inconclusive.

## Revision 37 exact-build smoke

The Java 8 offline build produced revision 37, SHA-256
`53e9a3546baac034bb53e5fe8ec719cea071b7d32b0de43a01d3e819efbd247a`. It was installed into the
full technical-mod Prism instance with the same SHA-256, loaded all production options, reached the
title screen, and joined the disposable B1 copy. The only archive difference from installed
revision 35 is `litemod.json`'s revision number; the comparison record is
[`revision37-payload-compare.json`](revision37-payload-compare.json). Evidence:
[`full-stack log`](revision37-full-stack-b1.log) and [`B1 join capture`](revision37-b1-join.png).
The B1 session was saved and returned to the title screen without changing blocks. The attempted
F3+T sequence did not trigger a resource reload, so it remains unverified; ChatCalc's Tab result
also remains inconclusive. This revision-only archive change does not repeat the vanilla-server
interaction suite. The full installed-mod feature matrix and release gate remain open.

### Revision 37 vanilla-server follow-up

The exact revision 37 build was rechecked against a fresh, unmodified Mojang 1.12.2 server. The
built and installed archive hashes both equal
`53e9a3546baac034bb53e5fe8ec719cea071b7d32b0de43a01d3e819efbd247a`. It loaded with `disabled:
none`, joined at `(260.5, 4.0, 0.5)`, rendered the world, and moved south on the same connection.
After `save-all flush`, server player NBT recorded `(260.5015408199979, 4.0, 1.29812354028732)` in
dimension 0. The server then stopped cleanly. This adds exact-artifact join and movement evidence;
revision 35 remains the exact artifact for the earlier correction, potion, and portal interactions.

Evidence: [client log](revision37-vanilla-client.log), [server log](revision37-vanilla-server-followup.log),
[joined capture](revision37-vanilla-joined.png), and [movement/F3 capture](revision37-vanilla-movement.png).
The client log again contains the MaLiLib `ConfigOverrideUtils.tryApplyOverridesFromServer` NPE on
remote-world entry. It matches the previously observed no-LiteOptimizer baseline; the client kept
running, created its render threads, and loaded advancements. One initial server process was
externally terminated during setup; the successful persistent-server run above completed the
checks and shut down cleanly. No blocks were changed. This is a focused movement test, not a
replacement for the full vanilla interaction or installed-mod release checklist.

## Revision 38 exact-build launch and world join

The Java 8 offline build produced revision 38 with SHA-256
`7371af8350f1f88d299fad47123e5bda7270bc52e41fc3120c13430162819395`; that exact archive was
installed with an identical hash and all 726 archive entries match. It launched with the full
technical mod stack, reported `disabled: none`, joined the disposable B1 copy, and rendered it.
Evidence: [full-stack log](revision38-full-stack-b1.log), [B1 capture](revision38-b1-join.png), and
[built/installed identity](revision38-payload-compare.json). This proves launch and world join for
the latest artifact, not the remaining compatibility matrix. The known missing repeater texture
and VoxelMap legacy block-ID file errors also appear in startup logs; neither stopped launch or
world join. No world blocks were changed.

## Revision 39 build comparison

The subsequent Java 8 offline build produced revision 39, SHA-256
`ca26dc7b6108f89bf80f01823c5119ec3c5e9d697ef44c2827b74b04ff807e04`. Its 726 entries match
the installed, runtime-tested revision 38 byte-for-byte except `litemod.json`'s revision field.
Revision 39 has not been installed or launched; the exact comparison is
[`revision39-build-compare.json`](revision39-build-compare.json). No source changed between these
builds. The open session prevented a fresh install and launch, so release claims remain anchored
to revision 38.

## Revision 42 build: Tweakeroo immediate chunk-build bridge

Revision 42 built successfully (SHA-256
`529eb8ee32fa3a698107df11146eb933a583ee6975f1f5f342ebfa0f50ab4ce4`). It adds an optional
reflection bridge for `forceChunkRendersOnMainThread`: dirty Sodium meshes are built synchronously
on the render thread while enabled; per-section build revisions discard older async results when
a newer build supersedes them. The bridge's presence/absence, dynamic-toggle, and stale-result
filter self-checks pass. Exact
built/installed entry differences are recorded in
[`revision42-build-compare.json`](revision42-build-compare.json). It is not installed or launched,
so this remains an unverified implementation. Required runtime check: with a disposable world and
full mod stack, toggle Tweakeroo's setting OFF→ON→OFF while dirtying chunks, verify visible terrain
updates, no task/GL crashes, and no stale mesh reappears. Then run B3 state parity and the vanilla
server smoke on this exact artifact.

## Revision 44 build: safe async-result invalidation

Revision 44 is the latest successful Java 8 offline build (SHA-256
`813afcf58f88e2ab79bc8e934fb3acc2e14b2bb3687336a3d3a31d905441aba2`). It carries the
Tweakeroo immediate-build bridge and per-section build revisions: upload filtering rejects stale
async results after a newer rebuild/sort is created, so worker threads do not need to be stopped
when the setting changes. The optional-setting check and a harness executing the real upload-filter
method pass. Built/installed entry identity and changed class list:
[`revision44-build-compare.json`](revision44-build-compare.json). Revision 44 has not been installed
or launched; exact-revision render-thread and compatibility checks remain open.

# Compatibility checklist — the instance's mod set is a hard requirement

Every release must load next to **all** of these (PrismLauncher instance `1.12.2 test ai`,
`minecraft/mods/1.12.2/`) and every line below must still work. Run the full list at B1 for
a release candidate; during development check affected hooks. A failure blocks the release.

Revision112 is the frozen product build. The grouped session completes current-payload streaming (35,240cm; 16 X columns), clear portal arrival, F3+T and server-confirmed ItemScroller transfers, supplementing its previously successful vanilla interactions. B1 now also has inspected IsoRenders exports, VoxelMap terrain, LiteTweaker beam suppression/restoration, BBOutline toggling, MiniHUD beacon range/spawn chunks/unload-label update, and real WorldEditCUI selection rendering on a separate WorldEdit server. See the [grouped evidence](../profiles/revision112-release-gates-2026-10-16/README.md) and [build](../profiles/revision112-build-2026-10-16/README.md). All nine default-on options have gain/boundary evidence. The full remaining feature matrix and baseline mod failures still prevent release certification; the earlier reset risk has not recurred in this session.

Policy gate added 2026-10-14: the release must also work with an **unmodified vanilla 1.12.2
server**, without a server mod/plugin or custom protocol. Check join/reconnect, movement and
server corrections, block placement/breaking, inventory use, entity interaction, chunk streaming
and dimension changes. The gate has partial evidence recorded in
[`vanilla-server-smoke-2026-10-14`](../profiles/vanilla-server-smoke-2026-10-14/README.md).
Revision 87 passed 6,000-tick all-off/all-on cumulative parity on the stable `test` save. Revision 97 is a historical successful Java 8 offline build (SHA-256 `4a80a24fc6beb166872e30dcee58d982d8c6757c877220ad8d7e3fd639f71882`); its exact archive loaded with the full stack, reached the title screen and joined the unmodified vanilla server ([revision 97 build evidence](../profiles/revision97-build-2026-10-15/README.md)). Its archive differs from revision 96 only in manifest revision metadata. The exact revision-95 artifact’s Sodium B6 repeat at render distance 16 reduced average frame time by 59.7% and 1% low by 52.5%; a separate render-distance-10 pair showed 22.4% and 40.2% reductions ([revision 95 benchmark evidence](../profiles/revision95-sodium-B6-rd16-repeat-2026-10-15/README.md)). Revision 94 loaded the full target profile and joined/rendered disposable `B1-storage-smoke` ([revision 94 startup evidence](../profiles/revision94-build-2026-10-15/README.md)). Revision 94 passed 6,000-tick OFF/ON and OFF/OFF parity at fixed render distance 2 with identical dumps ([revision 94 parity](../profiles/revision94-parity-render-distance-2/README.md)); render-distance-10 controls still show intermittent chunk (-120, 44) outliers ([revision 93 diagnostic](../profiles/revision93-parity-diagnostic-2026-10-15/README.md)). Revision 87 also passes Tweakeroo Free Camera movement/return and main-thread F3+A reload. Revision 96 exact-artifact runs joined the unmodified server and verified movement, survival placement and breaking, potion consumption, a completed farmer trade (`stat.tradedWithVillager = 1`), hotbar-to-inventory transfer confirmed by server NBT selector, and portal travel to Nether coordinates with the vanilla advancement. The portal blocks were server-created, so natural frame activation remains open. A large RCON teleport emitted vanilla speed warnings and does not count as correction or distant-chunk-streaming proof ([revision 96 evidence](../profiles/revision96-vanilla-server-2026-10-15/README.md)). Revision 88 server smoke confirmed movement (876 cm scoreboard delta), a console-driven correction to a remote rendered position, and client stone placement and breaking confirmed by server block queries. Revision 90 completed a vanilla-generated farmer trade in Survival; the server awarded `What a Deal!` and reported `stat.tradedWithVillager = 1` ([trade evidence](../profiles/revision90-vanilla-trade-2026-10-15/README.md)). Revision 90 also passed vanilla-server potion use ([potion evidence](../profiles/revision90-vanilla-potion-2026-10-15/README.md)). Revision 91 passed vanilla-server potion use, natural portal travel, and inventory GUI open/close; see [revision 91 vanilla smoke](../profiles/revision91-vanilla-server-2026-10-15/README.md), [revision 87 parity](../profiles/revision87-parity-2026-10-15/README.md), [revision 87 Tweakeroo smoke](../profiles/revision87-tweakeroo-smoke-2026-10-15/README.md), and [revision 88 build/server smoke](../profiles/revision88-build-2026-10-15/README.md). Revision 97's server-side movement increased from 1,338 cm before a small, settled 0.25-block RCON correction to 1,950 cm afterward with no `moved wrongly` warning; [exact session evidence](../profiles/revision97-vanilla-server-2026-10-15/README.md).
Revision 35 joined the exact artifact to a fresh unmodified Mojang 1.12.2 server and rendered the
saved test position. Revision 33 follow-up confirmed client movement, client block placement and
breaking via server block queries, a server-summoned villager trade GUI, and a persisted server
teleport to X=260.5 followed by reconnect and chunk rendering. The logs/screenshots are in the
linked smoke profile. Historical
`0.6.0-rc2`/revision 21 evidence covers basic interactions and villager trading;
revision 22 covers natural portal activation and Nether travel. Revision 25 passes exact-artifact
join/render, forward movement, client block placement, and client block breaking, confirmed by
server-side position/block queries, successful-join reconnect, villager trade GUI interaction,
and dimension travel through a server portal block. The vanilla server recorded
`We Need to Go Deeper`. On the byte-identical revision 25 code/resources packaged as revision 28,
the vanilla server accepted authoritative teleports to X=0.5 and X=260.5; the latter crossed 16
chunk columns, the client rendered terrain, and saved player NBT confirms the destination. Natural
portal activation also passed on that payload: vanilla fire placement created portal blocks inside
the frame, the player entered the portal, and the vanilla server awarded `We Need to Go Deeper`;
saved player NBT records Dimension -1. Inventory use also passed: in Survival, the client consumed
a server-provided Swiftness potion; the client showed Speed and the returned glass bottle, while
saved player NBT records active effect id 1. The remaining installed-mod feature matrix is open.
These observations do not close the gate.
Carpet server evidence below does not satisfy it. Client visual tradeoffs are allowed, but must not break interactions or installed
rendering/overlay hooks.

## Evidence reuse audit — 2026-10-16

Revision 112 is frozen for release checking. A documentation edit or changed manifest revision does not invalidate unchanged code. The post-101 renderer upload-queue fix requires focused terrain/overlay checks, not another shared-state parity run. All nine release-default options have recorded gain and boundary evidence; see the current release-gate profile.

Carry forward the RC2 profile's real Carpet rule synchronization, SchematicPreview widget rendering, Litematica paste/verifier and Newlight torch light-update checks; later narrower UI checks do not revoke them. Carry forward revision-104 sphere/browser/placement/materials/verifier, revision-97 ChatCalc and light levels, revision-28 completed pack reloads, and revision-107/110 post-fix B1 rendering. These are feature-specific passes, not a claim that every row is complete.

Remaining after the grouped session: Tweakeroo dropped-shulker stacking, particle suppression, TE distance override and remaining item-frame/hologram variations; visibly generated MiniHUD structure boxes; full B1 certification. UsefulMod frame/shulker rendering, Litematica layer stepping, Carpet piston preview, fast-settings placement/breaking and hand restock now have evidence in the grouped profile. TellMe and Model Gap Fix remain baseline failures. Final build113 passed and differs from runtime-tested112 only in manifest revision; temporary settings were restored.

## Historical pre-fix artifact audit — 2026-10-13

Source: `091d4e1` plus the working-tree GUI mixin, its registration, and translations.
The `assets/lang/en_us.lang` diff only adds a final newline. The GUI mixin uses RETURN/HEAD
injections and is registered in the client list; no `GuiVideoSettings` target appears in the
foreign-hook matrix. `MixinPlugin` maps `render.sodium.gui` to the `sodium` option and render
master switch. Culling keys are separately registered in `Option.java`.

Identity before this audit build:
- Built `0.3.0.litemod`: SHA-256 `4ffc5dae456d853dca203913d756f2c569d766992c13ffb8a733ea6a254a429f`.
- Installed `0.3.0.litemod`: SHA-256 `6c4dee473c7a0a21a9354e8953b9ff50b93304670f1464b168619ce2a2ebaa6e`.

Audit validation: Java 8 offline build exited 0 in 6 s. Resulting jar SHA-256:
`427441697de606ccf2b694adfb47afec5e6cd02dde9379e31b4dbc61ded5e06c`.
`git diff --check` passed; no source changed and no game session was launched.

The pre-audit build and installed hashes differ; jar packaging timestamps may contribute. No archived session recorded a jar
hash, so its observations support history but cannot certify the current release artifact.
Do not silently relabel historical passes as passes on a fresh build.

**Current evidence, 2026-10-16:** exact revision 101 (SHA-256
`93bcdac9797fa11abbafcd9af3e3216a91201846041e8a3f09df0b0168d8de6e`) passed both 6,000-tick
OFF/OFF determinism and OFF/ON cumulative parity at render distance 2 on the stable `test` save.
All three dumps are byte-identical at total hash `9494ea25e3dee2acba33a46502beb2369ea15a1ccc1797f0ba7130c729ee6341`.
See the [revision-101 parity profile](../profiles/revision101-parity-2026-10-16/README.md).
Revision 100's mixed control remains historical evidence; the render-distance-10 chunk outlier is
still an unresolved fixture limitation.
Revision 86 (SHA-256
`fbe8ea0019561b4c0edc89baf956b15296e8c39cd175c44b6e825f08f463b934`) loaded with the full
installed stack in the disposable Prism clone and passed 6,000-tick all-off/all-on cumulative
parity and a matching all-off/all-off determinism control on the stable `test` save. All three dump
SHA-256 values match at `ec40cde8cf712ecd6b80a06d98d524bc1dc7ebc141a0c976edf5dbf60877912b`; see the
[revision 86 parity profile](../profiles/revision86-parity-2026-10-15/README.md). The earlier isolated Mixin 0.7.5 dev-client world smoke also resolved the MCP LVT startup failure. Exact revision-86 vanilla-server results are recorded in the [dedicated-server smoke profile](../profiles/vanilla-server-smoke-revision86-2026-10-15/README.md): reconnect, movement, place/break, trade GUI opening, potion use, Nether travel, and distant terrain streaming passed. The full installed-mod feature matrix and per-candidate gain evidence remain open.

| check | observed result | artifact/build evidence | remaining proof / prerequisite |
|---|---|---|---|
| Options entry, tabs, Apply/return | revision 23 opened all four tabs and Apply/Done returned without changing settings. Revision 25 also opened the LiteOptimizer Options screen from Video Settings. | exact revisions 23 and 25; [profile screenshots and log](../profiles/vanilla-server-smoke-2026-10-14/README.md) | revision 25 only captured General; full options-table carry-forward remains from revision 23 |
| Full instance startup/world join | Exact revision 104 (SHA-256 `4d2c3a44…593237`) loaded with the target Prism clone and rendered disposable B1; LiteOptimizer initialized, and the full stack session registered 17 mods. MiniHUD Info Lines toggled ON/OFF with the default hotkey and was restored OFF. TellMe remains disabled for its baseline MaLiLib API mismatch; Model Gap Fix’s mixin selection issue also reproduces with LiteOptimizer removed. | [revision 104 B1 smoke](../profiles/revision104-fullstack-B1-2026-10-16/README.md); [build and archive identity](../profiles/revision104-build-2026-10-16/README.md) | remaining installed-mod feature matrix; baseline-failing TellMe and Model Gap Fix still need a compatible installed configuration to count as working |
| Vanilla-server movement/place/break/trade/correction/potion/portal/inventory/chunk streaming | Exact revision 112 joined and completed an emerald-for-bread villager trade confirmed by the `trade112` statistic and saved inventory, accepted two small same-dimension corrections without movement warnings, and recorded +644 cm from a short Survival walk. Revision 111 passed breaking, potion use, 16-column terrain streaming, inventory transfer both ways and natural portal entry. Its dimension-conversion `moved too quickly` warning was reproduced by a LiteLoader-only client with an empty mods directory. The revision-112 session had another client connection reset without an exception, as in revision 111; the cause remains unknown. | [revision 112 vanilla-server evidence](../profiles/revision112-vanilla-server-2026-10-16/README.md); [revision 112 build](../profiles/revision112-build-2026-10-16/README.md); [revision 111 interactions/streaming](../profiles/revision111-vanilla-server-2026-10-16/README.md) | exact revision 112 placement and breaking at (18, 26, 4), both RCON-confirmed after correcting an initial aim miss; potion use (sustained F12 consumed the potion; server confirmed use statistic, returned bottle and Speed effect), portal and chunk streaming; full installed-mod feature matrix and investigation of the unexplained client reset |
| Litematica browser/load/placement/layers/material list | Exact revision 104 browsed to `6gt_Crafting_Station_1.12`, loaded it into memory, created a placement, and visibly rendered its translucent bounds in B1. Placement OFF removed the overlay; ON restored it; the temporary placement was removed. Its Material List showed 91 items. The Schematic Verifier completed on the 93-block schematic across four chunks, reporting 92 missing blocks and one wrong block at the selected B1 origin, with no world changes. | [revision 104 Material List](../profiles/revision104-fullstack-B1-2026-10-16/litematica-material-list.png); [verifier result](../profiles/revision104-fullstack-B1-2026-10-16/litematica-verifier.png); [post-removal capture](../profiles/revision104-fullstack-B1-2026-10-16/litematica-placement-removed.png); [revision 104 profile](../profiles/revision104-fullstack-B1-2026-10-16/README.md); [rev32/35 historical captures](../profiles/vanilla-server-smoke-2026-10-14/README.md) | all-layer coverage, layer switching, paste and remaining browser features |
| Litematica paste/verifier | RC2 profile passed: 93 setblock commands; aligned scan 2/2 chunks and selected-mismatch overlay | same RC2 profile; disposable server | current revision23 carry-forward; this is a functional smoke, not exact structure-state parity |
| MiniHUD info lines/spawn sphere/light-level overlay | Exact revision 35 showed Info Lines HUD after the `H` hotkey, then showed the OFF toast when toggled back. Its Configs → Renderers page enabled Light Level Overlay; green light numbers rendered over the B1 floor. Both settings were restored OFF. Exact revision 96 also toggled Info Lines ON and back OFF in the B1 copy with the full stack; no world blocks changed. Exact revision 97 enabled MiniHUD Light Level Overlay in the B1 scene, visibly rendered green block-light numbers, then restored the option OFF and saved without block changes. Exact revision 102 opened MiniHUD Configs with `H,C` and toggled Structure Bounding Boxes OFF then restored ON in the B1 copy; no matching generated structure was visible, so this covers the settings control only. The exact revision-102 follow-up and exact revision-104 B1 smoke both toggled Info Lines visibly ON then OFF and restored the setting; no world blocks changed. On exact revision 104 in B1, MiniHUD's Shape Manager created and visibly rendered a translucent `Can Spawn Sphere (>24)` centered on the player; the temporary shape was removed and the world config now has no shapes, with no block changes. `spawnChunkReal` remains false. Earlier Rev37 `H,C` attempts failed because the key chord was sent sequentially; the working chord sends both key-downs together. | [rev35 Info Lines on](../profiles/vanilla-server-smoke-2026-10-14/revision35-minihud-info-on.png), [off](../profiles/vanilla-server-smoke-2026-10-14/revision35-minihud-info-off.png), [rev35 light levels on](../profiles/vanilla-server-smoke-2026-10-14/revision35-minihud-lightlevel-on.png), [off/config](../profiles/vanilla-server-smoke-2026-10-14/revision35-minihud-lightlevel-off.png), [rev37 log](../profiles/vanilla-server-smoke-2026-10-14/revision37-fullstack-b1.log), [revision 96 Info Lines ON screenshot](../profiles/revision96-fullstack-B1-2026-10-15/rev96-minihud-info-lines-on.png), [revision 97 light-level overlay](../profiles/revision97-build-2026-10-15/minihud-light-level-on.png), [revision 102 MiniHUD structures toggle](../profiles/revision102-fullstack-B1-2026-10-16/README.md) | visible structure-box rendering in a world with a generated structure; chunk-unload bookkeeping, spawn chunks and beacon range |
| Tweakeroo config/main-thread rendering/F3+A/free camera | Revision 32 opened Tweakeroo Configs, enabled `Chunk Render On Main Thread`, and used F3+A. Revision 87 exact artifact started with the setting enabled in the disposable clone, loaded B1, accepted F3+A, and returned to the same rendered scene. Revision 87 also moved the Free Camera render view with W, then toggled OFF and returned to the player's X/Z without rejoining. Revision 91 placed a glowstone marker with main-thread mode OFF and tested the OFF→ON→OFF setting transition with F3+A reloads. Revision 97 tested server-confirmed glowstone placement/removal with main-thread mode ON and F3+A after each update, then restored air and config. Exact revision 101 opened Tweakeroo Configs in the full stack and toggled the setting OFF→ON→OFF; no rebuild was triggered. | [rev32 setting and reload](../profiles/vanilla-server-smoke-2026-10-14/revision32-tweakeroo-main-thread-enabled.png), [rev87 Tweakeroo smoke](../profiles/revision87-tweakeroo-smoke-2026-10-15/README.md), [rev91 dirty-section and live toggle](../profiles/revision91-dirty-section-2026-10-15/README.md), [rev97 dirty-section check](../profiles/revision97-dirty-section-2026-10-15/README.md), [rev101 full-stack UI toggle](../profiles/revision101-fullstack-B1-2026-10-16/README.md) | placement/restock/stacking and remaining features |
| Carpet Client | Exact revision 101 opened Carpet Client and its client-options screen in the full-stack B1 copy; the piston visualizer preference toggled and was restored. Server version was `null` in singleplayer, so no Carpet rule sync or piston preview was exercised. | [revision 101 Carpet UI smoke](../profiles/revision101-fullstack-B1-2026-10-16/README.md) | connect to Carpet server for rule sync, piston preview, and remaining render hooks |
| Other installed mods | Direct Connect version selector/join passed on RC2. Revision 28 toggled Soartex Fanver on and back off; both resource reloads completed, texture atlases were recreated, and the original four selected packs were restored. Exact revision 97 opened ChatCalc in the B1 copy, changed `2+2=` to `2+2=4` on Tab, then closed chat without sending the expression; the world was saved and quit. The attempted F3+T sequence opened the pause menu and did not produce resource reload logs. | RC2 profile, [revision28 reload evidence](../profiles/vanilla-server-smoke-2026-10-14/README.md), [revision97 ChatCalc result](../profiles/revision97-build-2026-10-15/README.md), [revision37 full-stack log](../profiles/vanilla-server-smoke-2026-10-14/revision37-full-stack-b1.log) | F3+T shortcut; ItemScroller, commands/outlines and other table features |

### Production configuration and optimization eligibility

| item | audit result | decision / focused follow-up |
|---|---|---|
| GL43 multidraw | load/save normalization forces false; control removed; current config false | production gate passed by code/config check; exact revision23 Advanced tab has no GL43 control |
| Compact vertex format | load/save normalization forces false; control removed; current config false | production gate passed by code/config check; exact revision23 Advanced tab has no compact-format control; vanilla block vertex layout is selected |
| Block-face culling | load/save normalization forces false; control removed; current config false | production gate passed by code/config check; exact revision23 Advanced tab has no block-face control |
| Fog occlusion | load/save normalization forces false; default and current config false | production gate passed by code/config check; discrepancy resolved |
| Translucency sorting | load/save normalization forces true; control removed; current config true | required vanilla ordering is enforced |
| Item-frame display lists | isolated historical B1 gain 18.13→10.39 ms, entity-inclusive pixel difference 0 | development gain retained; revision 96 full-stack B1 rendering smoke passed; interaction and remaining mod-feature checks still required |
| Entity culling | historical -89% entity section cost combines display lists/culling; reported clean images suppress moving entities but retain hanging entities | isolated gain and controlled entity-inclusive parity missing; ineligible for release certification until evaluated |
| Tile-entity frustum vs. occlusion | `tile_entity_frustum` has an isolated B1 no-entities result: average frame time 6.05→4.07 ms (-32.7%), 1% low 23.65→9.54 ms (-59.7%), and `blockentities` 4.192→2.629 ms (-37.3%); stable-save 6,000-tick parity and client-only boundary are documented. The separate `tile_entity_occlusion` path measured only -2.0% and was removed as redundant. | Keep frustum eligible; full-stack visible-TESR and vanilla-server interaction checks remain open. Occlusion stays rejected. |
| Entity model matrix | 2,665 pixel differences; implementation removed | rejected; do not reopen without a new parity-safe approach |
| Light-cache tuning | existing profiles do not identify cache reads as hot | deferred; no implementation task |

Production configuration normalization is implemented and verified: load/save forces the
unsafe renderer paths off and translucency sorting on, with their UI controls removed. The
current Prism config records those safe values. `python3 tools/check-sodium-options.py` passed
against real Gson 2.8.0, including unsafe old JSON, missing files, null sections, malformed
JSON, and write/reload. The rebuilt revision 21 candidate loaded with the full installed stack;
its startup evidence is in `docs/profiles/vanilla-server-smoke-2026-10-14/`. This clears the
configuration blocker; it does not replace the remaining renderer, mod-feature, and vanilla
server interaction checks.

### Current revision 21 carry-forward — 2026-10-14

The Java 8 offline build produced RC2 revision 21, SHA-256
`161e419ca8ddc143371f0dd5182144ed4ef1c146dd5ae867ea8e9b48fa6d1d86`. It was installed and
loaded with the full technical stack, then joined a disposable copy of `B1-storage-smoke` at
`(-1969.5, 37.25, 700.5)`. All packaged entries other than `litemod.json`'s revision field match
the interaction-tested revision 20 artifact byte-for-byte. Startup logs show no new mixin
conflicts compared with the no-LiteOptimizer baseline; the normalized seven distinct conflict
warnings are pre-existing. The MaLiLib config-override NPE also reproduces in the no-mod
baseline. Logs and hashes are in `docs/profiles/vanilla-server-smoke-2026-10-14/`.

The B1 save was copied to `B1-release-smoke-2026-10-14` before opening; the original save was
not used for interactive checks. This session confirmed launch, full-stack mod initialization,
and world join. The F3+T resource reload check is still pending: prior VNC key-chord attempts
failed in `vncdo 1.4.2` with an `ord()` parser error; a later diagnostic sequence hit the same
error before sending the chord. Stop this input path. Entity interaction was separately verified
on the revision 21 client: right-clicking a vanilla-server villager opened its trading GUI. The screenshot
and dedicated server log are in the vanilla smoke profile. At revision 21, the earlier dimension
test manually placed the portal block; revision 22 later passed natural portal-frame activation
and travel. See the vanilla smoke profile for artifact identity and evidence.

### Revision 22 carry-forward — 2026-10-14

The Java 8 offline build produced revision 22, SHA-256
`0b92be88329fc2abc7be9c8579a8af23ffe58e734de2f02da871ebf0f7e4e616`. Compared with revision 21,
all 726 archive entries match except the `revision` field in `litemod.json`; the rebuilt artifact
was installed and the full technical stack loaded it, initialized LiteOptimizer with no disabled
options, and joined the disposable `B1-release-smoke-2026-10-14` copy. Evidence:
[`revision22-full-stack-b1.log`](../profiles/vanilla-server-smoke-2026-10-14/revision22-full-stack-b1.log)
and [`revision22-b1-join.png`](../profiles/vanilla-server-smoke-2026-10-14/revision22-b1-join.png).
On this artifact, Options → Video Settings → LiteOptimizer Options opened, all four settings tabs
rendered, and Done returned through the parent menus without a crash. No settings were changed.
Captures: [`revision22-video-settings.png`](../profiles/vanilla-server-smoke-2026-10-14/revision22-video-settings.png),
[`revision22-sodium-options.png`](../profiles/vanilla-server-smoke-2026-10-14/revision22-sodium-options.png),
and the Quality, Advanced, and Performance captures beside them. This carries forward revision 21
behavior checks but does not certify the unfinished feature table.
MiniHUD Info Lines was visibly present in the same disposable B1 copy, toggled off with its `H`
hotkey, then toggled back on to its original state. Both captures are recorded; no persistent
config change or world-block change was made.
See [`revision22-minihud-off.png`](../profiles/vanilla-server-smoke-2026-10-14/revision22-minihud-off.png)
and [`revision22-minihud-on.png`](../profiles/vanilla-server-smoke-2026-10-14/revision22-minihud-on.png).
The MiniHUD `H,C` config shortcut did not open after two VNC input sequences. On 2026-10-14,
revision 23 was freshly launched and joined the disposable B1 copy; MiniHUD's light-level toggle
was enabled in its config before launch, and green level numbers were visibly rendered over the
test floor. The screenshot and full-stack join log are
[`revision23-minihud-lightlevel.png`](../profiles/vanilla-server-smoke-2026-10-14/revision23-minihud-lightlevel.png)
and [`revision23-full-stack-b1.log`](../profiles/vanilla-server-smoke-2026-10-14/revision23-full-stack-b1.log).
The temporary config toggle was restored after a clean game shutdown. This confirms only the
overlay rendering path; the remaining MiniHUD feature checks stay open.
The Carpet Client main menu and client options also open on revision 22, but show server version
`null` in this singleplayer world. This is menu-only evidence; Carpet server rules and hooks still
need a Carpet server fixture. Capture and limits:
[`revision22-carpet-client-options.png`](../profiles/vanilla-server-smoke-2026-10-14/revision22-carpet-client-options.png)
and the profile README.

The latest Java 8 offline build succeeded and produced revision 24, SHA-256
`03ddc07cb65c2442b80a1ffea04bced8b5e20ccd47092953cc1943468f425800`. The exact artifact is
installed in the Prism instance with the same hash. A fresh launch loaded LiteOptimizer with no
disabled options and joined the disposable B1 copy with the full technical mod stack; its seven
distinct foreign mixin-conflict warnings match the no-mod baseline. Evidence:
[`revision24-full-stack-b1.log`](../profiles/vanilla-server-smoke-2026-10-14/revision24-full-stack-b1.log)
and [`revision24-mixin-warning-comparison.json`](../profiles/vanilla-server-smoke-2026-10-14/revision24-mixin-warning-comparison.json).
The full feature compatibility table is still incomplete.

Optimization selection: the current comparison and profiles produced two accepted client candidates
and no further candidate that clears the gain/correctness bar. Do not add speculative render work;
select another optimization only after a new profile identifies a hot path with a safe boundary.

| mod (file) | what to try | our mixins that touch its targets |
|---|---|---|
| LiteOptimizer 0.6 UI | Video Settings opens the LiteOptimizer Options button; Sodium options open, return, and save correctly | `GuiVideoSettings.initGui/actionPerformed` |
| malilib 0.54.0 | config screens open, hotkeys fire | none planned |
| litematica 0.31.4 | Exact rev97 B1 copy opened its `M` menu, previewed and loaded `6gt Crafting Station` (93 blocks), created a placement, and rendered its translucent overlay. Material List showed 91 distinct items and populated block rows. The temporary placement was removed and the world saved with no block changes. Still test all layers, verifier, paste and remaining browser features. | [rev97 Litematica interaction](../profiles/revision97-build-2026-10-15/README.md); [overlay screenshot](../profiles/revision97-build-2026-10-15/litematica-placement-overlay.png); [material list screenshot](../profiles/revision97-build-2026-10-15/litematica-material-list.png) | `RenderGlobal` (Sodium pillar), `ChunkProviderClient`, `CompiledChunk`, `ViewFrustum`, `Chunk`, `World`, `WorldClient` |
| minihud 0.41.1 | info lines, shapes (spawn sphere), light-level overlay, chunk-unload bookkeeping, structure boxes, spawn chunks, beacon range | `RenderGlobal`, `ChunkProviderServer`, `World`, `WorldClient`, `PathNavigate`, `TileEntityBeacon` |
| tweakeroo 0.31.0 | Exact revision 104 toggled `Chunk Render On Main Thread` ON→F3+A→OFF→F3+A in B1; the scene rendered in both states and the setting was restored OFF. Free Camera and F3+A also have exact revision-87 coverage. Revision 97 tested real block placement/removal with main-thread rendering ON and rebuilt after each change; revision 91 tested OFF→ON→OFF with reloads. Still test flexible placement, fast placement/breaking, hand restock, shulker stacking (`EntityItem`), particle disable, TE render-distance override, and item-frame/hologram tweaks | `RenderGlobal.setupTerrain`/`updateChunks`, `ChunkRenderDispatcher`, `ChunkRenderWorker`, `ParticleManager`, `TileEntityRendererDispatcher`, `RenderManager`, `EntityItem`, `Explosion`, `World`, `Entity` |
| itemscroller 0.31.1 | scroll items in a GUI | none |
| Carpet Client 1.4d (loaded; 1.4c is ignored as the older revision) | piston preview, `notifyLightSet` hook, `RenderChunkOptifine`, `TileEntityRenderDispatcher`, `BufferBuilder`, `World`/`WorldServer` rules sync | `RenderGlobal.notifyLightSet`, `BufferBuilder` (translucency sort), `World`, `WorldServer`, `EntityLivingBase`, `Entity` |
| newlight 1.0.2 | lighting still correct after block changes; chunk load light | must be zero overlap: `World`, `WorldServer`, `Chunk`, `ChunkProviderServer`, `ExtendedBlockStorage`, `SPacketChunkData`, `AnvilChunkLoader`, `RenderGlobal.notifyLightSet` |
| litetweaker 1.10.4 | its disables/tweaks (beacon beam, vignette, block hit…) | `TileEntityBeaconRenderer`, `Block`, `BlockRenderDispatcher`, `EntityRenderer` |
| UsefulMod 1.1.0 | client commands, item frame/shulker rendering tweaks, `RenderGlobal.isOutlineActive`/`playEvent` | `RenderGlobal`, `RenderItemFrame`, `EntityItem`, `ParticleManager`, `TileEntityRendererDispatcher`, `RenderManager` |
| tellme 0.6.3 | Dump commands cannot currently be exercised: enabling TellMe on the disposable clone fails at init with a `FileUtils.getConfigDirectory()` `NoSuchMethodError`. The identical exception occurs with LiteOptimizer disabled; the installed MaLiLib 0.54.0 exposes `getMinecraftDirectory(): Path` instead. TellMe is disabled in the target profile, so this is a pre-existing TellMe/MaLiLib version mismatch, not a LiteOptimizer regression. | [revision 93 TellMe baseline](../profiles/revision93-tellme-baseline-2026-10-15/README.md) | Revisit only if the installed TellMe/MaLiLib pair changes |
| chatcalc 1.0 | chat math | none |
| connect_to_1_12_x 1.5 | server list | none |
| voxelMap 1.7.1 | minimap renders, `renderSky`/`updateClouds` hooks | `RenderGlobal.renderSky/updateClouds` (Sodium sky mixin) |
| worldeditcui 1.12.1 | selection box renders | `RenderGlobal`? (check refmap — none listed) |
| isorenders 0.1.0, schematicpreview 0.1.0 (own mods) | isometric render; schematic preview widgets | `Framebuffer`/GL state only |
| BBOutlineReloaded 1.0.13 | structure outlines | `Chunk`, `WorldClient`, `WorldServer`, `MinecraftServer` |
| Model Gap Fix 1.0.0 | Revision 110 repeats the Mixin 0.7.5 selection NPE for `mixins.modelgapfix.json`; the rev97 no-LiteOptimizer baseline shows the same failure, so this is not a LiteOptimizer regression. It does not stop client startup or B1 rendering, but the visual fix is not counted as working. Evidence: [rev110 B1 startup](../profiles/revision110-fullstack-B1-2026-10-16/README.md), [rev97 startup and no-LiteOptimizer baseline](../profiles/revision97-build-2026-10-15/README.md). Focused generated-model/item-frame render check remains open. | `ItemModelGenerator.getBlockParts` (generated scan; LiteOptimizer has no target there) |

LiteOptimizer 0.6 UI smoke passed 2026-10-01 in the isolated headless Sway session: the translated
button opened Sodium options, Done returned to Video Settings, and Apply persisted a temporary
`enable_clouds` change across close/reopen. The original `enable_clouds=true` value was restored
and confirmed in `liteoptimizer-sodium.json`. This does not cover B1 overlay smoke for the other
installed mods.

Partial smoke completed 2026-09-30 through isolated headless Sway/WayVNC after a fresh game
restart. In the explicitly selected `B1-storage-smoke` copy, Litematica's menu/browser,
schematic load/create-placement, and translucent colored overlay rendered; MiniHUD info lines
rendered; Tweakeroo's config hotkey and main-thread chunk rendering with F3+A reload worked
without a logged exception. [Screenshot](../profiles/0.6-overlay-smoke.png) is from
`-1970 38 700 45 30`, near the recovered B1 pose. The temporary schematic was unloaded and
MiniHUD/Tweakeroo test toggles restored. A spawn sphere rendered in the separate `test` save;
its B1 rendering is still unverified. The remaining table features are pending, so this is not
a full checklist pass. Build command `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build
--offline` exited 0; the UI diff review found no correctness issues.

Follow-up on 2026-10-03: launched the full test instance through the nested Sway desktop at
`localhost:5900`. LiteLoader loaded LiteOptimizer with the full mod set, reached the title screen,
and joined `B1-storage-smoke` without a startup crash. MiniHUD's config screen opened in that
world. VNC input required WayVNC's transient virtual seat; further config controls still need a
reliable interaction path. The on-disk MiniHUD info-line setting remains `false`. This confirms
startup and world join only; sphere, verifier/layers/paste, free camera/placement, and Carpet hooks
remain pending.

Follow-up later on 2026-10-03: launched with `/home/froyln/Dev/ai-desktop.sh prism` and joined
`B1-storage-smoke` at `(-1969.5, 38.0, 700.5)`. MiniHUD info lines visibly rendered date/time,
coordinates, facing, and TPS; its config GUI showed Info Lines HUD ON, and the shared config now
has `infoLinesRendering.enabled=true`. The H then C shortcut opened MiniHUD config. Shapes and
the B1 spawn sphere remain unverified. Prism later reported the Minecraft process had crashed;
the end of `latest.log` contained no Java exception and no newer crash report was present, so the
cause is undetermined.

Follow-up on 2026-10-13 in the isolated Sway/WayVNC desktop: the full instance launched and joined
`B1-storage-smoke` at `(-1970, 38, 700)`. MiniHUD's `Can Spawn Sphere (>24)` shape rendered over
the storage area; the temporary shape was removed afterward. Its light-level toggle was enabled
and then restored to off, but a visible light overlay was not confirmed. Litematica's browser
loaded `6gt_Crafting_Station`, created a placement, and rendered the schematic overlay in B1; the
temporary placement was removed and the schematic unloaded. No world blocks were changed. The
previous VNC input issue is resolved: key chords work when key-down and key-up events are sent in
one `vncdo` sequence.

Continuation later the same day: Litematica's Material List calculated 91 total items for
`6gt_Crafting_Station`. Tweakeroo's free camera was toggled on and back off; no player movement was
made. Carpet Client's menu and options screens opened, but showed server version `null`, so
server-dependent behavior and piston/render hooks remain untested. The instance contains both 1.4c
and 1.4d files; LiteLoader selected 1.4d revision 1303 and ignored 1.4c revision 30. Startup logs
also show existing conflicts among Carpet Client, Tweakeroo, LiteTweaker, UsefulMod, and LiteLoader
(including skipped redirects/overwrites); a clean compatibility run is not established. The
verifier, all-layer rendering, paste, light-level visibility, and remaining per-mod checks are
still open. The game was returned to the title screen after testing.

Continuation on 2026-10-13: Litematica's verifier opened for `6gt_Crafting_Station` and completed
its scan across 4/4 chunks. It displayed 1 wrong block and 92 missing blocks at the default,
unaligned placement; this verifies the UI and scan path only, not a valid structure comparison.
The temporary placement was removed and the schematic unloaded, with no world blocks changed.
The game was saved back to the title screen. Aligned verifier results, all-layer rendering,
paste, light-level visibility, and the remaining per-mod checks are still open.

Continuation later on 2026-10-16: revision 106 crashed during the first Sodium chunk upload while joining the disposable B1 scene (`NoClassDefFoundError: FutureDequeDrain`). Revision 107 removed that standalone helper; the exact artifact then loaded the B1 scene with all 17 LiteLoader mods and rendered the technical storage area. This confirms startup/join/chunk-upload only, not each mod feature. See [revision 107 runtime evidence](../profiles/revision107-fullstack-B1-2026-10-16/README.md). The vanilla-server gate needs a fresh run on this changed payload.

Continuation on 2026-10-16 using exact rev104 in the disposable clone: Litematica's layer-mode hotkey persisted `SINGLE_LAYER`; the layer-step hotkeys were also exercised, but captures do not clearly establish the resulting visual change. The test mode was restored to `ALL` and the placement map is empty. Count this as a mode-toggle smoke only; layer rendering and paste remain open. No world blocks were changed. Evidence: [rev104 full-stack B1 profile](../profiles/revision104-fullstack-B1-2026-10-16/README.md).

The mechanical input for this table is `docs/compat/instance-mixins.md` (every vanilla member the
other installed mods inject into or redirect), regenerated with `python3 tools/compat-scan.py`
whenever a mod in the instance is updated. Rules:

1. Before landing any mixin, grep its target class **and** method in `instance-mixins.md`.
2. A foreign `member` row on a method we `@Overwrite` = **forbidden** unless the foreign injection
   is at `HEAD`/`RETURN` and our overwrite keeps a vanilla-shaped body for it to land in. Prefer
   `@Inject` + cancel over `@Overwrite` everywhere a foreign row exists.
3. A foreign `call` row (some mod redirects an invocation inside a method we rewrite) = the
   invocation must still exist in our version with the same owner/name/descriptor, or we hand
   that mod's redirect the same semantics some other way. Tweakeroo's
   `RenderGlobal.updateChunks → ChunkRenderDispatcher.updateChunkNow` and Litematica's
   `EntityRenderer → RenderGlobal.renderBlockLayer/renderEntities/updateChunks` are the known ones
   for the Sodium pillar.
4. Mixin application order across mods is by `priority`; ours default to 1000 like theirs. When a
   foreign mixin must see our change (or must not), set priority explicitly and write why in the
   catalog row.
5. Two `@Overwrite`s on one method (theirs + ours) crash at load — Newlight overwrites lighting
   methods; that is why lighting is off-limits.

## Current entity-culling decision (2026-10-13)

[Focused B1 comparison](../profiles/entity-culling-B1-2026-10-13/README.md) changes only
entity culling with Sodium/display lists held on. Frame average 3.24→3.31 ms: no demonstrated
gain; general entity parity remains unproven. 52 full-image differences are confined to dynamic
MiniHUD text; 0 differences below top 96 rows is limited scene evidence, not a full-screen pass.
Release eligibility remains **unproven**. Previous `--no-entities` captures retain item frames
and paintings; the older statement that they excluded all entities was too broad.

## Current TE-culling decision (2026-10-13)

[Focused comparison](../profiles/te-culling-B1-2026-10-13/README.md): frustum has repeated
18.3%/17.8% average-frame gains with the other switches fixed. Full capture parity is still
unproven: original off/off differs at 19 pixels; final particle/glint-controlled off/on differs
at 9 pixels. No tolerance was introduced. Resolve final-settings capture reproducibility
before certifying frustum. TE occlusion is redundant for ordinary Sodium's visible list,
with no established gain; its damage/foreign-dispatch geometry argument also needs proof.
It remains release-ineligible. Configs and the prior scratch save were restored.

## Conservative 0.6.0-rc2 candidate — 2026-10-13

[Identified candidate evidence](../profiles/0.6.0-rc2-2026-10-13/README.md) supersedes the
release eligibility of removed entity graph culling, TE occlusion and generic TE frustum
culling. The latter has vanilla RNG/GL-state side effects that invalidate its generic proof.
The candidate retains measured display lists:6.26→5.14ms, exact off/on and unchanged-control
images both0pixels. Cumulative B3-hoppers600tick parity passes.

Current artifact smoke confirms options Apply/save/return, full-stack B1 server join, Carpet
version/rules sync, schematic browser/preview/all-layer overlay/material list, paste and aligned
verifier selection overlay, visible MiniHUD light numbers and torch updates, free-camera
movement with unchanged server player position, and Direct Connect version selector/join.
Seven distinct foreign conflict warnings also occur without LiteOptimizer; none are added.
Carpet/malilib baseline payload exceptions remain, and the whole feature table is **not passed**.
This is an untagged development candidate; historical observations are not blanket release proof.
Revision 37's exact built and installed archive also joined an unmodified vanilla 1.12.2 server,
rendered the world, and persisted same-connection movement from `(260.5, 4.0, 0.5)` to
`(260.5015, 4.0, 1.2981)` in server player NBT. See the revision 37 vanilla follow-up evidence in
the profile. The remote-join MaLiLib NPE still occurs and matches the no-LiteOptimizer baseline.
This exact-artifact check does not close other server interactions or the remaining feature rows.

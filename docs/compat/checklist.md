# Compatibility checklist — the instance's mod set is a hard requirement

Every release must load next to **all** of these (PrismLauncher instance `1.12.2 test ai`,
`minecraft/mods/1.12.2/`) and every line below must still work. Run the full list at B1 for
a release candidate; during development check affected hooks. A failure blocks the release.

## Current artifact audit — 2026-10-13

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

| check | observed result | artifact/build evidence | remaining proof / prerequisite |
|---|---|---|---|
| Options entry, return, Apply/save | passed; original clouds toggle restored | 2026-10-01 rebuilt working tree; historical hash absent | confirm on identified release candidate after relevant code changes |
| Full instance startup/world join | B1 joined on 2026-10-13 | installed 0.3.0; historical hash absent | identified candidate startup; baseline comparison for foreign conflict warnings |
| Litematica browser/load/placement/translucent overlay/material list | passed; 91 material items | B1 smoke, 2026-10-13; historical hash absent | explicit all-layer check and prepared paste test in disposable world |
| Litematica verifier | UI/scan finished 4/4 chunks, 1 wrong + 92 missing at unaligned placement | B1 smoke, 2026-10-13; historical hash absent | aligned comparison and selected-mismatch world overlay; scan completion alone is insufficient |
| MiniHUD info lines/spawn sphere | visibly rendered; sphere removed | B1 smoke, 2026-10-13; historical hash absent | light-level visibility, other table features on candidate |
| Tweakeroo config/main-thread rendering/F3+A/free camera toggle | observed; free camera restored off | 2026-09-30 and 2026-10-13; historical hash absent | camera movement, placement and remaining table features in disposable copy |
| Carpet Client | 1.4d selected; 1.4c ignored; menu opened with server version null | startup log and B1 menu, 2026-10-13 | suitable Carpet server fixture and piston/render/rules checks |
| Other installed mods | no complete feature smoke record | startup alone is not a feature pass | prepared checks from table below in one release session |

### Configuration and optimization eligibility (pre-fix audit)

| item | audit result | decision / focused follow-up |
|---|---|---|
| GL43 multidraw | default false; UI enables on supported hardware; backend factory honors true; known sub-pixel differences | release blocker: enforce fixed-function backend for production even with saved true config; remove/disable production selector |
| Compact vertex format | default false; saved/UI true selects HFP and shader oneshot even without GL43 | release blocker: enforce vanilla SFP layout; UI and saved config must agree |
| Block-face culling | default false; user-selectable; report lists non-parity mode | ineligible until proved; include in production parity-configuration follow-up |
| Fog occlusion | default **true**, despite port report calling it opt-in/non-parity; read by chunk manager | release blocker: resolve code/report discrepancy; enforce parity-safe path pending proof |
| Translucency sorting | default true, user-selectable | audit disabling this alongside parity-sensitive settings; do not certify arbitrary settings as vanilla output |
| Item-frame display lists | isolated historical B1 gain 18.13→10.39 ms, entity-inclusive pixel difference 0 | development evidence retained; release artifact and full mod checks still required |
| Entity culling | historical -89% entity section cost combines display lists/culling; reported clean images suppress moving entities but retain hanging entities | isolated gain and controlled entity-inclusive parity missing; ineligible for release certification until evaluated |
| TE frustum + occlusion | combined -42.3% frame figure; TE section gain within noise; clean terrain screenshots suppress entities | no per-feature gain attribution; evaluate each with renderer/dependencies held constant and visible TESRs in controlled captures |
| Entity model matrix | 2,665 pixel differences; implementation removed | rejected; do not reopen without a new parity-safe approach |
| Light-cache tuning | existing profiles do not identify cache reads as hot | deferred; no implementation task |

Production configuration fix completed 2026-10-13: config load/save now forces multidraw,
compact vertices, block-face culling, and fog occlusion off and translucency sorting on.
Their UI controls were removed; unrelated quality/performance/culling settings are preserved.
`python3 tools/check-sodium-options.py` exercises real Gson 2.8.0 load/save, unsafe old JSON,
missing files, null sections, and malformed JSON with only Minecraft text/logging boundaries
stubbed. It passes on the fix and fails on the pre-fix source. This proves configuration
normalization, not rendered pixels or the in-game layout. Those candidate checks remain owed.
Review found no material issues. Java 8 offline build exited 0 in 12 s; resulting jar SHA-256:
`8135c1c9287e4c716e9133a0965e974bc8dc89cc6eb110071d5ff9e829fe19fc`. This new jar has not been installed into the running test instance.

Evaluation order: production parity configuration is constrained; next use one focused culling evaluation
with explicit off/on dependencies. Use a representative occluded-TE scene if B1's existing
vantage cannot expose a gain. If evidence remains inconclusive, defer rather than manufacture
an individual gain from the combined result. No new optimization is selected from stale profiles.

| mod (file) | what to try | our mixins that touch its targets |
|---|---|---|
| LiteOptimizer 0.6 UI | Video Settings opens the LiteOptimizer Options button; Sodium options open, return, and save correctly | `GuiVideoSettings.initGui/actionPerformed` |
| malilib 0.54.0 | config screens open, hotkeys fire | none planned |
| litematica 0.31.4 | load a schematic, placement renders (all layers, translucent + overlay), verifier overlay, paste, material list, schematic browser | `RenderGlobal` (Sodium pillar), `ChunkProviderClient`, `CompiledChunk`, `ViewFrustum`, `Chunk`, `World`, `WorldClient` |
| minihud 0.41.1 | info lines, shapes (spawn sphere), light-level overlay, chunk-unload bookkeeping, structure boxes, spawn chunks, beacon range | `RenderGlobal`, `ChunkProviderServer`, `World`, `WorldClient`, `PathNavigate`, `TileEntityBeacon` |
| tweakeroo 0.31.0 | free camera, `tweakChunkRenderDistance`/`forceChunkRendersOnMainThread`? (its `RenderGlobal.updateChunks` redirect), flexible placement, fast placement/breaking, hand restock, shulker stacking (`EntityItem`), particle disable, TE render distance override, item frame/hologram tweaks | `RenderGlobal.setupTerrain`/`updateChunks` (**direct collision with Sodium**), `ChunkRenderDispatcher`, `ChunkRenderWorker`, `ParticleManager`, `TileEntityRendererDispatcher`, `RenderManager`, `EntityItem`, `Explosion`, `World`, `Entity` |
| itemscroller 0.31.1 | scroll items in a GUI | none |
| Carpet Client 1.4d (loaded; 1.4c is ignored as the older revision) | piston preview, `notifyLightSet` hook, `RenderChunkOptifine`, `TileEntityRenderDispatcher`, `BufferBuilder`, `World`/`WorldServer` rules sync | `RenderGlobal.notifyLightSet`, `BufferBuilder` (translucency sort), `World`, `WorldServer`, `EntityLivingBase`, `Entity` |
| newlight 1.0.2 | lighting still correct after block changes; chunk load light | must be zero overlap: `World`, `WorldServer`, `Chunk`, `ChunkProviderServer`, `ExtendedBlockStorage`, `SPacketChunkData`, `AnvilChunkLoader`, `RenderGlobal.notifyLightSet` |
| litetweaker 1.10.4 | its disables/tweaks (beacon beam, vignette, block hit…) | `TileEntityBeaconRenderer`, `Block`, `BlockRenderDispatcher`, `EntityRenderer` |
| UsefulMod 1.1.0 | client commands, item frame/shulker rendering tweaks, `RenderGlobal.isOutlineActive`/`playEvent` | `RenderGlobal`, `RenderItemFrame`, `EntityItem`, `ParticleManager`, `TileEntityRendererDispatcher`, `RenderManager` |
| tellme 0.6.3 | dump commands | `Block`, `Chunk`, `TileEntity` accessors only |
| chatcalc 1.0 | chat math | none |
| connect_to_1_12_x 1.5 | server list | none |
| voxelMap 1.7.1 | minimap renders, `renderSky`/`updateClouds` hooks | `RenderGlobal.renderSky/updateClouds` (Sodium sky mixin) |
| worldeditcui 1.12.1 | selection box renders | `RenderGlobal`? (check refmap — none listed) |
| isorenders 0.1.0, schematicpreview 0.1.0 (own mods) | isometric render; schematic preview widgets | `Framebuffer`/GL state only |
| BBOutlineReloaded 1.0.13 | structure outlines | `Chunk`, `WorldClient`, `WorldServer`, `MinecraftServer` |

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

The mechanical input for this table is `docs/compat/instance-mixins.md` (638 rows: every vanilla
member those mods inject into or redirect), regenerated with `python3 tools/compat-scan.py`
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

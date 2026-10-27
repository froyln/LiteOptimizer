# LiteOptimizer execution plan

Updated 2026-10-16. Optimize the Minecraft 1.12.2 client for unmodified vanilla-server play.
Client-only rendering/presentation may change with documented configurable tradeoffs; server
simulation, RNG, update order, player actions and vanilla networking remain unchanged.
Keep Java 8, LiteLoader/Mixin 0.7.5, LGPL-3.0, and no runtime dependencies.
Architecture and invariants: `AGENTS.md`. Candidate inventory: `docs/catalog.md`.
Replanning validation: planning review found no material issues;
`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` produced revision 99 and exited 0 (7 s).

Verification procedures: `docs/methodology.md`. Previous evidence and task history:
`docs/history/plan-through-2026-10-13.md` (read only the relevant section when needed).

## GitHub publication — 2026-10-17

Published https://github.com/froyln/LiteOptimizer (public), main `73a5ff6`, with 36 focused
component/optimization/documentation commits. Clean publication checkout:
`/home/froyln/Dev/LiteOptimizer`. Original local history and workflow files are preserved here;
none of AGENTS.md, CLAUDE.md, PLAN.md, .claude/, session logs, desktop automation or owner-specific
launch scripts entered the public repository history. All 465 source/resource files in the
publication checkout match this working mod byte-for-byte.

Removed the upstream coffee donation widgets, URL handler and six translation entries. Kept
upstream copyright/credit notices. README documents nine enabled features, configuration,
compatibility limitations and measured OFF/ON gains, with three retained raw CSV pairs and
explicit provenance limits for older reports. The historical structure-index percentage is
not asserted because its recorded percentage disagrees with displayed timings. Full license
texts accompany the public source.

Validation: `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0 here
(19 s) and in the clean publication checkout (20 s). Public source equality and benchmark
arithmetic checks passed; read-only review found no remaining README/UI issues. Built archive
inspection confirms the donation widgets/text are absent and the mixin refmap is present.
No new in-game session or benchmark was run for this UI/documentation publication; prior runtime
evidence and open release-certification limitations remain as recorded below. No release/tag
or binary asset was published. Remote main matches local publication HEAD.

## Shulker render updates — 2026-10-17

Public fix commit: `4cebf2a`, branch `fix/shulker-render-updates`; pushed with PR
https://github.com/froyln/LiteOptimizer/pull/1, merged into main as `369f5a5`.
Local publication main and GitHub main match; publication checkout is clean.

Fixed a client renderer scheduling bug matching the owner's report of invisible newly placed
shulkers and lingering broken ones. A transparency sort could increment the section revision
while a full rebuild was awaiting upload, copying the old block-entity list and making the
new geometry/TE list stale. Track uploaded-data revisions and guard both sort requests and
sort submission (including stale queued requests). Production patch is limited to
`ChunkRenderContainer` and `ChunkBuilder`; no new mixin or config option.

Focused regression `python3 tools/check-chunk-build-revisions.py` failed before and passed
after the fix. Java8 offline build exited0 (18s), manifest116, SHA-256
`4b548255a9eac2722173acc43672a1fea14b4c7469231b356e8465a1c4f1abf4`.
Packaged-mod checks passed in a disposable root/save: Use placement, inventory opening, Attack
removal, visibility after camera movement and removal while moving with stained glass in the
same chunk section. Async workers were enabled; no extra edit/F3+A was used to refresh visibility.
The old intermittent symptom was not reproduced visually; regression proof covers its matching
scheduler mechanism. No performance, universal parity or full-release certification claim.
Source review found no blocking issue. Evidence: `docs/profiles/shulker-render-updates-2026-10-17/`.
Public-checkout build exited0 and all archive entries match tested116 except revision metadata.
Fixed artifact alias: `build/shulker-fix/liteoptimizer-liteloader-1.12.2.litemod`.
Original worlds/instance configs were untouched; temporary client exited cleanly, isolated
desktop stopped, temporary account copy removed.

## Performance comparison charts — 2026-10-18

Added PNG/SVG charts of mean FPS, mean frame time and slowest-1% mean frame time from the three
retained raw CSV pairs (terrain RD16/RD10 and B1 item-frame display lists). Zero-baseline bars,
actual values and unrounded-data percentage gains; clearly individual-feature historical pairs.
README embeds both charts. `tools/plot-benchmarks.py` regenerates them with Python3/matplotlib;
no runtime dependency was added. Rendered and visually inspected both final charts; source/data
review found no blocker. Public Java8 offline build exited0 in8s. No new in-game benchmark run.
Public commit `0ea3041` pushed to GitHub main; artifacts and documentation synchronized here.

## Strengthen benchmark evidence — complete, 2026-10-20

Completed 22 audited runs: three paired whole-mod comparisons each on B1 storage (RD10) and B6
flight (RD16), three isolated structure-index pairs on B4, and one forced-GC pair each for
blockstate storage and baked-quad dedup. The frame runs alternated OFF/ON order, used 30 seconds
warmup plus 60 seconds recording, VSync off, 1920×1080, Java 8u504, Ryzen 5 2600 and GTX 1660
SUPER. Effective configs and renderer settings were audited for every accepted run. Three setup
attempt categories were excluded with reasons: screenshot overlap, stale launcher arguments, and
runner collection failure after recording. No production source changed for these measurements.

Whole-mod median paired effects: B1 FPS +95.0%, mean frame time −48.7%, slowest 1% mean −40.4%;
B6 FPS +52.3%, mean frame time −34.3%, slowest 1% mean −53.5%. B1 paired FPS gains ranged
+61.9% to +95.3%; B6 +48.7% to +54.1%. Sampled integrated-server MSPT was inconsistent on B6
(0.0% median paired change). Structure index showed −0.3% median paired FPS change and −15.6%
MSPT on the single tested fixture; no general server-speed claim. Memory measurements remain
preliminary (one pair each); quad count was 43,690 to 29,214 (33.1% fewer instances), without a
total-heap percentage claim.

Public commit `f901f8d` pushed to GitHub main. The README, four dark charts, raw CSVs, effective
configs, renderer configs, per-run summaries, complete memory class histograms, hardware/runtime
metadata, artifact hash, source revision, and fixture hashes are in `docs/benchmarks/2026-10-20/`.
The clean public checkout build passed with Java 8 and `--offline` (8 seconds). Local private
benchmark workspace remains under `run/eval/strengthened-2026-10-20/`; copied Prism account data
was deleted and the isolated desktop was stopped after the run.

## Current position

**Execution update (2026-10-16): freeze revision 112.** User resumed work and requested less reasoning/repetition. No rebuild for documentation-only edits. Source and normalized archive-entry fingerprints are in `docs/profiles/revision112-release-gates-2026-10-16/`. Reuse passes for unchanged code; the revision-106/107 change was confined to Sodium upload-queue handling, so it does not invalidate revision-101 shared/world/memory cumulative parity. Reviewer audit found recorded gains and boundary evidence for all nine release-default options; do not repeat those benchmarks merely to replace manifest revision numbers. Sodium repeat evidence is revision 95. Remaining work is genuinely missing installed-mod feature coverage and vanilla runtime checks, not another optimization inventory.


Latest verified product build remains revision 112 (Java 8 offline build exit 0; SHA-256 `f4f331643d280fc9c90aaacc538ed4037ecdf79bd12639c1ce8c50a7bd2d3592`). No source changed in the grouped release session. All nine default-on optimizations have retained gain/boundary evidence; reviewer audit and frozen source/payload manifests prevent redundant remeasurement. Rev112 vanilla interaction evidence now covers join/reconnect, ordinary movement, small corrections, placement/breaking, completed trade/saved inventory, potion use, 35,240 cm of walking through 16 X chunk columns, naturally entered portal with clear Nether rendering, F3+T resource reload, and ItemScroller transfers both ways with server-confirmed counts. No reset occurred in this follow-up session; earlier unexplained resets remain a tracked risk.

The grouped B1 session renders the benchmark pose on a disposable copy and additionally passes IsoRenders item/block PNG export (both inspected), VoxelMap minimap terrain, LiteTweaker beacon-beam suppression/restoration, BBOutline outline toggle, MiniHUD beacon range and real spawn chunks, and visible unload-order labels across a 23-chunk relocation. A separate cached-Forge + WorldEdit 6.1.10 fixture now runs on loopback 25568 with a disposable B1 copy; the unchanged LiteLoader client joined and visibly rendered an 80-block WorldEditCUI selection. Evidence: `docs/profiles/revision112-release-gates-2026-10-16/`.

Remaining release proof: Tweakeroo dropped-shulker stacking, particle suppression, tile-entity distance override and remaining item-frame/hologram variations; visibly generated MiniHUD structure boxes; finish B1 matrix certification. UsefulMod frame/shulker previews, Litematica Y30→Y31 layer stepping, Carpet piston preview, Tweakeroo placement/breaking with fast settings and server-confirmed hand restock now pass. Earlier connection resets remain a tracked risk; no reset occurred in this grouped session. TellMe/MaLiLib and Model Gap Fix have pre-existing baseline failures and are not counted as working.

Final Java8 offline build113 exited0 in10s (SHA-256 `115010902e5d6e711fe65a358da63c9c7de2928ebd31da80034c18846f84f7cd`). Exact entry comparison proves only the manifest revision differs from runtime-tested112; source hashes are unchanged. All temporary test settings were restored and the client plus test servers exited cleanly. Build113 is the latest functional untagged candidate; full release certification remains open.

**Owner's policy change (2026-10-14): adopted in project documentation.** Exact pixels and
client-only animation/RNG are no longer universal acceptance gates. Shared/server code still
requires strict parity, including the integrated server. Primary gains are client frame time,
FPS, chunk loading and heap; remote server TPS is outside this client mod's control.
Rendering candidates previously rejected only for client differences can be reconsidered
individually. No removed feature is automatically restored; gain, intended visual quality,
network/action boundary and installed-mod compatibility still need evidence. The current RC2 code has measured tile-entity frustum and camera-visibility optimizations plus
the Sodium renderer. Revision 58 passed 6,000-tick cumulative parity on the stable `test` save. Revision 86 passed 6,000-tick all-off/all-on state parity on that save; see `docs/profiles/revision86-parity-2026-10-15/`. The B1 save fails OFF/OFF determinism and is not used for strict parity. Revision 86 was also launched as the exact reobfuscated release artifact in the disposable full-stack Prism clone against an unmodified Mojang 1.12.2 server. Join/reconnect, server-authoritative movement, client block placement/breaking, villager trading GUI, potion use, Nether travel, and remote terrain streaming passed; no villager trade transaction completed. A save-all during Nether chunk activity produced vanilla chunk-save concurrency errors, so that disposable world save is not certified clean. Evidence and limits: `docs/profiles/vanilla-server-smoke-revision86-2026-10-15/`.

Revision 87 added an optional Tweakeroo Free Camera spectator-flag bridge. Revision 101 was the prior successful Java 8 offline build (SHA-256 `93bcdac9797fa11abbafcd9af3e3216a91201846041e8a3f09df0b0168d8de6e`, manifest revision 101); `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` passed. Its archive differs from revision 100 only in `litemod.json` revision metadata, so revision-100 vanilla-server interaction evidence covers the same code payload. Its archive was installed with matching SHA in the disposable full-stack Prism clone; all entries match revision 99 except the manifest revision. Revision 100 loaded, joined/reconnected to the unmodified vanilla server, recorded 180 cm of movement after score reset, passed server-confirmed Survival stone placement and breaking, completed a Fletcher trade and Swiftness potion use, and accepted a settled 0.0625-block correction reflected in client F3 coordinates without a server movement warning. A separate 10-block fixture teleport emitted `moved too quickly` and is not counted as a correction pass. Evidence: `docs/profiles/revision100-build-2026-10-15/` and `docs/profiles/revision100-vanilla-server-2026-10-15/`. Revision 99 (SHA-256 `5d987cfc17e42982ba786fca3d77f8ef11a5489028e014c52d5cab24c1f15820`) also passed full-stack vanilla join, 876 cm movement, and placement. Revision 98 is byte-identical to revision 99 except manifest revision; it confirmed a settled 0.0625-block correction without movement warning and server-confirmed breaking, while a 0.25-block correction emitted `moved wrongly`. Those checks remain carry-forward only. Evidence: `docs/profiles/revision99-vanilla-server-2026-10-15/` and `docs/profiles/revision98-vanilla-server-2026-10-15/`. Revision 97 evidence below remains historical. Revision 97 is the preceding runtime-tested artifact (SHA-256 `4a80a24fc6beb166872e30dcee58d982d8c6757c877220ad8d7e3fd639f71882`). Its archive differs from the exact revision-96 artifact only in `litemod.json` revision metadata. Revision 97 loaded with the full technical mod stack and joined the unmodified vanilla server; server-side movement reached 1,338 cm, a settled 0.25-block server correction was accepted without `moved wrongly`, and later movement raised the score to 1,950 cm. Evidence: `docs/profiles/revision97-build-2026-10-15/` and `docs/profiles/revision97-vanilla-server-2026-10-15/`. Revision 97 also reopened the disposable B1 copy, previewed/loaded `6gt Crafting Station`, rendered its placement overlay, and displayed its 91-item Material List; the temporary placement was removed and the world saved without block changes. Evidence: `docs/profiles/revision97-build-2026-10-15/README.md`. Revision 96 exact artifact also loaded the disposable B1 scene with the full stack and toggled MiniHUD Info Lines on and off; evidence: `docs/profiles/revision96-fullstack-B1-2026-10-15/`. Revision 95 completed two Sodium B6 pairs; evidence: `docs/profiles/revision95-sodium-B6-rd16-repeat-2026-10-15/` and `docs/profiles/revision95-sodium-B6-rd10-2026-10-15/`. Revision 94 launched with the full stack, loaded disposable `B1-storage-smoke`, joined its integrated server and rendered the scene; evidence: `docs/profiles/revision94-build-2026-10-15/`. Revision 91 passed the focused Tweakeroo bridge check (present/absent), chunk-build revision check, audit self-checks, and full-stack startup using the same code payload. The revision-87 code payload loaded and joined B1; Free Camera movement rendered the scene and toggling it off returned to the player without rejoining. Tweakeroo's main-thread chunk setting loaded enabled and F3+A returned to a rendered B1 scene. Revision 87 passed 6,000-tick all-off/all-on state parity on the stable `test` save; OFF and ON dumps both hash to `ec40cde8cf712ecd6b80a06d98d524bc1dc7ebc141a0c976edf5dbf60877912b`. The tested revision-88 code payload joined a fresh vanilla server, rendered terrain, recorded 876 cm of server-side movement, accepted a server-console position correction, and passed server-verified client stone placement and breaking. Revision 90 completed a vanilla-generated farmer trade in Survival; the server awarded `What a Deal!` and reported `stat.tradedWithVillager = 1`. The same revision-90 session reconnected after a server kick and returned at the saved position. On the exact revision-90 artifact, vanilla-server Survival potion use passed: a server-given Swiftness potion became a glass bottle, Speed appeared client-side, and the vanilla server's `stat.useItem.minecraft.potion` scoreboard reached 1. The revision-91 artifact also passed vanilla-server Survival potion use, natural portal travel (server advancement and `Dimension:-1`), and inventory GUI open/close in the Nether; its potion-use scoreboard remains 1. On revision 92, the vanilla server confirmed a glass bottle in the player inventory after transfer from the hotbar to the main inventory and back; RCON `testfor` matched the item before a successful `save-all` and clean disconnect. Evidence: `docs/profiles/revision92-vanilla-inventory-2026-10-15/`, `docs/profiles/revision93-build-2026-10-15/`, `docs/profiles/revision92-build-2026-10-15/`, `docs/profiles/revision91-build-2026-10-15/`, `docs/profiles/revision91-vanilla-server-2026-10-15/`, `docs/profiles/revision90-vanilla-trade-2026-10-15/`, `docs/profiles/revision90-vanilla-potion-2026-10-15/`, `docs/profiles/revision88-build-2026-10-15/`, `docs/profiles/revision87-tweakeroo-smoke-2026-10-15/`, and `docs/profiles/revision87-parity-2026-10-15/`. Revision 92 and revision 95 B6 profiles show aggregate Sodium gains over repeat runs, but do not isolate individual Sodium candidates. The full installed-mod feature matrix remains open; revision 97 has since passed a dirty-section edit/removal and F3+A rebuild while Tweakeroo main-thread mode was ON.

At render distance 10, the revision-93 repeat parity run exposed two OFF-side outliers in chunk `(-120, 44)` while ON and OFF controls matched at total hash `9fa11a43…`. Revision 101 passed 6,000-tick OFF/OFF and OFF/ON parity at fixed render distance 2; all three dumps are byte-identical at `9494ea25…`. The RD10 chunk-loading variance remains a fixture limitation. Evidence: `docs/profiles/revision101-parity-2026-10-16/README.md`. Revision 100's mixed control and timed-out verbose repeat are retained as historical failure evidence. TellMe 0.6.3 also fails initialization with the same MaLiLib API `NoSuchMethodError` with LiteOptimizer enabled and disabled; it is disabled in the target profile. Evidence: `docs/profiles/revision93-tellme-baseline-2026-10-15/`.

Revision 25, built after fixing the test helper's overlong offline username, joined the vanilla
server and rendered its overworld. On that exact artifact, client movement, block placement and
breaking were confirmed by server-side position/block queries; reconnect, villager trade GUI, and
portal dimension travel also passed. Revision 28 is payload-identical to revision 25
except its manifest version; on the current installed client, an unmodified vanilla server accepted
two authoritative teleports and the client rendered after streaming to X=260.5 across chunk
columns. The server's saved player NBT confirms X=260.5. A vanilla fire placement activated a
portal frame on that payload; the player entered it, received the Nether advancement, and saved
NBT records Dimension -1. A server-provided Swiftness potion was consumed in Survival; client UI
and saved player NBT confirmed the effect and returned bottle. A reversible Soartex Fanver
resource-pack toggle completed both reloads and restored the original selection; the log records
missing-texture warnings, and F3+T remains untested. Most installed-mod feature checks remain open.
The full release checklist is still open. See
`docs/profiles/vanilla-server-smoke-2026-10-14/`.
Detailed acceptance: `docs/methodology.md` §1 scope policy. Historical sections below retain
the original decisions/evidence; this policy supersedes their strict-client-parity criteria.
Policy validation: documentation review found no blockers; `git diff --check` passed;
`JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0. Historical
revision 44 build SHA-256 is `813afcf58f88e2ab79bc8e934fb3acc2e14b2bb3687336a3d3a31d905441aba2`;
it adds `TweakerooCompat` and per-section result revisions in Sodium’s chunk pipeline. Exact
entry comparison against installed revision 38 is in
`docs/profiles/vanilla-server-smoke-2026-10-14/revision44-build-compare.json`. Revision 38
remains the last exact full-stack B1 runtime build; its only archive difference from installed
revision 35 is the manifest revision field. On 2026-10-15, the disposable B1 copy saved and
rejoined at the same player position after Tweakeroo Free Camera was toggled OFF. Current audit,
Sodium option, Tweakeroo reflection, and stale-result filter self-checks pass. The exact revision
37 artifact joined the unmodified Mojang server and persisted same-connection movement in player
NBT; revision 38 then launched with the full stack and joined B1, with its exact build/install
identity and logs in the profile. Exact-rev35 server tests verify same-session correction/chunk
streaming, potion use, and portal activation/dimension travel; Litematica placement overlay and
Tweakeroo Free Camera ON/OFF also passed in B1. ChatCalc's `2+2=` input did not visibly evaluate
after Tab; key delivery remains unverified. Revision 33 block place/break, trade GUI, and saved-
position reconnect remain carry-forward evidence. Exact-build logs, captures, and artifact
comparison are in `docs/profiles/vanilla-server-smoke-2026-10-14/`. The MaLiLib remote-join NPE
still matches the no-LiteOptimizer baseline. F3+T remains unverified; the full installed-mod
feature matrix and release gates remain open. The `forceChunkRendersOnMainThread` bridge drains
dirty section builds synchronously and uses per-section build revisions to reject late async
results, but render-thread safety and dirty-chunk ON/OFF behavior need exact-revision testing. See
`docs/compat/sodium.md` and the Tweakeroo release-check row.

Current working artifact, 2026-10-15: Java 8 offline `build` passed and produced
`build/libs/liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod` (SHA-256
`aa4fecb4a5348500c0ed861eb7ef5d010aede0b9d3581dd2c931791ce54f7a6e`, manifest revision 56);
the exact file is installed in the disposable full-stack clone. Revision 54 added a release-default
gate: only nine measured candidates default on, while legacy `true` values for the other implemented
keys are rewritten off. Revision 55 changes Sodium's `RenderGlobal.setupTerrain` hook from an
overwrite to a cancellable HEAD injection after the static Tweakeroo collision review. Java 8 build,
Config migration self-check, audit, Sodium options, Tweakeroo bridge, chunk build revision, shell
syntax, and diff checks pass on revision 56. Rebuilding for final verification advanced the manifest
to revision 56; the clone archive SHA-256 matches it exactly. A Rev46 clone run passed 600-tick OFF/OFF and
OFF/ON state hashes, but a 6,000-tick attempt did not reach its dump and is not a pass; the run
was stopped after focus/demo-session stalls. The preceding exact-hash artifact loaded with the full installed mod set, but the parity auto-load
crashed in `WorldSettings` because Minecraft returned no `WorldInfo` for the copied B3 fixture.
Its `level.dat` exists and passes gzip integrity. The dev-only preflight now checks `SaveHandler`
metadata before launch so this failure should be diagnosed without a crash; that guard is built but
not runtime-verified. The exact revision-51 hash was launched through the already-running Prism
window into the disposable full-stack clone and reached Minecraft's main menu; LiteLoader reports
LiteOptimizer 0.6.0-rc2 loaded alongside the installed technical mod stack. The GUI launch used
Prism's cached JVM settings, so it lacked the parity system properties and did not test the parity
world-load guard. CLI routing could not target the running Prism process, while a separate
disposable Prism root opened Quick Setup requiring Microsoft sign-in; no sign-in was attempted. A
revision-56 parity attempt through the existing Prism process also confirmed that it launches the
clone with cached arguments, ignoring the temporary `instance.cfg` parity JVM args. A fresh Prism
root stops at its Microsoft-account setup page before offline launch. Both attempts were stopped;
the parity script restored the clone config and removed `_parity`. No state-parity result was
produced. At that point the parity runner only detected Prism's cached-settings behavior and failed
fast. The 2026-10-15 follow-up below replaces that JVM-argument dependency with a clone-local settings
file; the previous attempts remain failures, not parity results.
The parity runner was stopped cleanly; its trap restored clone config and removed `_parity`. A
follow-up GUI attempt confirmed Prism's Launch control stays disabled while the owner's original
Minecraft process is active. The clone's latest log is still the older main-menu run with every
optimization disabled; it does not show revisions 54, 55, or 56. I left the owner's game untouched.
The current revision-56 artifact
therefore has not yet been runtime-launched, and its config migration has not been runtime-verified.
Current-artifact world-load/parity,
cumulative parity, vanilla-server smoke, the full installed-mod feature checklist, and per-optimization
gain evidence remain open.

Follow-up on 2026-10-15: the compatibility scanner now excludes LiteOptimizer itself, matching its
documented foreign-hook purpose. Regeneration reports 639 rows across 16 other mod archives; an exact
member check finds zero collisions across the 12 current `@Overwrite` methods. Java 8 offline build
passes and produced manifest revision 57 (`90625aefe0c4acedbc42be3525a81691f184c3e8c88715b78e06d66190377f59`),
installed byte-for-byte in the disposable parity clone. Four focused checks pass: Sodium option
registration, audit regressions, chunk-build revision filtering, and Tweakeroo bridge presence and
absence. A clone parity launch through a separate Prism root stopped at Quick Setup before Minecraft
started; a second attempt routed through the initialized default root accepted the launch request but
did not start the target Java process before its 60-second startup timeout. Neither produced a parity
result. Both runner paths cleaned up temporary worlds and restored clone settings. The owner game was
not interacted with. The full release gates above remain open.

The exact revision-57 build was subsequently launched through the initialized Prism root with a
temporary missing-key config, so the nine evidence-backed defaults were active. LiteLoader loaded
the full installed technical stack, the log reported no LiteOptimizer mixin-application failure,
and the selected disposable `B1-storage-smoke` world started its integrated server and logged the
player joining at the saved test position. The clone's prior config was restored and the clone was
stopped. This is revision-57 startup/world-join evidence; the follow-up revision-58 parity and
world-join evidence is recorded below. Visible render checks, vanilla-server interaction, and the
full feature checklist remain open. Details:
`docs/profiles/revision57-clone-smoke-2026-10-15/`.
The follow-up revision 58 artifact (SHA-256
`c2232ba38f8b13554eddb4f18f4dd871bf708c808dddd1f6a2452f0651bbb760`) was installed in the same
clone and completed 6,000-tick OFF/ON plus OFF/OFF cumulative parity on the stable `test` save;
all three dumps are byte-identical. A B1 OFF/OFF control is nondeterministic at the final
`World.rand` seed, so B1 remains a render/interaction fixture and is excluded from strict parity.
Revision 60 (SHA-256
`b94e8754c257b037d2674859d5ae7c52e901dddd6598c2e3f163a9d3a86e9243`) passed Java 8 offline
`build`, was installed in the disposable clone, and differs from tested revision 58 only in the
automatic manifest revision number. Revision 60 has not been relaunched. Evidence:
`docs/profiles/revision58-parity-2026-10-15/`. The full-stack launch/join and cumulative parity
are now current-payload evidence; the vanilla-server interaction gate and full feature matrix
remain open.
Revision 70 supersedes the staged revision 61 archive with the same code payload and a newer
manifest revision; its 728 archive entries match revision 61 except for the manifest revision.
The isolated full-stack Prism clone and test identity are available; the earlier account prerequisite
is cleared. Exact revision 102 joined the local unmodified vanilla server and passed movement,
breaking, and inventory transfer, but the placement retry remains unconfirmed. The next vanilla
session must use a changed input/placement method after diagnosing why the `P` binding did not send
a placement; do not repeat the same action without that change. Trade, potion, portal, correction,
and distant chunk streaming also remain due on the current code payload. Group those interactions
with the remaining B1 compatibility features in the next isolated GUI session. The LVT startup path
has passed an isolated Mixin 0.7.5 dev-client smoke; full-stack confirmation remains open. Artifact,
source, parity, and gain audits can proceed independently while GUI checks are pending.
The previous log also contains a Carpet Client/LiteTweaker redirect conflict on `EntityRenderer`;
it is a mod-to-mod redirect collision, not a LiteOptimizer target. A fresh static audit found a
separate current release risk: Sodium's `RenderGlobal.setupTerrain` replacement bypassed Tweakeroo's
two `ViewFrustum` bookkeeping injections. It now uses `@Inject` + cancel, following the mixin
convention, but those internal vanilla hooks still do not execute. The foreign-hook audit now finds
no exact target/member intersections across the remaining 12 LiteOptimizer `@Overwrite` methods;
this static result does not prove callback behavior. The main-thread chunk-build toggle already has a
Sodium bridge; whether the free-camera bookkeeping is redundant with Sodium's stable section renderer
still needs exact-artifact ON/OFF testing. Full feature-matrix checks, exact-current-artifact
world/vanilla-server smoke, cumulative parity, and release approval remain open.

The mod already has its infrastructure and substantial optimization code. Do not restart
foundation work or plan another wholesale Sodium/Lithium/FerriteCore port.

| area | current evidence | next decision |
|---|---|---|
| Foundation | build, deterministic state harness, benchmark recorder, config switches exist | reuse; repair only a demonstrated blocker |
| Lithium | 0.2/0.5 batches implemented; hopper entity lookup reports isolated B3 MSPT 3.75 → 3.22 ms | seek a new hot path only after profiling the current build |
| Sodium | terrain port implemented; item-frame display lists report B1 18.13 → 10.39 ms and pixel comparison 0 differences | validate current artifact, then target remaining measured cost |
| FerriteCore | blockstate storage reports about 14 MB less old-generation heap; quad dedup about 2.2 MB | preserve; additional memory work requires live retained-heap evidence |
| Client development | `tile_entity_frustum` and `camera_visgraph_cache` passed their isolated gain and 6,000-tick parity gates | keep candidates with recorded measurements; finish installed-mod release checks |
| Release | Revision 35 exact archive (`4c59feacd4b90a9d255893431b59617628db6c1460a1c73824e5b770c3f88645`) built and installed identically, loaded with no disabled options, joined/rendered disposable B1, and connected to the unmodified vanilla server. Exact-rev35 checks now include a same-session server correction/chunk transition, server-provided potion consumption with saved effect/bottle, and natural portal activation/travel confirmed by server block checks, advancement, and saved dimension. Litematica loaded `6gt_Crafting_Station` and rendered a translucent placement overlay in B1. Revision 33 movement, placement/breaking, trade, and reconnect remain carry-forward evidence; revision 32 material-list and other mod observations remain carry-forward. Full installed-mod feature matrix remains open. | release gate remains open |

Reported combined culling measurements are not proof of each feature's isolated gain.
The archive/catalog hold exact conditions and limitations. GL43 has known pixel differences
and a poor NVIDIA result: production config now excludes that backend and compact vertices.
There are existing uncommitted source/resource changes: inspect and preserve them before edits.

## Argentum rendering-method review (completed 2026-10-14)

Compared Argentum's feature list against LiteOptimizer's measured profiles. Retained the independently
validated `tile_entity_frustum` and `camera_visgraph_cache` methods. Rejected and removed
`font_char_batch` after two B2 OFF/ON runs showed 5.8% and 1.9% slower average frame times and
worse 1% lows. Entity instancing and occlusion are deferred until the dense-entity benchmark fixture
is corrected. Evidence and source-license decision: `docs/argentum-comparison-2026-10-14.md`.

## Why execution changes

The old version batches mixed implementation, research, release gates, and repeated GUI
attempts. Work continued on compatibility menus after the optimization work had finished.
The catalog's original proposed batches also name optimizations already implemented or rejected.
From now on, the catalog is an inventory, not a requirement to port everything.

Development completion and release approval are separate milestones. Focused compatibility
checks accompany changed hooks; the full installed-mod checklist remains mandatory before
release. A release blocker does not automatically block independent development.

## Completed: artifact and evidence audit

**Status: done** — 2026-10-13. Current ledger: `docs/compat/checklist.md`.
Legacy backend eligibility, combined-gain attribution, and missing artifact identities were
recorded. Offline Java 8 build exited 0. Historical passes were not relabeled as release passes.

## Audit fixes (2026-10-13)

**Status: done.** Existing working tree committed as `7c5e1ee` before edits.
Fixed the six Ponytail audit findings: loaded-mob persistence counts, worker wakeups,
equal block properties, escaped furnace recipe maps, null option enums, negative workers.
Scope: existing mixins/helpers/config and focused regression checks; no new optimizations.
Acceptance: failures reproduced before fixes, focused checks pass, production refmap reviewed,
Java 8 offline build exits 0, focused runtime validation where available. Full release gates
remain separate. Foreign-hook inventory has no EntityLiving persistence/FurnaceRecipes collisions;
StateImplementation's existing Tweakeroo hook remains untouched.
Focused Java/Gson checks pass and fail on the pre-fix sources. Reviewer found no blocking
defect. Full-stack B3 cumulative parity passes at 600 ticks, with startup and shutdown;
final `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exits 0 (21 s).
Final build code/resources match the runtime-tested jar; only the automatic litemod revision
counter differs. Evidence: `docs/profiles/audit-fixes-2026-10-13/README.md`.

## Completed: 1.12 optimization method comparison

**Status: research complete — 2026-10-14.** Compared upstream methods with registered
production mixins and helpers; report: `docs/optimization-comparison-2026-10-14.md`.
Corrected catalog assumptions about paused hidden animations and the Forge-only uploader
complexity fix. Shortlist: VisGraph queue/enum reuse, model-list compaction, GPU animation
frame copies, camera visibility reuse, and independently adapted palette bulk decode. The
isolated VisGraph experiment was rejected after a 1.3% average-frame regression and worse 1%
lows; re-ranked `tile_entity_frustum` under the client-only policy and implemented it. Current
profile, parity and gain gates remain.
Validation: `JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build --offline` exited 0
(7 s); `git diff --check` passed. Product source and instance settings were untouched.
Focused research review found no blocking inaccuracies; no upstream benchmark was run.

## Completed: re-enable measured client block-entity frustum culling

Candidate / config key: off-screen block-entity renderer frustum culling / `tile_entity_frustum`.
Profile evidence and workload: current B1 run (no entities, 20 s warmup, 40 s capture) measured
6.05→4.07 ms average frame time (−32.7%), 23.65→9.54 ms 1% low, and `blockentities`
4.192→2.629 ms. Integrated MSPT was 4.10→3.93 ms and is not claimed as a client-render gain;
max-frame latency was noisy. The isolated VisGraph experiment measured 6.70→6.79 ms average
(+1.3%) and worse 1% low, so that patch was removed. Full conditions and limits:
`docs/profiles/tile-entity-frustum-B1-2026-10-14/README.md`.
Vanilla target and foreign hooks: client-only `RenderGlobal.renderEntities` and
`TileEntityRendererDispatcher.render`; both have documented installed-mod hooks.
Scope/boundary argument: skip only renderer calls for non-global block entities whose
conservative 3×3×3 block box misses the frame frustum; it does not touch ticking, world state,
RNG on the server, packets, or player actions.
Intended client tradeoff: intentionally omit off-screen renderer work; custom renderers extending
beyond the conservative bound may be clipped. Configurable and enabled by default.
Smallest change / excluded work: restore prior bounded helper and two injections only; do not
restore redundant graph-occlusion test or queue rewrite.
Gain metric and off/on configuration: B1 frame time, 1% low, and `blockentities` section,
matching prior camera pose and `--no-entities` controls.
Validation: Java 8 offline build passed; the fresh artifact loaded with the full installed stack
and entered the disposable B1 world. The earlier B1 `--only tile_entity_frustum` parity run
matched, but later B1 OFF/OFF testing showed that save can change the final `World.rand` seed with
the optimizer disabled; that earlier result is not counted as strict parity. Revision 58's
all-defaults OFF/ON and OFF/OFF runs both pass for 6,000 ticks on the stable `test` save. The parity
dumper sorts potion-effect NBT by id/amplifier because the vanilla backing map serializes in
process-dependent order. Existing RenderGlobal/dispatcher hooks were reviewed; reviewer found no
code-level blockers. The isolated B1 gain is recorded in the profile evidence.
The dedicated vanilla-server smoke has older-candidate passes for join/reconnect, movement,
inventory receipt, block place/break, chunk streaming, villager trading, and natural portal travel.
Revision 25's exact artifact joined and rendered on vanilla and passed forward movement, direct
client block placement, block breaking, successful-join reconnect, villager trading, and a portal
dimension transition, with server-side position/block checks and GUI evidence. On its byte-identical
revision 28 payload, server correction, chunk streaming across 16 chunk columns, vanilla fire-based
portal activation, Nether travel, and survival potion use also passed. The full installed-mod
feature checklist remains pending. Matched clean world-exit controls with and without LAN hosting
did not reproduce one earlier unarchived packet-drain NPE; the same startup config-selection NPE
appears in the no-mod baseline. This is recorded as non-reproduction, not a closed root cause.
This candidate adds no packet or player-action hooks.

## Completed: reuse camera-section visibility flood fill

Candidate / key: `camera_visgraph_cache`, chosen from the researched shortlist after the current
B6 moving-camera profile showed `chunk_graph_rebuild` at 0.090 ms per frame (11% of measured
profiler sections). Static B1 showed only 0.036 ms in terrain setup, so B6 was the selection
workload.
Scope: cache only the six exits returned by the vanilla `VisGraph` for the exact camera block;
the existing chunk graph BFS and neighbor order are unchanged. Invalidate on camera-section
rebuild scheduling, section load/unload, and completed visibility updates. A hit returns a fresh
set because the caller removes one facing for view-direction filtering.
Validation: Java 8 offline build passed. Three 20 s warmup / 40 s B6 flight pairs measured average
frame gains of 2.1%, 3.9%, and 3.4%; median 3.4%. Median 1% low gain was 7.7%, while
`chunk_graph_rebuild` fell 48–51%. The latest pair's gameplay pixels were identical; only 161 HUD
pixels changed. The historical 6,000-tick result (hash
`a64f0f37052903fe1188832dd296e3d866e1ac40f14e0a6abd5be26514f32ab8`) has no saved fixture
identity, so the current parity claim is revision 58's cumulative OFF/ON and OFF/OFF pass on the
stable `test` save. Evidence and caveats:
`docs/profiles/camera-visgraph-cache-B6-2026-10-14/README.md` and
`docs/profiles/revision58-parity-2026-10-15/README.md`.

## Execution queue

## Next optimization investigations — in progress 2026-10-24

Priority is culling first, targeted GUI rendering second, and a network profile probe only if the
first two are done or a current profile points at packet work. These are investigations, not a
commitment to ship each idea. New rendering switches stay off by default until profiled.

### 1. MoreCulling-inspired entity and block-entity culling — high priority

Use MoreCulling as an ideas-only reference; do not copy its GPL-3.0 code into this LGPL-3.0 mod.
Audit Vintagium's `RenderGlobal` entity and block-entity dispatch before choosing hooks. Existing
`tile_entity_frustum` is a separate, measured frustum optimization; existing entity culling and TE
occlusion evaluations found no gain in their tested workload. Reuse their evidence and reopen only
the distinct occlusion-culling path, with a current workload/profile establishing headroom.

Plan stages: (a) trace vanilla/Vintagium dispatch and current foreign mixins; (b) choose a
conservative visibility rule and define explicit bypasses for registered/unknown TESRs, selection
boxes and overlays, ridden/riding entities, glowing/name-tagged entities, and Litematica/MiniHUD
render paths; (c) implement one isolated option only after that audit; (d) keep it off by default
until full-stack compatibility, visibility/interaction checks, and isolated profiling pass. It may
be enabled by default only after repeatable gain is shown without hiding technical overlays or
changing simulation, ticking, packets, or player actions. No generic culling framework.

**Execution update — 2026-10-24:** `entity_culling` remains experimental and default-off. The
isolated MultiMC full-stack smoke retained Litematica, MiniHUD, selection, and outline overlays. An
open/glass B4 pair did not cull an entity and was slower with the option on, so its timings are
excluded as a gain claim. A separate sealed-section fixture at a fixed pose did suppress one of
three visible entities (`E: 2/3` off, `E: 1/3` on); screenshots and conditions are in
`docs/profiles/entity-culling-B4-2026-10-24/README.md`. The existing `tile_entity_frustum` path has
prior B1 draw-cost and frame measurements; the newer `tile_entity_occlusion` dispatcher hook has
not passed a runtime suppression check and stays default-off. Do not claim gain for either new
occlusion path until a representative measured workload passes.

### 2. ImmediatelyFast-inspired GUI, item, and text paths — targeted

Restrict investigation to GUI/HUD, item-model, and text rendering. Terrain/chunk rendering is out of
scope because Vintagium already owns that pipeline. First profile GUI-heavy B2 and the B1 item-frame
scene to identify a hot path. Existing `render/font_renderer_batching` was rejected after two
slower measurements; do not repeat glyph batching under a new name. A distinct glyph lookup/cache
or GUI item-model batching candidate must have a separate hypothesis, compatibility audit, and
isolated before/after result. Reuse `render/model_display_lists` where its item-frame-only cache
already covers the work. Keep any new option off by default until measured and visually validated.

**Execution update — 2026-10-24:** the GUI-only display-list candidate was implemented and tested
in the isolated full-stack client, then removed after the matched creative-inventory OFF/ON pair
showed worse results with it on: average frame time +7.8%, slowest 1% +20.3%. Terrain, text, and
the existing item-frame display-list path were not changed by the candidate removal. Glyph caching
remains deferred because prior measurements were slower. Raw captures and the enabled GUI screenshot
are in `docs/profiles/gui-item-display-lists-B4-2026-10-24.md`.
Existing JFR evidence had only four inclusive MaLiLib glyph/text samples in 25 seconds, and prior
font batching was slower, so glyph caching remains deferred. See
`docs/profiles/gui-item-display-lists-B4-2026-10-24.md`.

### 3. Network micro-optimization — probe only, very low priority

Do not port Krypton wholesale or alter the Netty pipeline. First capture JFR and a focused benchmark
of packet encode/decode allocation and CPU cost during ordinary vanilla-server play. If packet
processing is not a material client bottleneck, close the probe with no code. If it is, consider
only a narrowly scoped allocation reduction or lookup cache after proving byte-identical output
(same packet IDs, serialized field order and contents), unchanged player-action timing, and a clean
connection/play smoke against an unmodified vanilla server. No networking optimization may ship on
performance claims alone.

**Execution update — 2026-10-24:** the modded MultiMC client connected to the disposable vanilla
1.12.2 loopback fixture, received world data, and completed active-play movement/chat checks. A
60-second JFR was dominated by the installed RawInput mouse poller (5,747 self samples); Sodium
chunk graph/rendering was the next sampled work, and Netty/packet encoding did not appear as a
hotspot. No network code or protocol behavior changed. Close the probe without a network
optimization; reopen only if a repeatable profile places packet processing on the critical path.
See `docs/profiles/network-probe-vanilla-2026-10-24.md`.

References: [MoreCulling](https://github.com/FxMorin/MoreCulling),
[ImmediatelyFast](https://github.com/RaphiMC/ImmediatelyFast),
[Krypton](https://github.com/astei/krypton). No product source changed by this plan entry.

| order | task | deliverable and stop condition |
|---|---|---|
| 1 | culling hook/workload audit | current hook map, whitelist/bypass rules, and profile target; stop if no safe, measurable occlusion path |
| 2 | targeted culling candidate | one gated implementation, off by default; retain only if full-stack visibility and isolated gain pass |
| 3 | GUI/text/item profile | one evidence-backed candidate, or close if the old font-batching result still represents the workload |
| 4 | packet profile probe | JFR/benchmark report only; implement only if packet work is material and wire/action proof is available |

Current coding slice: wire Sodium's existing visible-section predicate into Vintagium's entity
loop under `entity_culling` (default off), preserving outline and ridden-entity paths. The prior
B1 run suppressed moving entities and the old profile config key was not recognized by the
release artifact; it is not valid gain evidence for this implementation. Current B4 client-section
profiling and full-stack visibility checks remain required before enabling it by default. The
isolated desktop is stopped while the configured Prism instance is already running; do not touch
that process or use host GUI control. The separate TE occlusion path remains deferred: the current
visible-TE collector makes its ordinary graph check redundant; custom TESR-safe occlusion needs a
separate measured design.

| order | task | deliverable and stop condition |
|---|---|---|
| 1 | current artifact/evidence reconciliation | done: ledger, explicit gaps, build |
| 1a | enforce production parity settings | done: normalized load/save, unsafe controls removed, focused check |
| 2 | select next bottleneck | completed for this review: current B3-hoppers worker/render profile; no further candidate passed the value and correctness gates |
| 3 | implement one optimization | completed for this review: existing measured `tile_entity_frustum` and `camera_visgraph_cache` remain the accepted candidates; palette decode, primitive inner flood fill, model-list compaction, and GPU animation copies are deferred with evidence |
| 4 | assemble release candidate | cumulative regression checks and one scheduled full compatibility session; resolve failures, then release approval |

Repeat tasks 2–3 only when a useful candidate exists. Versions describe delivered behavior;
they do not impose quotas of render/world/memory features. Never tag or publish without authorization.

Current candidate review: the exact built and installed artifact passed the Java 8 offline build,
focused config/audit regression checks, and a 30-second B3 runtime/profile capture (319 FPS average,
3.13 ms average frame, 5.02 ms average integrated MSPT). `WorldSlice.copyBlocks` was 10/237 worker
self samples, while `chunk_update` was 0.203 ms/frame; its existing loop already matches vanilla's
contiguous storage index. No new production change met the remaining evidence gate. Detailed current
profile: `docs/profiles/B3-hoppers-on.top.txt`; remaining shortlist decisions:
`docs/optimization-comparison-2026-10-14.md`.

## Candidate evaluation: chunk-slice decode

Deferred after a fresh B3-hoppers profile on 2026-10-14. `WorldSlice.copyBlocks` appeared in 28
of 267 sampled chunk-worker self frames, but the existing loops already match the storage order
(`y << 8 | z << 4 | x`): X is the innermost coordinate and both arrays are traversed contiguously.
Changing loop order would reduce locality. A bulk palette decoder needs direct storage access and
must first resolve the live `ExtendedBlockStorage` references retained by `ClonedChunkSection`;
the current evidence does not justify that threading-sensitive rewrite. Render-thread
`chunk_update` averaged 0.218 ms/frame on B3, so this candidate remains deferred until a safe
snapshot implementation and a focused chunk-build measurement are available.

## Completed: production settings and culling evaluation

- **Parity settings: done.** Unsafe saved flags normalized on load/save; their selectors removed.
  Real-Gson regression passes and fails on pre-fix source. Java 8 build exited 0; runtime full
  compatibility remains owed. Details: `docs/compat/checklist.md`.
- **Entity culling evaluation: done.** No gain at the initial B1 pose with moving entities
  suppressed (3.24→3.31 ms). General parity/gain remain unproven; release-ineligible.
  Evidence: `docs/profiles/entity-culling-B1-2026-10-13/README.md`.
- **TE culling evaluation: done.** Frustum isolated average-frame gains 18.3%/17.8%, exact
  parity unresolved (9 final pixels; earlier unchanged control: 19 pixels). Ordinary TE occlusion
  redundant, no established gain; release-ineligible. No product code changed in evaluation.
  Java 8 build exited 0 in 6 s; restoration/diff checks and evidence review passed.
  Evidence: `docs/profiles/te-culling-B1-2026-10-13/README.md`.

## Completed: conservative 0.6.0-rc2 candidate

**Status: candidate complete; full release validation remains open.** The built jar is installed
in the test instance and matches SHA-256
`a26226f6ec8f96fa9174378feab1efd048cca0e2ae074cd2a3d0f11562e8519b`. LiteLoader startup
records version `0.6.0-rc2`, all options loaded, and a connection to the disposable Carpet server:
`docs/profiles/0.6.0-rc2-2026-10-13/final-startup.log`. The prior runtime-tested payload differs
only in two compiled version-string constants; proof is `version-payload-check.json`.

Candidate acceptance passed: Java 8 offline build; focused real-Gson settings and bench-config
checks; B3 cumulative state parity; isolated B1 display-list gain with exact off/on and off/off
pixel captures; and focused full-stack compatibility smoke. The exact conditions and boundaries
are recorded in `docs/profiles/0.6.0-rc2-2026-10-13/README.md` and
`docs/compat/checklist.md`. Original shared mod settings were restored after the final launch.

Do not tag or call this a full release yet. The full compatibility table remains open, and the
malilib direct-reconnect NPE has no matched no-LiteOptimizer comparison. Continue with one grouped
release-check session after preparing the remaining fixtures; if the reconnect issue reproduces
without LiteOptimizer, close it as baseline evidence, otherwise investigate the responsible hook.

## Selecting optimization work

- Start with a current profile, not an upstream feature list. Reuse recent profiles when code,
  workload, hardware, and settings still match. A method name in a sample is a lead, not a proof.
- Ready starting workloads: B1 storage for rendering/heap, B3 hoppers for integrated-server cost.
  Use B4 only for observed entity/spawning cost. B5 requires a running quarry; B6 requires a
  repeatable flight workload. Missing fixtures block those candidates, not the whole project.
- Inspect the top remaining costs in the optimized build. B3 previously sampled
  `WorldSlice.copyBlocks`, flat quad rendering, and AO bounds filling; confirm current importance
  before changing them. AO reuse and chunk buffers are already in the Sodium port.
- Rank supported candidates by expected user-visible gain relative to implementation effort,
  parity risk, invalidation complexity, and foreign mixin collisions. Choose the narrowest win.
- Keep hopper caching, AI caches, tick-scheduler rewrites, light-cache tuning, and global enum
  replacement deferred unless their specific costs are measured. Server RNG/update-order
  changes remain rejected. Client-only matrix math, culling, animation and GPU formats may be
  reevaluated with explicit visual tradeoffs; previous rejection does not establish eligibility.
- No required percentage win is invented here. The improvement must be repeatable beyond the
  observed run noise and worth the added maintenance. An inconclusive result is not a gain.

## Work contract for each candidate

Before coding, write a short task card in this file:

```
Candidate / config key:
Profile evidence and workload:
Vanilla target and foreign hooks:
Scope/boundary argument (client-only or shared/server; state, order, RNG, networking/actions):
Intended client tradeoff / config switch and default (or identical-output claim):
Smallest change / excluded work:
Gain metric and off/on configuration (include dependencies):
Focused compatibility checks:
Acceptance / blocker / stopping condition:
```

Use existing helpers and upstream-compatible code. One candidate per task/commit; a tightly
coupled pair needs an explicit reason and combined attribution. No speculative infrastructure.

Verification order:
1. Small compile/helper check while editing; build once the change is coherent.
2. Relevant shared/server parity run or client visual/interaction validation; controlled render
   captures require zero differences only for identical-output claims. Screenshots cannot prove
   behavior for entity types they suppress; `--no-entities` retains hanging entities, so it can
   cover item frames/paintings but not moving entities. Shared simulation retains float/order parity.
3. Isolated off/on gain measurement with dependencies held constant where the harness permits.
   Reuse existing runners. A combined benchmark supports a combined claim only.
4. Focused installed-mod smoke for the actual touched hooks, then final offline build if code changed.

Record one concise result with artifact paths, settings, numbers, and limitations. Failed server
parity or unintended client regressions mean fix or reject; compatibility failures need investigation; unmeasurable candidates are deferred
or removed. Additional runs require a failure, noise, or changed conditions to justify them.

## Release validation without endless GUI work

- Full `docs/compat/checklist.md` remains a release gate with the installed mod set at B1.
  During development run only affected-hook checks and startup/world join when applicable.
- Reuse passed observations within a release candidate when the artifact, relevant hooks,
  installed mod versions, fixture, and settings are unchanged. Record that identity. Old evidence
  with an unknown artifact is supporting history, not a definitive release pass.
- Group pending checks into one session with prepared schematics, placements, and expected results.
  Use a disposable copy for paste, placement, inventory, lighting changes, and destructive tests;
  preserve original worlds and restore temporary configs. Paste is a required check to prepare,
  not a feature to leave perpetually untested because the original world must be preserved.
- GUI control stays in isolated headless Sway/WayVNC. Never use the owner's desktop/input.
- After two unsuccessful attempts at the same GUI action or 20 minutes without new evidence,
  stop that check, record the exact blocker, and switch to independent work. These are execution
  budgets, not exemptions from acceptance. Do not repeat an unchanged blocker next session.
- Investigate installed-mod conflict warnings against a baseline without LiteOptimizer before
  assigning blame. Baseline conflicts remain visible; do not claim blanket compatibility.
- Server-dependent Carpet checks need a suitable test server/fixture. Record the missing prerequisite
  and schedule the check; menu version `null` is not a server-hook pass.

Release ready means: successful build; each included optimization has boundary/gain evidence
and relevant server parity or client tradeoff validation; required cumulative regression checks
pass; vanilla-server play and the full compatibility checklist pass; no unresolved
release-blocking failure. Development may proceed while a release prerequisite is outstanding.

## Agent session rules

Read this plan, the relevant catalog row, and only necessary code/evidence. Do not reread the
full history or catalog on every “continue”. Continue the named task until its stop condition.
Update its status once; keep at most one task in progress. Put detailed results in the relevant
catalog/profile/compat file, not a growing chronological transcript here.

When blocked, write: exact prerequisite, evidence, next action, and whether independent work
can proceed. A “continue” must produce a new decision, implementation, or proof; it must not
merely repeat the same GUI attempt and append another status paragraph.

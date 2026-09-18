# PLAN.md

Execution tracker. Project context is in `AGENTS.md`; method and rules of admission in
`docs/methodology.md`; the list of optimizations in `docs/catalog.md`. Agents read this at
session start and after every compaction (`.claude/hooks/on-compact.sh` re-injects the head
of this file).

Status legend: `pending` / `in progress` / `blocked (<why>)` / `done (<sha>)` / `dropped (<why>)`.
Exactly one task should be `in progress` at a time.

Roadmap — three pillars in one `.litemod`, each a package with its own config keys:

| pillar | package | model | what it is on 1.12.2 | size |
|--------|---------|-------|----------------------|------|
| **Sodium** | `render/` | Sodium 0.4 → **Vintagium** (Asek3/sodium-1.12, LGPL-3, 16.6k LOC, Forge) | replace the terrain render pipeline (`RenderGlobal.setupTerrain`/`renderBlockLayer`/`updateChunks`, `RenderChunk`, `ChunkRenderDispatcher`) with Sodium's: chunk-section graph + occlusion culling, worker-thread meshing into compact vertex formats, one VAO/VBO region per chunk column, `MultiDraw`/oneshot GL backends, light/AO cache, biome-color cache, particle/entity culling, fast translucency sort | L (port + de-Forge + LiteLoader compat) |
| **Lithium** | `world/`, `ai/` | Lithium (ideas) + **carpetmod112** (code) + TISCarpet113 (code) + audit rows | game-logic optimizations that leave state bit-identical: collisions, entity lookups, hoppers, tick scheduler, explosions, AI goal/pathing, block-entity ticking, chunk save | M×many |
| **FerriteCore** | `memory/` | FerriteCore (ideas) + **FoamFix/VintageFix** (1.12.2 code, LGPL-3) | blockstate property tables, `BakedQuad`/model dedup, sprite frame release, NBT canonicalization, `EntityDataManager` arrays, `ItemStack.isEmpty` | M |

Versions:

- **0.1 Foundation** — config plugin (per-optimization toggles, per-pillar master switches), parity
  harness, BenchRecorder, benchmark worlds; the compat matrix against the instance's mod set.
- **0.2 Lithium I** — world batch 1 + the S-effort memory rows (`itemstack_is_empty`,
  `entity_data_manager`, `nbt_tag_pooling`). First release worth installing: MSPT and autosave.
- **0.3 Sodium I** — Vintagium port, oneshot backend, vanilla-equivalent pixels; render
  batch 1 (particles, textures, model matrix, entity culling) lands alongside because Sodium's
  world renderer owns those hooks.
- **0.4 FerriteCore** — blockstate tables, quad/model dedup, sprite frames. Heap numbers.
- **0.5 Lithium II** — world batch 2 (hoppers, tick scheduler, explosions), AI batch.
- **0.6 Sodium II** — multidraw backend, chunk light cache tuning, entity/TE occlusion culling,
  options screen (or MiniHUD-less config). Then whatever the profiles still show.

Ordering reasons: Foundation first because nothing ships without the harness. Lithium I before
Sodium I because it is small, low-risk and proves the pipeline; Sodium I is the biggest single
piece of work and its risk is *compatibility* (it overwrites `RenderGlobal` methods that
Litematica, MiniHUD, Carpet Client, Tweakeroo, Newlight and VoxelMap inject into), so it gets its
own compat task before any port code lands.

Decisions already made (do not reopen without new data):

- **License: LGPL-3.0** (owner confirmed 2026-09-17). Vintagium, carpetmod112, FoamFix,
  VintageFix, Lithium, TISCM code may be ported with attribution; MIT/Unlicense sources too.
  Sodium ≥ 0.5 (PolyForm), Meldexun's repos, TexFix, tr7zw EntityCulling: ideas only.
- No runtime dependency (no malilib). Config = flat JSON + `IMixinConfigPlugin.shouldApplyMixin`
  gating per optimization package. No GUI in 0.x (Sodium's options screen is a 0.6 item).
- **Mixin 0.7.5** (bundled in `liteloader-1.12.2-SNAPSHOT.jar`; verified from
  `MixinEnvironment.class`). Vintagium declares `minVersion 0.8` and uses `@Dynamic` once and
  `overwrites.conformVisibility`; everything else it uses (`@Inject/@Redirect/@Overwrite/@Shadow/
  @Accessor/@ModifyVariable`) exists in 0.7.5. Port down; do **not** try to ship a newer Mixin
  (LiteLoader's tweaker loads its own first).
- **Compatibility with the owner's full instance mod set is a release gate** (18 litemods, see
  `AGENTS.md` → Compatibility invariants, `docs/compat/checklist.md`). Every task's acceptance
  includes "loads with the full set, checklist passes at the B1 pose". `tools/compat-scan.py`
  → `docs/compat/instance-mixins.md` (638 foreign injection rows) is consulted before every mixin.
- Lighting is Newlight's job. We do not touch it.
- Nothing ships without a parity proof and a measured number. Parity failure = revert.
- Mod id `liteoptimizer` / package `dev.froyln.liteoptimizer` are placeholders picked by the
  agent; renaming = `gradle.properties` + one package move.

---

## Task: Research — verify and complete `docs/catalog.md`

**Status:** done (uncommitted) — profiles deferred, not blocking

Turn the seed catalog into a verified, prioritized list. **Read only — no mixins, no code in
`src/` except throwaway probes that are deleted after.** Deliverable is the catalog plus the
baseline profiles that justify the priorities. Two inputs: the upstream mods' *source* (what
they optimize and how) and the 1.12.2 *decompiled vanilla* (whether that code exists here and
where the time actually goes).

### Files to read

- `docs/methodology.md` — the admission rules every row must satisfy.
- `docs/vanilla-audit.md` — what the vanilla hot paths do, line by line; the source of every **audit** row.
- `docs/catalog.md` — the seed; every row is a hypothesis.
- `~/.gradle/caches/minecraft/net/minecraft/minecraft/1.12.2/stable/39/minecraftSrc-1.12.2.jar`
  — decompiled vanilla, after `./gradlew setupDecompWorkspace`. Extract to the scratchpad.
- Installed litemods in `~/.local/share/PrismLauncher/instances/1.12.2 test ai/minecraft/mods/1.12.2/`
  — `unzip -l` each; the class list tells which vanilla classes they already mixin into.

### Steps

1. **References.** `git clone --depth 1` each upstream from the table in `docs/catalog.md` into
   `~/Dev/references/<name>/` (Lithium, Sodium **0.5.x branch**, FerriteCore, FoamFix 1.12.2
   branch, VanillaFix, BetterFps, EntityCulling 1.12, Nothirium, Vintagium or Celeritas,
   UniversalTweaks, LoliASM, ParticleCulling, carpetmod112). Record each repo's license file and
   the commit hash in the table. Missing/renamed repos: note it and move on.
2. **Enumerate upstream.** For Lithium: list every key in its mixin config
   (`lithium.properties` / `LithiumConfig` defaults + the README table) — that is the checklist.
   For Sodium 0.5: the `mixin/` package tree. For each 1.12.2 mod: its mixin config json +
   its own config's toggle list. Produce one row per distinct optimization; merge duplicates
   across mods into one row with all sources listed.
3. **Map to 1.12.2.** For every row, find the vanilla 1.12.2 class/method in the decompiled
   source and fill `applies` with the exact names. Mark `no` when the code was introduced
   after 1.12 (POI, `ChunkSection` ticking counts, new light engine, `EntityTrackingSection`).
   Mark `partial` when only some of it exists.
4. **Collision check.** For every target class, grep the installed litemods' class lists and
   mixin json (`unzip -p <mod> mixins.*.json`). Note collisions with Newlight, Carpet Client,
   Tweakeroo, Litematica, MiniHUD, malilib in the row. `@Overwrite` collisions = blocker.
5. **Parity classification.** Fill `parity` for every row with a one-line reason using the
   checklist in `docs/methodology.md` §1. Cross-check against carpetmod112's opt-in rules:
   anything Carpet gates behind a rule is `high` unless we can show why not.
6. **Baseline profiles.** Build the six benchmark worlds (`docs/methodology.md` §2 — reuse the
   schematics in the instance's `schematics/` dir for B1). For each: 60 s JFR (`jcmd`), one
   `/debug` server profile, a `shift+F3` pie screenshot, heap histogram. Save under
   `docs/profiles/<world>-baseline.*` (JFR files gitignored if > 5 MB; keep the `jfr print`
   top-50 summaries as text). Write the top-10 hot methods per world into `docs/profiles/README.md`.
7. **Prioritize.** Fill `gain` from the profiles (which world's hot list contains the row's
   target), `effort` from the size of the vanilla method and the upstream diff. Sort each
   section by priority. Anything whose target never appears in any profile drops to
   `candidate (not hot)`.
8. **Propose batches.** At the bottom of `docs/catalog.md` write the concrete task list for
   roadmap steps 3–7: 3–5 rows per batch, in order. Then create the Infrastructure task below
   with real acceptance numbers (what "the harness works" means on which world).

### Acceptance

- Every row of `docs/catalog.md` has `applies` (with class names), `parity` (with reason),
  `gain`, `effort`, a source license, and no `verify` left in the sources table. **(done except
  `gain` = `pending profiles`)**
- `docs/profiles/README.md` lists top-10 hot methods for all six benchmark worlds with the
  measurement conditions (pose, RD, ticks).
- `docs/catalog.md` ends with the proposed batch list; each batch names its benchmark world
  and expected metric.
- `src/` unchanged (git diff empty under `src/`); `./gradlew build` exits 0.

### Notes / findings

- Scaffold builds: `liteoptimizer-liteloader-1.12.2-0.1.0.litemod` produced with system JDK 8
  (no foreign litemod to deobfuscate, so the SchematicPreview ZipException workaround is not
  needed). `setupDecompWorkspace` done: `minecraftSrc-1.12.2-sources.jar` (2168 files) in the
  gradle cache; the asset-download step after it fails on this network (irrelevant).
- Parity harness feasibility confirmed: 1.12.2 has ~20 server-relevant `new Random()` sites
  (listed in `docs/methodology.md` §1), all reachable by `@Redirect`; `World.updateLCG` is the
  sneaky one.
- 2026-09-17, steps 1–5 and 8 done (see `docs/catalog.md`): 16 upstreams cloned to
  `~/Dev/references/` with commits + licenses; Lithium's 170 keys, Sodium 0.5's mixin tree,
  FoamFix/VanillaFix/BetterFps/LoliASM/UniversalTweaks configs and Carpet 1.12's rule list
  enumerated; every row mapped to 1.12.2 classes (hot ones read in the decompiled source);
  installed-litemod collisions listed per row; batches proposed.
- Biggest find: **carpetmod112** — 1.12.2-exact, LGPL-3, community-vetted optimizations
  (`optimizedTileEntityRemoval`, masa's `blockCollisionsOptimization`, the MASA subset of
  `optimizedTNT`, falling-block/TNT movement early-outs). Its rule categories double as the
  community's own parity verdicts. Sodium 0.5 branch is PolyForm Shield → ideas only; Meldexun's
  repos have no license → ideas only; Vintagium/LoliASM moved (LoliASM found at LoliKingdom/).
- **Open decision for the owner:** license this mod LGPL-3.0 (lets us port carpet/FoamFix/
  Lithium code). Recommended.
- Blocked on a person in-game: steps 6–7 (baseline profiles). Procedure and the save→benchmark
  mapping are in `docs/profiles/README.md`; `tools/profile.sh` records JFR + prints hot frames.
  The instance's bundled JRE is Oracle 8u202 (JFR is a commercial flag there) — point
  `instance.cfg` `JavaPath` at `/usr/lib/jvm/java-8-openjdk/bin/java` first.
- Round 2 (same day, web + Modrinth API + UsefulMods list + MC-Optimization-Guide): 13 more
  upstreams cloned (Vintagium = Sodium on 1.12.2 under LGPL-3, VintageFix, Valkyrie (MIT),
  better-biome-blend (Unlicense), StellarCore, FermiumASM, TISCarpet113 (technical-community
  Lithium ports for 1.13), PortalSuperCache, FastFurnace/FastWorkbench (MIT), Relictium,
  Sledgehammer, Opticraft, MC-Optimization-Guide). Catalog now 57 candidate rows / 10 rejected.
  New high-value rows: `world/pending_ticks_chunk_lookup` (autosave stutter — every chunk save
  scans the whole tick list, verified), `render/entity_model_matrix` (−90% GL calls on mobs, MIT),
  `render/biome_color_cache`, `render/ao_face_allocation`, `memory/entity_data_manager` (lock per
  entity-data read, verified), `world/entity_fluid_checks`, `world/furnace_recipe_cache`.
- Round 3 — vanilla source audit (`docs/vanilla-audit.md`, 30 findings A1–H4, method by method with
  line numbers): what the code does per entity tick, per TE tick, per block change, per chunk save,
  per frame, per chunk rebuild, per client tick. Adds 8 catalog rows found by reading, not by mod
  lists: `render/camera_visgraph_cache` (4096 block reads + flood fill per frame while moving),
  `render/entity_iteration_per_chunk` (17k chunk hash lookups per frame), `render/chunk_light_cache`,
  `render/client_visible_chunks`, `memory/itemstack_is_empty` (map lookup per `isEmpty()`),
  `world/equipment_change_check`, `world/cramming_predicate_cache`, `world/entity_type_counts`
  (4 full entity-list scans per tick for spawning). Catalog: 65 candidates.
- Vanilla facts verified while mapping (in the catalog rows): no per-TE frustum test, no
  particle culling at all, all animated sprites uploaded every frame, `EntityItem` full stacks
  can never merge, `World.updateEntities` uses O(n·m) `removeAll`, `randomTickOptimization`
  shifts the random-tick LCG (rejected).

---

## Task: 0.1 Foundation — config plugin, BenchRecorder, parity harness, benchmark worlds

**Status:** done (7c3c18d, 383bdee, c3b840d) — bench noise check pending a second off run

Everything later tasks need to prove their claims. **No optimizations in this task.** Each step
is one commit; the branch is `main` (solo project, owner said commits are fine when asked).

### Files to read

- `docs/methodology.md` §1–2 — the spec for harness and measurement.
- `~/Dev/SchematicPreview/AGENTS.md` → Gotchas (mixin package rules, accessor casts, GL state).
- Mixin 0.7.5 `IMixinConfigPlugin` (in `org.spongepowered:mixin:0.7.4-SNAPSHOT` on the gradle
  classpath — same interface).
- `docs/compat/instance-mixins.md` — before every mixin in the harness (`World`, `Entity`,
  `MinecraftServer`, `Explosion`, `MathHelper`, `Item` all have foreign rows; ours are
  `@Redirect`s on `new Random()` inside constructors/static init, which no other mod touches — verify).

### Design (decided; change only with a reason written here)

```
src/main/java/dev/froyln/liteoptimizer/
├── LiteModLiteOptimizer.java        # LiteLoader entry; registers dev tools when -Dliteoptimizer.dev=true
├── config/
│   ├── Config.java                  # load/save <liteloader config dir>/liteoptimizer.json; static lookups
│   ├── Option.java                  # enum of every optimization key: pillar, name, default, description
│   └── MixinPlugin.java             # IMixinConfigPlugin: package dev.froyln.liteoptimizer.mixin.<pillar>.<name>.* → Option
├── dev/
│   ├── BenchRecorder.java           # Tickable + keybind: frame + integrated-server tick times → run/bench/<label>.csv
│   └── ParityDump.java              # tick counter + world-state hash → run/parity/<label>.txt, then shutdown
└── mixin/
    └── dev/parity/                  # seeded Randoms (only applied when -Dliteoptimizer.parity=<seed>)
        ├── WorldMixin.java          # rand, updateLCG
        ├── EntityMixin.java         # rand
        ├── ExplosionMixin.java, MinecraftServerMixin.java, MathHelperMixin.java, ItemMixin.java,
        │   InventoryHelperMixin.java, BlockDispenserMixin.java, TileEntityDispenserMixin.java,
        │   TileEntityEnchantmentTableMixin.java, LootMixins…, MapGenBaseMixin.java, NoiseGeneratorMixins
        └── MinecraftServerTickMixin.java   # counts ticks after world load, triggers ParityDump at N
src/main/resources/
├── mixins.liteoptimizer.json        # plugin + every mixin class, grouped by pillar/name
└── liteoptimizer.default.json       # generated at build? no — Config writes all keys on first run
tools/
├── parity.sh <world-dir> <ticks> [--only <name>]   # copy world ×2, run all-off then all-on, diff dumps
├── bench-summary.py <a.csv> [<b.csv>]             # avg / 1% low / max, delta
└── (existing) test-in-game.sh, profile.sh, compat-scan.py
```

- **Config format:** `{"render": true, "world": true, "ai": true, "memory": true, "<name>": true, …}`
  — pillar master switches + one key per optimization; missing key = default; unknown key =
  warning; malformed file = defaults + warning, file rewritten with all known keys and a
  `_comment` per key from `Option.description`. Read once in `MixinPlugin.onLoad` (before any
  mixin applies); `Config` is the single source of truth for both plugin and runtime code.
- **Key → package rule:** `dev.froyln.liteoptimizer.mixin.<pillar>.<name>.SomeMixin` is applied
  iff `Config.get(pillar) && Config.get(name)`. `mixin.dev.*` is applied iff the matching
  `-Dliteoptimizer.*` system property is set — never by config, never in a release by accident.
- **Determinism harness:** every `new Random()` site in `docs/methodology.md` §1 gets a
  `@Redirect(at = @At(value = "NEW", target = "java/util/Random"))` (0.7.5 supports `NEW`
  redirects for no-arg constructors — verify on the first one; fallback: `@Inject` at `RETURN` of
  the constructor and reflectively replace the final field via `@Mutable @Shadow @Final`) →
  `ParitySeeds.next()` = `new Random(seed + counter++)`. Also `World.updateLCG` (initialised from
  an unseeded Random) → `ParitySeeds.next().nextInt()`.
- **State hash:** `ParityDump` walks `WorldServer` chunks sorted by `(x,z)`; per chunk: each
  `ExtendedBlockStorage` `BlockStateContainer` serialized via `getData().getBackingLongArray()` +
  palette ids (use `BlockStateContainer.write(PacketBuffer)` — deterministic), block-light and
  sky-light `NibbleArray.getData()`, TEs sorted by pos → `writeToNBT` → `NBTTagCompound.toString()`,
  entities sorted by id → `writeToNBT` minus `UUIDMost/UUIDLeast` → `toString()`; plus
  `pendingTickListEntriesTreeSet` in order (pos, block id, time, priority), `worldTime`,
  `totalWorldTime`, and `World.rand`'s internal `seed` (reflect `AtomicLong seed`). SHA-256 per
  chunk + one overall; written as text so `diff` names the chunk.
- **Tick control:** `MinecraftServerTickMixin` `@Inject(method="tick", at=HEAD)` counts ticks
  once `worlds[0]` exists and the player has joined (`playerList.getCurrentPlayerCount() > 0`);
  at tick N → `ParityDump.run()` then `server.initiateShutdown()`; `Minecraft.shutdown()` on the
  client thread after the server stopped. The player is `/tp`'d to the world's recorded pose at
  tick 0 by the harness via `server.getCommandManager().executeCommand` (parity of *inputs*).
- **BenchRecorder:** `Tickable` (LiteLoader) samples `Minecraft.getDebugFPS()`-independent
  frame deltas from `onTick` using `System.nanoTime` per render frame via a `LiteLoader`
  `RenderListener`… simpler: mixin `@Inject` at `Minecraft.runGameLoop` HEAD records
  `nanoTime` deltas; server MSPT from `MinecraftServer.tickTimeArray` (public). CSV columns:
  `frame_ns, mspt_ms`. Start/stop by keybind (LiteLoader `KeyBinding` registered in `init`) or
  `-Dliteoptimizer.bench=<label>,<seconds>` (auto: start 30 s after world load, run N s, quit).
- **Benchmark worlds:** copies under `~/Dev/OptimizationMod-1.12.2-worlds/<id>/` with a `pose.txt`
  (`x y z yaw pitch`); `tools/parity.sh` and the bench auto-mode read it.

### Steps

1. `config/Option`, `config/Config`, `config/MixinPlugin`; plugin registered in
   `mixins.liteoptimizer.json`; startup log lists enabled/disabled keys. Commit
   `feat(config): per-optimization toggles via IMixinConfigPlugin`.
2. `mixin/dev/parity/*` + `ParitySeeds` + `ParityDump` + tick control; `tools/parity.sh`.
   Prove **vanilla-vs-vanilla** determinism first: two all-off runs on B3 for 6000 ticks hash
   identical; then B2, B4. If not identical, find the unseeded source (the harness is useless
   until this passes). Commit `feat(dev): parity harness`.
3. `dev/BenchRecorder` + `tools/bench-summary.py`; run-to-run noise on B1 < 3 % across 3 runs.
   Commit `feat(dev): bench recorder`.
4. Benchmark worlds B1–B6 copied from the instance's saves (`docs/profiles/README.md` table),
   `pose.txt` each. Not in git; README lists them.
5. Baseline: `tools/profile.sh` on B1–B6 → `docs/profiles/README.md` filled (top-10 per world).
   Commit `docs(profiles): baselines`.

### Acceptance

- `liteoptimizer.json` with a key `false` → that package is not applied (startup log +
  `-Dmixin.debug.export=true` shows no transformed class for it).
- `tools/parity.sh B3 6000` all-off vs all-off exits 0 three times in a row; same for B2, B4.
- `tools/bench-summary.py` on two baseline B1 recordings: noise < 3 %.
- Full instance mod set loads with the litemod (no harness flags) and Litematica/MiniHUD/Tweakeroo
  smoke lines from `docs/compat/checklist.md` pass at the B1 pose.
- `./gradlew build --offline` exits 0.

### Notes / findings

- `IMixinConfigPlugin.shouldApplyMixin` receives the fully-qualified mixin class name → the
  pillar/name are the 5th/6th package segments; keep the package rule strict.

---

## Task: 0.2 Lithium I — world batch 1 + S-effort memory rows

**Status:** done (v0.2.0, e2172e4) — 12 rows shipped (+ structure_lookup_index from the B4 profile); equipment_change_check / falling_block_movement deferred with reasons; blockpos_garbage, cramming_predicate_cache, nbt_tag_pooling low value. **Owner still owes the in-game smoke checklist** (`docs/compat/checklist.md`) on this build.

Rows (from `docs/catalog.md` batch 5 + memory quick wins), one commit each, harness on B2/B3/B5
per commit: `world/tile_entity_removal` (pilot for the harness), `world/pending_ticks_chunk_lookup`,
`memory/itemstack_is_empty`, `memory/entity_data_manager`, `world/entity_fluid_checks`,
`world/block_collisions` (masa), `world/hard_hitbox_entity_list`, `world/item_merging`,
`world/equipment_change_check`, `world/cramming_predicate_cache`, `world/entity_type_counts`,
`world/falling_block_movement`, `world/furnace_recipe_cache`, `world/raytrace_allocations`,
`world/blockpos_garbage`, `memory/nbt_tag_pooling`.

### Acceptance

- `tools/parity.sh` exit 0 on B2, B3, B4, B5 with all of the above on.
- B2/B3/B5 MSPT and the B3 autosave spike before/after in each catalog row.
- `./gradlew build` exits 0; the litemod loads next to the full instance mod set without a crash.

---

## Task: 0.3a Sodium compat matrix — before any Vintagium code

**Status:** done (`docs/compat/sodium.md`)

For every method Vintagium overwrites or redirects in `RenderGlobal`, `ChunkRenderDispatcher`,
`RenderChunk`, `WorldClient`, `ChunkProviderClient`, `BufferBuilder`, `BlockModelRenderer`,
`ParticleManager`, `Frustum`: list every injection the instance's litemods make into the same
method (`javap -v` on their mixin classes → `@Inject`/`@Redirect` targets), decide per case:
keep the vanilla method body reachable (Sodium's own approach: leave `renderBlockLayer` etc.
and early-return into the new renderer so foreign `@At("HEAD")` injects still apply), provide
the invocation a foreign `@Redirect` targets, or document the incompatibility. Known: Tweakeroo
`forceChunkRendersOnMainThread` (redirects `ChunkRenderDispatcher.updateChunkNow` inside
`RenderGlobal.updateChunks`), Litematica `RenderGlobal`/`ViewFrustum`/`CompiledChunk`/
`ChunkProviderClient`, MiniHUD/Carpet Client/VoxelMap/UsefulMod `RenderGlobal`, Newlight
`RenderGlobal.notifyLightSet`.

### Acceptance

- `docs/compat/sodium.md`: table method × mod × injection × resolution, built from
  `docs/compat/instance-mixins.md`. Zero "unknown". Tweakeroo `updateChunks`/`setupTerrain`,
  Litematica `renderBlockLayer`/`renderEntities`/`updateChunks` calls, VoxelMap `renderSky`/
  `updateClouds`, Newlight/Carpet `notifyLightSet` each have a written resolution.

---

## Task: 0.3b Sodium I — port Vintagium to LiteLoader (oneshot backend)

**Status:** done (0.3.0) — port report in `docs/compat/sodium.md`; render-parity protocol in
`docs/methodology.md` §1. Delivered: 0 differing pixels vs vanilla at the B1, B3 and B6 poses
(vanilla-vs-vanilla control also 0); B6 flight frame −36 % / 1 % low −27 % uncapped; B1 −5 %.
Steps as planned except: the parity draw path is a **fixed-function backend** (vanilla's own
GL calls per section), not Sodium's oneshot shader backend — the shader transform is a
sub-pixel off vanilla and `glMultiDrawArrays` costs ~10× a plain draw on NVIDIA; multidraw
and the compact vertex format stay opt-in. Left for 0.6: the options screen, entity/TE
occlusion, chunk light cache, the by-eye overlay checklist (owner).

**Constraints in bold:** **Mixin 0.7.5 only**, **no Forge classes**, **pixels identical to vanilla
at the benchmark poses** (screenshot diff), **Litematica/MiniHUD overlays still render**.

### Steps

1. Copy `me.jellysquid.mods.sodium.client.*` (242 files) under `dev.froyln.liteoptimizer.render.sodium`
   keeping file headers + `LICENSE.vintagium`; drop `gui/` (options screen — 0.6) and `SodiumClientMod`/
   `SodiumMixinTweaker` (Forge entry points).
2. De-Forge the 16 coupled files: fluids → vanilla `BlockLiquid` only (`VanillaFluidBlock`,
   `FluidRenderer`, `WorldSlice`, `WorldUtil`); `UnpackedBakedQuad`/`LightUtil` → vanilla `BakedQuad`
   vertex layout; `GuiIngameForge` → `GuiIngame`; `ForgeHooksClient.setRenderLayer`/
   `MinecraftForgeClient.getRenderLayer` → own thread-local; `ObfuscationReflectionHelper` → accessors;
   `Loader.isModLoaded` → `LiteLoader.getInstance().isModInstalled`.
3. Port the 42 mixins to 0.7.5: drop `@Dynamic`, drop `conformVisibility` (make overwrites
   public where vanilla is), `minVersion 0.7.5`; apply the compat-matrix resolutions.
4. Register `SodiumWorldRenderer` from `RenderGlobal` hooks; GL capability check → fall back to
   vanilla when `GL20`/VAO unsupported (config `render/sodium` master switch).
5. Screenshot diff at B1/B6 poses vs vanilla; B1/B6 frame-time before/after; heap before/after
   (Sodium's compact vertex format should *reduce* VRAM/heap — record it).

### Acceptance

- 0 differing pixels at the B1 and B6 poses (fixed time, no particles, no entities in frame);
  documented, explained exceptions only.
- B6 flight: average frame time and 1% low improved; chunk build throughput (chunks/s) improved.
- Full instance mod set loads; Litematica schematic overlay, MiniHUD shapes, Carpet Client
  render hooks verified by eye at the B1 pose.

---

## Task: 0.4 FerriteCore — memory batch

**Status:** pending

`memory/blockstate_property_storage` (VintageFix/FoamFix code), `memory/baked_quad_dedup`,
`memory/model_dedup` + `model_part_dedup`, `memory/sprite_frames_release`, `memory/resource_location_intern`.
Acceptance: heap after forced GC at the B1 pose before/after; `jcmd GC.class_histogram` deltas
for `int[]`, `BakedQuad`, `ImmutableTable`, `TextureAtlasSprite`.

---

## Task: 0.5 Lithium II — world batch 2 + AI

**Status:** pending

`world/hopper_inventory_cache` (+ bb cache), `world/tick_scheduler`, `world/explosions` (MASA
subset), `world/entity_lookup`, `ai/goal_selector`, `ai/pathfinding_node_cache`,
`ai/can_entity_be_seen`, `world/portal_search_cache`. Harness on B3/B4/B5 per commit.

---

## Task: 0.6 Sodium II

**Status:** pending

Multidraw backend (GL43), `render/entity_culling` + `render/tile_entity_occlusion` on top of
Sodium's culling data, `render/entity_model_matrix`, options screen, `render/chunk_light_cache`
tuning, and whatever the profiles then show.

---

## Task: (retired) Pilot — `render/tile_entity_frustum` end to end

**Status:** dropped (superseded by 0.2 — `world/tile_entity_removal` is the pilot)

Smallest catalog row with a screenshot-provable parity, to exercise the whole pipeline:
catalog argument → package + key → build → screenshot diff → benchmark → commit. If the
research task finds a better pilot, swap it here.

### Acceptance

- Screenshot diff at the B1 pose: 0 differing pixels with vs without.
- B1 frame time delta reported (any value; the point is the pipeline).
- Catalog row moved to `shipped (<sha>, <numbers>)`.

---

## Done

- Scaffold — done (uncommitted), template + FG 2.3 liteloader build + empty mixin config;
  `./gradlew build` exits 0.

## Dropped / deferred

- Lighting engine work — deferred to "never unless profiles say so", Newlight covers it.

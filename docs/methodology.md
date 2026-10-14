# Methodology: how an optimization gets in

Three questions, in order. A "no" at any step ends the candidate.

1. **Is it parity-safe?** Same game state, same RNG consumption order, same iteration order,
   same rendered pixels (for render work). Argued in writing first, then proven by the harness.
2. **Is it measurable?** A before/after number on at least one benchmark world.
3. **Is it worth its code?** Gain per line. A 2% win that costs 400 lines of `@Overwrite` loses.

## 1. Parity

### Why a naive "run twice, compare" does not work

Vanilla 1.12.2 is not reproducible run-to-run: `World.rand`, every `Entity.rand`,
`MathHelper.RANDOM`, `Block.RANDOM`, `Item.itemRand` are `new Random()` (unseeded). Two vanilla
runs of the same world already diverge. So the harness must first make vanilla deterministic,
then compare mod-on vs mod-off under that determinism.

### Harness (implemented in 0.1; `tools/parity.sh <world-dir> [ticks] [--only <key>] [--vanilla]`)

Runtime flags (all under `mixin/dev/*`, applied only when the matching `-Dliteoptimizer.<pkg>`
property is set, never from the config file):

| property | effect |
|---|---|
| `liteoptimizer.parity=<seed>` | seeds every `new Random()` listed below (`ParitySeeds`), counts server ticks, dumps the overworld at tick N (`ParityDump` → `<game dir>/parity/<label>.txt`) and exits |
| `liteoptimizer.parity.world=<save>` | main menu loads this save immediately (`GuiMainMenuMixin`) |
| `liteoptimizer.parity.ticks=<n>`, `.label=<name>`, `.pose=x,y,z,yaw,pitch` | run length, dump name, `/tp` at tick 0 |
| `liteoptimizer.nopause=1` | `Minecraft.displayInGameMenu` cancelled — an unfocused window would otherwise pause the integrated server and freeze the tick count |
| `liteoptimizer.bench=<label>[,warmup_s[,seconds]]` (+ `.quit`) | `BenchRecorder`: frame durations + integrated MSPT → `<game dir>/bench/<label>.csv` |

`tools/parity.sh` copies the world into the instance as `saves/_parity`, sets the JVM args in
`instance.cfg`, writes `liteoptimizer.json` all-off / all-on (or `--only <key>`), launches the
instance twice via `prismlauncher --launch`, diffs the two dumps, and restores everything (trap).
`--vanilla` runs off vs off — the determinism check that must pass before the harness means
anything. Dumps land in `run/parity/`.

### Isolated benchmark comparisons

`tools/bench.sh <world> <warmup> <seconds> --compare <key>` keeps all known switches on
in both runs and changes only the named source option. This holds renderer dependencies constant when measuring one retained optimization. `--only` remains the older all-off versus selected-feature
mode and does not isolate a feature from renderer dependencies. Saved `run/bench/<world>-off/on.config.json`
files record the exact switches. Inspect these and the Sodium settings before attributing gain.
The runner refuses a live Minecraft process before changing instance settings.

`--no-entities` hides moving entities but **retains item frames and paintings**. A B1 capture
in that mode covers hanging-entity rendering and TESRs; it cannot prove other entity types.
Disable dynamic HUD overlays consistently for pixel comparisons, or explicitly report their
pixels separately without claiming an entire screenshot match. For Tweakeroo glint suppression,
check both `YeetToggles.disableItemGlint` and `Lists.itemGlintItemList`: a potion blacklist
does not suppress enchanted-tool glint. Save and restore these user settings.

### Harness design

- **Seed everything.** A mixin package `mixin/dev/parity/` redirects every `new Random()`
  that feeds game state to `new Random(seed + counter++)`, counter advanced in construction
  order. Construction order is itself part of vanilla behavior, so this is deterministic iff
  the game is. Verified list from `grep -rn "new Random()"` over the 1.12.2 decompiled source
  (client-render/audio/gui sites omitted — they never touch state):
  `World.rand` and `World.updateLCG` (random-tick LCG is seeded from an unseeded Random!),
  `Entity.<init>`, `Explosion.<init>`, `MinecraftServer.random`, `MathHelper.RANDOM`,
  `Item.itemRand`, `InventoryHelper.RANDOM`, `BlockDispenser.rand`, `TileEntityDispenser.RNG`,
  `TileEntityEnchantmentTable.rand`, `TileEntityLockableLoot`/`EntityMinecartContainer` loot,
  `ContainerEnchantment`, `MapGenBase.rand`, `MapGenStructure`/`MapGenStronghold` locals,
  `NoiseGenerator*` no-arg constructors, `DragonFightManager`/`DragonSpawnManager`/
  `TileEntityEndGateway` locals, `CommandWeather`. (`Block` has no own Random in 1.12.2 — it
  uses `world.rand`.) `Random`
  instances created with `new Random(seed)` are already deterministic.
  **`Math.random()`** (a global unseeded `Random` inside the JDK) is used by vanilla for state:
  `EntityLivingBase.<init>` (spawn yaw), `attackEntityFrom` (knockback direction),
  `EntityItem.<init>` (drop motion + rotation), `EntityXPOrb.<init>`, `EntityTNTPrimed.<init>`
  (primed-TNT push direction), `WorldEntitySpawner.findChunksForSpawning` (attempts per pack),
  `SlotFurnaceOutput.onCrafting` (XP rounding). Each call site is redirected to a per-thread seeded
  Random. `ThreadLocalRandom` only appears in `AttributeModifier` (UUID) and client sound code.
- **Wall-clock budgets.** `PlayerChunkMap.tick` loads player chunks until 49 are done *or 50 ms
  passed* — which chunks exist at tick N then depends on disk speed. The harness pins that clock
  (`PlayerChunkMapMixin`) so the loop is bounded by the counter only.
- **World ticks are held until the player is in** (`MinecraftServerWorldTickMixin`): the join
  takes a variable number of server ticks, and ticking the world during it consumed RNG.
- **The pause menu is disabled** (`-Dliteoptimizer.nopause`): an unfocused window pauses the
  integrated server.
- **`PathNavigate.checkForStuck` uses `System.currentTimeMillis()`** to abandon a mob's path,
  so mob movement diverges with real elapsed time (first seen at tick ~107 on the witch farm).
  The harness feeds it a clock that advances 50 ms per counted tick (`PathNavigateMixin`).
- **`Math.sin/cos/pow` differ between interpreter and JIT on JDK 8** (fdlibm vs x87 intrinsics);
  the game-state call sites (`EntityAILookIdle`, `EntityPlayer.dropItem`, `EntityMinecart`,
  `EntityEnderEye`, `EntityTNTPrimed`, `EntityDragon`, `MathHelper` tables) are redirected to
  `StrictMath` in harness mode. Not proven to matter on the test worlds, pinned on principle.
- **Fixed input.** Player does nothing; harness `/tp`s them to a fixed pose on world load,
  render distance fixed, `doDaylightCycle false`, `randomTickSpeed` at the world's value.
- **Fixed length.** Count integrated-server ticks from the first tick after the world finishes
  loading (`MinecraftServer.tick` inject). At tick N (default 6000 = 5 min) dump and quit.
- **State hash.** Iterate loaded chunks in ascending `(x, z)`; for each chunk hash, in order:
  every `ExtendedBlockStorage` block data + palette, block light + sky light arrays, every
  `TileEntity.writeToNBT` (sorted by pos), every `Entity.writeToNBT` (sorted by entity id;
  strip `UUIDMost/Least`, which come from an unseeded source, and `Pos` beyond 1e-9 rounding
  only if a documented float-order concern appears — default is exact). Also hash the
  scheduled tick list (`WorldServer.pendingTickListEntriesTreeSet`, in iteration order), the
  world time, and the `World.rand` internal seed (reflectively) — the last one catches an
  optimization that skipped or added a random call even when the visible state happened to
  match. Write `<runDir>/parity/<label>.txt` with per-chunk hashes so a mismatch says *which
  chunk* diverged.
- **Runner.** `tools/parity.sh <world> <ticks>`: copies the world twice, runs the game with
  config all-off then all-on (or `--only <optimization>`), diffs the two dumps. Exit 0 = parity.
- **Render parity.** For render optimizations: fixed pose, `F2` screenshot at tick N with
  and without; `compare -metric AE` (ImageMagick) must be 0 for anything claiming
  "identical output" (culling, batching, texture animation). Things that legitimately change
  pixels (none planned) would need an explicit exception in the catalog.

### What the harness proved (2026-09-17, world B3 "50 furnace Array", seed 1)

Vanilla-vs-vanilla, two separate game launches, 600 ticks (and 6000 on B3): all 625 chunks, every entity and
tile entity, the scheduled-tick list, world time **and** `World.rand`'s internal seed identical;
the per-call-site histogram of `World.rand` consumption (verbose mode, `TracingRandom`) identical
too. Before the fixes above, runs diverged in (1) world time (join delay), (2) one chunk
(chunk-load timing), (3) `World.rand` only (`Math.random()` in the spawner). Each was found
with the verbose dump: `PARITY_VERBOSE=1 tools/parity.sh …` writes `<label>.detail.txt` with
every entity/TE NBT, a per-tick series (loaded/sent chunks, rand calls, player pos, entity
count) and the rand call-site histogram — diff those to name the culprit.

### Parity-argument checklist (write it in `docs/catalog.md` before coding)

- Which vanilla method(s) are touched, and what observable side effects they have.
- RNG: does the code call any `Random`? Same count, same order after the change?
- Iteration order: lists, `entityLists`, `tileEntities`, hash maps (`HashMap` order is
  deterministic for the same insertion sequence, but changing insertion order changes it).
- Floating point: same operations in the same order (no reassociation, no `fast` math).
- Caching: what invalidates the cache? Every vanilla mutation path must invalidate it
  (`setBlockState`, chunk unload, TE removal, entity removal/teleport/dimension change).
- Threading: does anything move off the main thread? Then results must be consumed at the
  same point vanilla would have computed them.

### Render parity (the Sodium pillar): same pixels, same mesh bytes

The render pillar cannot be checked with the world-state hash: its output is the frame. The
check is a screenshot diff at a fixed pose, and it only works once everything that vanilla
itself renders non-deterministically is pinned. `tools/bench.sh <world> [warmup] [seconds]
[--no-entities] --profile` does all of it, for the vanilla run and the mod run alike:

- **Fixed tick, not wall clock.** The screenshot is taken on the frame after client world tick
  `⌈(join + warm-up) / 100⌉ · 100` — an *absolute* tick, so two runs whose join tick differs by
  a few ticks still shoot the same one (beacon beams and end portals animate per tick).
- **Partial tick 0.** Every frame of a screenshot run renders at `partialTicks = 0`
  (`MinecraftLoopMixin`), because where inside a tick the frame lands is wall-clock jitter.
- **Torch flicker frozen, lightmap still rebuilt** (`EntityRendererFlickerMixin`): the flicker
  is a `Math.random()` walk per client tick. (Cancelling `updateTorchFlicker` outright leaves
  the lightmap black — it is what sets `lightmapUpdateNeeded`.)
- **Animated sprites frozen** (`TextureAtlasSpriteFreezeMixin`): water, lava, fire, sea lantern,
  prismarine, magma advance per client tick since the atlas was loaded.
- **Pose baked into the save** (`level.dat` `Player.Pos/Rotation`, `abilities.flying`), not
  `/tp` at tick 0: chunks built before a teleport are sorted for the old camera.
- **`doDaylightCycle`/`doWeatherCycle` off, `raining`/`thundering` 0** in the world: rain streaks
  are random per frame, the sky colour moves with the time of day.
- **`--no-entities`** hides entities (`RenderManager.shouldRender` false): a dropped item spins
  from an unseeded client `Math.random()` and differs between any two runs, vanilla or not.
  Block entities stay. Some TESRs use additional client randomness or wall-clock effects;
  verify an unchanged control before calling a scene deterministic.

Vanilla-vs-vanilla under this protocol is 0 differing pixels on B1 (a base with beacons, water
streams, sea lanterns, glass) — that is the control; a mod-vs-vanilla diff then means the mod.

When pixels differ, the second tool tells *what* differs: with `--profile`, both runs dump
every built section's vertex bytes (vanilla's `BufferBuilder` after `postRenderBlocks`, Sodium's
mesh after `createMesh`; same 28-byte `BLOCK` layout) to `run/bench/<world>-vtx-{off,on}/`, and
`tools/vtx-diff.py` reports, per section and layer, vertex-count differences, differing fields
(position / colour / uv / light) and whether a difference is order-only. That is how the
outer-ring build rule, the empty-section sky light, the fluid position rounding and the double
draw of global block entities were found (see `docs/compat/sodium.md`).

## 2. Measurement

### Benchmark worlds (`~/Dev/OptimizationMod-1.12.2-worlds/`, outside the repo, built once)

| id | content | stresses |
|----|---------|----------|
| B1 base | a large real technical base pasted with Litematica (schematics in the instance's `schematics/` dir) | chunk meshing, TE rendering, item frames, memory |
| B2 items | item stream farm / mass drop, ~5k `EntityItem` | entity collisions, item merging, `getEntitiesWithinAABB` |
| B3 redstone | large clock/sorter arrays, thousands of scheduled ticks | tick scheduler, hoppers, block updates |
| B4 mobs | mob farm + spawning platforms, ~1k `EntityLiving` | AI, pathfinding, entity ticking |
| B5 tnt | TNT duper / world eater running | explosions, block updates, chunk rebuilds |
| B6 flight | elytra flight over generated terrain at RD 16/32 | chunk load/decode, chunk build throughput |

### What we record

- **Client:** average frame time and 1% low over a 60 s window after 30 s warm-up, fixed pose.
  `tools/bench.sh` sets `maxFps:260` (uncapped) in `options.txt` for the run and restores the
  file after; the instance's own cap (140) would hide every client-side gain. `--fly <blocks/tick>`
  moves the player straight ahead during the window (B6). `--profile` also dumps vanilla's
  section profiler (`run/bench/<world>-<label>.profile.txt`: ms per frame per section, and the
  rendered-section list) — JFR barely samples the client thread (LWJGL native calls), so this
  is the client-side profiler; `--jfr` remains for the server thread.
- **Server (integrated):** average and max MSPT over the same window.
- **Memory:** heap after a forced GC (`jcmd <pid> GC.run` then `GC.heap_info`), and the
  `Chunk`/model object counts from `jcmd <pid> GC.class_histogram | head -40`.
  `tools/bench.sh <world> --heap` does exactly that once the recording window starts and writes
  `run/bench/<world>-<label>.heap.txt` for off and on; diff the two.
- **Hotspots (research only):** JFR via `tools/profile.sh <label> [seconds]` (jcmd attach →
  `docs/profiles/<label>.jfr` + `<label>.top.txt` with the hottest self/inclusive frames).
  The instance ships Oracle `jre-legacy` 8u202, where JFR needs `-XX:+UnlockCommercialFeatures`;
  simplest is to point the instance at `/usr/lib/jvm/java-8-openjdk/bin/java` (8u504, JFR
  built in) in its `instance.cfg` `JavaPath`. Vanilla's own
  `/debug start` … `/debug stop` writes the server profiler tree to `debug/profile-results-*.txt`;
  `shift+F3` pie chart for the client. Both cost nothing and are the first thing to run.

The in-mod `BenchRecorder` (dev keybind) records frame times + integrated MSPT to
`<runDir>/bench/<label>.csv`; `tools/bench-summary.py` prints avg / 1% low / max and the
delta between two CSVs. Same world, same seed, same pose, mod-off then mod-on, three runs
each, report the median. A gain inside run-to-run noise (< 3%) counts as zero.

## 3. Working the catalog

`docs/catalog.md` is the single list. Every candidate goes in with: source (mod + module +
license), what it changes, applies to 1.12.2 (`yes`/`no`/`partial` with the class names), parity
risk (`none` = render or memory only, `low`, `med`, `high`), expected gain (which benchmark),
effort (S/M/L), status.

Priority = expected gain × confidence ÷ (effort × parity risk). Work top-down in batches of
3–5 related items per PLAN.md task. The order of batches is fixed by risk, not by gain:

1. infrastructure (config plugin, BenchRecorder, parity harness, benchmark worlds)
2. `render` — client only, parity risk none, proven by screenshot diff
3. `memory` — parity risk none, proven by heap numbers
4. `world` / `ai` — parity harness mandatory, one optimization per commit
5. big rewrites (chunk render pipeline, tick scheduler data structure) — only if the numbers
   from 2–4 leave them as the dominant remaining cost

Per optimization: catalog row with parity argument → `/plan-task` → implement (one package,
one config key) → `./gradlew build` → `tools/parity.sh` → benchmark → numbers into the row →
`perf(<area>): <name>` commit. Anything that fails parity is reverted, not tuned.

## Licensing

Porting an *idea* (what to cache, what to cull) needs no license. Copying *code* does. Before
the first line is copied from any upstream, the catalog row records that upstream's license
and this project's license is chosen to be compatible (candidates: MIT if we copy only from
MIT sources — FerriteCore, FoamFix, VanillaFix, older Lithium; LGPL-3.0 if we copy from
Sodium 0.5 / Phosphor / Vintagium / Celeritas / current Lithium). Verify each license on the
actual commit copied from — several of these projects relicensed over time. Default: clean-room
from the decompiled vanilla source plus the upstream's *description* of the change.

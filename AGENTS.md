# LiteOptimizer (LiteLoader 1.12.2)

Client-side optimization mod for Minecraft 1.12.2 on LiteLoader, aimed at the technical
community (Carpet servers, Litematica/MiniHUD/Tweakeroo clients). It ports the *ideas* of
the modern optimization stack — Lithium (game logic), Sodium (rendering), FerriteCore (memory),
Phosphor (lighting) — plus the 1.12.2 Forge optimization mods that are behavior-preserving, to
a single dependency-free `.litemod`, licensed **LGPL-3.0**. **Hard rule: vanilla mechanics stay identical.** Every
optimization must produce the same game state, same render output (modulo FPS), same RNG
consumption and same update order as vanilla 1.12.2. If it can't, it doesn't ship.

> AI agents: `CLAUDE.md` imports this file and adds the working agreement. Task state is in
> `PLAN.md`. Path-specific rules are in `.claude/rules/`. Research output lives in `docs/`.

## Tech stack

- **Language:** Java 8 (`sourceCompatibility = 1.8`; no `var`, records, `List.of`, streams
  in hot paths). ForgeGradle 2.3 compiles with JDK 8 only.
- **Framework / runtime:** Minecraft 1.12.2 + LiteLoader 1.12.2 (`com.mumfrey.liteloader.LiteMod`),
  MCP mappings `stable_39`, LWJGL 2, SpongePowered Mixin **0.7.5** at runtime (bundled with LiteLoader;
  the annotation processor on the build classpath is 0.7.4 —
  no MixinExtras, no `@ModifyExpressionValue`/`@WrapOperation`; use `@Inject`, `@Redirect`,
  `@ModifyVariable`, `@ModifyConstant`, `@Overwrite`, `@Accessor`/`@Invoker`).
- **Package manager:** Gradle 4.10.3 wrapper (committed) + ForgeGradle 2.3-SNAPSHOT
  (`net.minecraftforge.gradle.liteloader` plugin) + MixinGradle 0.6-SNAPSHOT. Same setup as
  `~/Dev/isorenders` and `~/Dev/SchematicPreview`.
- **Key dependencies:** none at runtime. No malilib — config is a plain JSON file so the mod
  works in any LiteLoader install and can never be the reason another mod breaks.
- **Datastore / external services:** none.

## Commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk   # system JDK 21 cannot run ForgeGradle 2.3
./gradlew build --offline       # build → build/libs/liteoptimizer-liteloader-1.12.2-<ver>.litemod (drop --offline only when deps changed)
./gradlew compileJava           # fast check while iterating
./gradlew setupDecompWorkspace  # once: decompiled vanilla source into ~/.gradle/caches/minecraft (for research)
./gradlew runClient             # dev client from ./run (asset download often stalls on this network)
tools/test-in-game.sh           # build → copy litemod into the PrismLauncher instance → launch it
tools/parity.sh <world> [ticks] [--only key] [--vanilla]   # state-hash diff off vs on (docs/methodology.md §1)
tools/bench.sh <world> [warmup] [s] [--only key] [--heap] [--jfr]   # frame/MSPT off vs on; --heap adds forced-GC heap + class histogram
tools/bench-summary.py a.csv [b.csv]                       # frame/MSPT summary of BenchRecorder output
tools/profile.sh <label> [s]                               # JFR of the running game → docs/profiles/
```

No unit tests in the Gradle build. `compileJava` is the smallest check; `build` also generates
the mixin refmap and packs the litemod. Pure-Java helpers (no MC imports) carry a
`main`-based self-check runnable with `javac`/`java` directly — see `.claude/rules/testing.md`.

## Verification

The check that must pass before any change is called done:

```bash
JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew build   # must exit 0
```

Then, for every optimization that ships, the two hand checks described in `docs/methodology.md`:

1. **Parity:** the mod must not change game state. Run the parity harness (same world, same
   seed, N ticks, with and without the mod → identical chunk/entity NBT hash).
2. **Gain:** a before/after measurement on the benchmark worlds (FPS, MSPT, heap). No number,
   no merge — an optimization that can't be measured is deleted.

In-game session: `./gradlew build && tools/test-in-game.sh` launches the PrismLauncher instance
`1.12.2 test ai` (LiteLoader 1.12.2 + the full technical mod stack: litematica, malilib, minihud,
tweakeroo, carpet client, newlight, litetweaker, ...) offline with the fresh litemod installed.
Prefer it over `runClient`.

This command is also what `.claude/hooks/verify-on-stop.sh` runs when enabled, and the default
`/goal` condition: "`./gradlew build` exits 0".

## Project structure

```
build.gradle / gradle.properties      # FG 2.3 liteloader plugin + mixin; mod id/version live in gradle.properties
src/main/java/dev/froyln/liteoptimizer/
├── LiteModLiteOptimizer.java         # LiteLoader entry point — does nothing but register the mod
├── config/                           # JSON config: one boolean per optimization (read once at startup)
├── mixin/                            # Mixin classes ONLY — nothing else may live here (see Gotchas)
│   └── <area>/<optimization>/        # one sub-package per optimization, e.g. mixin/render/entity_culling/
└── <area>/                           # plain helper classes an optimization needs (outside mixin/)
src/main/resources/
└── mixins.liteoptimizer.json         # mixin config; refmap generated at build
docs/
├── methodology.md                    # how to measure, how to prove parity, benchmark worlds
├── catalog.md                        # every candidate optimization: source, applies?, parity risk, license, status
├── vanilla-audit.md                  # what the vanilla hot paths do, method by method, with line numbers
├── compat/                           # checklist.md (per-mod smoke tests) + instance-mixins.md (generated)
└── profiles/                         # profiler outputs / numbers backing decisions
tools/test-in-game.sh                 # install into PrismLauncher instance and launch
tools/compat-scan.py                  # regenerate docs/compat/instance-mixins.md from the instance's litemods
tools/profile.sh                      # JFR profile of the running game
```

## Architecture

Three pillars in one litemod, each a package with its own config keys and a master switch:

| pillar | package | role | upstream code we port (all LGPL-3-compatible) |
|--------|---------|------|-----------------------------------------------|
| Sodium | `render/` | replaces the terrain render pipeline (`RenderGlobal.setupTerrain`/`renderBlockLayer`/`updateChunks`, `RenderChunk`, `ChunkRenderDispatcher`) with Sodium's chunk-graph culling, worker meshing, compact vertex formats, region VBOs, GL20 oneshot / GL43 multidraw backends; plus particle/entity culling, texture animation, model matrix | Vintagium (`~/Dev/references/Vintagium`, Sodium 0.4 on 1.12.2 Forge) de-Forged and ported to Mixin 0.7.5 |
| Lithium | `world/`, `ai/` | game-logic optimizations with bit-identical state: collisions, entity lookups, hoppers, tick scheduler, explosions, AI, block-entity ticking, chunk save | carpetmod112 helpers, TISCarpet113 ports, own code from `docs/vanilla-audit.md` |
| FerriteCore | `memory/` | heap: blockstate tables, quad/model dedup, sprite frames, NBT canonicalization, `EntityDataManager`, `ItemStack.isEmpty` | FoamFix / VintageFix 1.12.2 code |

Flow: LiteLoader loads `litemod.json` → registers `mixins.liteoptimizer.json` → the
`IMixinConfigPlugin` (`config/`) reads `liteoptimizer.json` and answers `shouldApplyMixin` per
mixin package → Mixin applies each enabled optimization to its vanilla target at classload. Any
single optimization or whole pillar can be switched off without a rebuild — that is how a user
bisects a suspected parity bug, and how the Sodium pillar falls back to vanilla rendering on a GPU
without GL20/VAO.

Each optimization = one package under `mixin/<pillar>/<name>/` + helpers under `<pillar>/` +
one config key + one row in `docs/catalog.md` with its parity argument and measured gain.
Optimizations never depend on each other, except that render batch items (culling, textures)
hook Sodium's world renderer when the Sodium pillar is on and vanilla's when it is off.

Pillar packages: `render` (Sodium-like), `world` (Lithium-like), `ai` (Lithium-like), `memory`
(FerriteCore-like), `light` is **not** ours — `Newlight` already exists on LiteLoader; never
conflict with it.

## Configuration

- **Config file:** `<liteloader config dir>/liteoptimizer.json` — flat `{ "<optimization>": true|false }`.
  Missing keys = enabled. Read once at startup; changes need a restart (mixins are classload-time).
- **Environment variables:** none.
- **Secrets:** none.

## Compatibility invariants

The owner's instance (`~/.local/share/PrismLauncher/instances/1.12.2 test ai`, 18 litemods:
malilib, litematica, minihud, tweakeroo, itemscroller, Carpet Client ×2, newlight, litetweaker,
UsefulMod, tellme, chatcalc, connect_to_1_12_x, voxelMap, worldeditcui, isorenders,
schematicpreview, BBOutlineReloaded) **must keep working with every release**. Not a goal — a
gate. `docs/compat/checklist.md` is the per-mod smoke list; `docs/compat/instance-mixins.md`
(from `tools/compat-scan.py`) lists all 638 vanilla members those mods mixin into. Rules:

- Grep the target class + method in `instance-mixins.md` before writing a mixin.
- Never `@Overwrite` a method another installed mod injects into; `@Inject`+cancel instead.
- Never remove an invocation another mod `@Redirect`s (`call` rows) from a method we rewrite.
- Lighting methods are Newlight's (`@Overwrite`s) — off-limits.
- The litemod must load with the full set and the checklist must pass at the B1 pose before a
  version is tagged.

## Security invariants

No trust boundary: no network, no file input beyond the config JSON. Only rule: a malformed
config must fall back to defaults, never crash the game.

## Conventions

- **Parity first.** Before writing a mixin, write the parity argument in `docs/catalog.md`:
  why the output is bit-identical (same RNG calls in the same order, same iteration order,
  same side effects). No argument → not an optimization for this mod.
- Prefer `@Inject`/`@Redirect` over `@Overwrite`. `@Overwrite` only for a whole-method rewrite,
  and then the vanilla method body is pasted as a comment above for diffing.
- One optimization per package, one config key, same name in both, snake_case:
  `mixin/world/hopper_item_lookup/` ↔ `"hopper_item_lookup"`.
- Hot paths: no allocation, no boxing, no streams, no lambdas capturing state. This is the
  whole point of the mod.
- Client-side only mod; but singleplayer's integrated server runs inside the client, so
  `world`/`ai` mixins do run there. They must be safe when the class is loaded on a dedicated
  server anyway (LiteLoader is client-only, so this is theoretical — don't add server code).
- Compatibility rules above; `docs/compat/instance-mixins.md` is the lookup table.
- Git and commit rules: see `.claude/rules/git.md`. Testing rules: `.claude/rules/testing.md`.

## Gotchas

- **Build with `JAVA_HOME=/usr/lib/jvm/java-8-openjdk`.** Default JDK is 21; ForgeGradle 2.3
  dies on it. System JDK 8u504 works *here* because the mod has no third-party litemod
  dependency to deobfuscate (the `ZipException: invalid entry compressed size` bug in
  `~/Dev/SchematicPreview` only hits `deobfCompile` of foreign jars). If a dep is ever added,
  copy `tools/setup-build-deps.py` from SchematicPreview and pin Temurin 8u302.
- **Nothing but `@Mixin` classes may live under `dev.froyln.liteoptimizer.mixin`.** Mixin 0.7.4
  excludes the whole `package` from normal classloading; a plain helper there throws
  `NoClassDefFoundError: ... is a mixin class and cannot be referenced directly` at runtime,
  invisible to `build`. Casting to an accessor interface inside another mixin also breaks
  (`InvalidMixinException: unable to find corresponding type`); do the cast in a helper outside
  the package. Both learned the hard way in SchematicPreview.
- The shipped `.litemod` is Notch-obfuscated; the mixin `refmap` maps targets. Forgetting
  `ext.refMap` in `sourceSets.main` → mixins silently fail in prod.
- `./gradlew runClient` first run downloads assets from `resources.download.minecraft.net`,
  which returns HTTP 400 on this network. Use `tools/test-in-game.sh`.
- Decompiled vanilla source (for reading, never copying into the mod) lives at
  `~/.gradle/caches/minecraft/net/minecraft/minecraft/1.12.2/stable/39/minecraftSrc-1.12.2.jar`
  after `setupDecompWorkspace`. Extract it into `/tmp` or the scratchpad, not into the repo.
- `gradle.properties` needs `org.gradle.jvmargs=-Xmx3G` or decompilation OOMs.
- Without `--offline`, ForgeGradle re-fetches version JSONs (`BasePlugin.getRemoteJsons`) on
  every build; when that host times out on this network a 10 s build takes 10+ minutes and
  ends with a stack trace but still exits 0. Everything is cached — build offline.
- LiteLoader loads mods alphabetically by file name; mixin application order across mods is
  by priority, not by that. When two mods `@Inject` the same target it usually works; two
  `@Overwrite`s don't — see Conventions.

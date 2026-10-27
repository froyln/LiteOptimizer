# LiteOptimizer (LiteLoader 1.12.2)

Client-side optimization mod for Minecraft 1.12.2 on LiteLoader, aimed at the technical
community (Carpet servers, Litematica/MiniHUD/Tweakeroo clients). It ports the *ideas* of
the modern optimization stack — Lithium (game logic), Sodium (rendering), FerriteCore (memory),
Phosphor (lighting) — plus the 1.12.2 Forge optimization mods that are behavior-preserving, to
a single dependency-free `.litemod`, licensed **LGPL-3.0**. The primary target is an optimized
client connecting to an **unmodified vanilla server**: no server installation is required.
**Hard rule: server behavior stays vanilla; client-only behavior may change for performance.**
Rendering, visual effects, client animation timing and presentation math may differ when the
tradeoff is documented and configurable. Server simulation/state, server RNG consumption and
update order remain identical, including singleplayer's integrated server. Preserve vanilla
network compatibility and the meaning/timing of player actions; a client-side hook is not
permission to alter movement, interaction reach, inventory actions or server-bound packets.
Shared code/RNG must retain server parity or isolate the changed path to the client.

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

Then, for every optimization that ships, the two evidence checks described in `docs/methodology.md`:

1. **Behavior boundary:** shared/server logic must pass the parity harness (same world,
   seed, N ticks, with and without the mod → identical chunk/entity NBT hash). Client-only
   changes must document their visual/behavior tradeoff and prove they do not affect server
   simulation or player actions. Exact pixel equality is required only when claimed; otherwise
   inspect the intended differences and test visibility, interactions and mod compatibility.
2. **Gain:** a before/after measurement on the benchmark worlds (FPS, MSPT, heap). No number,
   no merge — an optimization that can't be measured is deleted.

In-game session: `./gradlew build && tools/test-in-game.sh` launches the PrismLauncher instance
`1.12.2 test ai` (LiteLoader 1.12.2 + the full technical mod stack: litematica, malilib, minihud,
tweakeroo, carpet client, newlight, litetweaker, ...) offline with the fresh litemod installed.
Prefer it over `runClient`.
Release checks also require connection and ordinary play on an unmodified vanilla 1.12.2
server. The full-stack Carpet fixture remains useful compatibility evidence, not proof of
vanilla-server compatibility. See `docs/methodology.md` for the client-change checks.

### Isolated in-game desktop workflow

When the owner wants Minecraft opened for an agent to inspect or control, use the existing
headless Sway desktop with WayVNC. It is separate from the owner's Hyprland workspaces and keeps
the physical mouse and keyboard out of the game session:

- Reuse the AI Sway session when available. If isolated VNC is not running, start it with
  `/home/froyln/Dev/ai-desktop.sh`, then verify `127.0.0.1:5900` is listening. Keep WayVNC
  bound to loopback.
- Launch PrismLauncher and Minecraft **inside that Sway session**, for example through its
  `SWAYSOCK` IPC (`swaymsg exec prismlauncher`), then use a local/headless VNC connection to
  `127.0.0.1:5900` for screenshots and virtual input. Do not start Prism from the regular
  Hyprland terminal and assume it will appear in Sway.
- Do not move or focus windows across the owner's workspaces, switch their active workspace, or
  use tools that drive the physical mouse/keyboard. Do not ask the owner to move Minecraft or
  click through the game. Use only the isolated session's compositor IPC and WayVNC virtual
  input for in-game interaction.
- Do not open a VNC viewer on the owner's normal desktop. Connect from the agent side; if that
  connection is unavailable, report the specific blocker instead of falling back to host input.
- The nested `WLR_BACKENDS=wayland` setup proved unreliable here because its Wayland output
  disappeared from WayVNC. Use the headless backend for this workflow.

### Minecraft GUI control rules

The isolated graphical desktop is available through VNC at `localhost:5900`. It belongs to
a separate Sway compositor and is separate from the owner's normal Hyprland desktop.
All Minecraft visual interaction must happen through this VNC session.

- NEVER control the normal Hyprland desktop or move the physical mouse.
- NEVER switch Hyprland workspaces.
- NEVER use `hyprctl` to focus, move, or interact with Minecraft.
- NEVER use desktop-control tools that operate on the host Hyprland session.
- NEVER bring Minecraft onto the physical monitors.

Before GUI control, verify that isolated VNC is available:

```bash
ss -ltnp | grep 5900
```

If `127.0.0.1:5900` is not listening, run `/home/froyln/Dev/ai-desktop.sh` and check again.
If it is still unavailable, report that the isolated VNC desktop is unavailable. Never
fall back to controlling the normal desktop; host Hyprland is off-limits for GUI automation.

Capture the current isolated desktop and inspect `/tmp/ai-desktop.png`:

```bash
vncdo -s localhost::5900 capture /tmp/ai-desktop.png
```

Use VNC mouse and keyboard input freely inside the isolated desktop:

```bash
vncdo -s localhost::5900 move X Y
vncdo -s localhost::5900 click 1          # left click
vncdo -s localhost::5900 move 500 400 click 1
vncdo -s localhost::5900 click 3          # right click
vncdo -s localhost::5900 key enter
vncdo -s localhost::5900 type "example text"
```

Interaction loop:

1. Capture `/tmp/ai-desktop.png`.
2. Inspect the screenshot.
3. Decide what needs to be clicked or typed.
4. Interact through `vncdo`.
5. Capture another screenshot.
6. Verify the result.
7. Repeat as necessary.

Take a new screenshot whenever the GUI changes. Never assume an action succeeded without
checking a new screenshot.

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
| Sodium | `render/` | terrain pipeline, worker meshing, buffers, culling, textures and client presentation; visual tradeoffs allowed after focused validation (current production config still excludes legacy shader backends/compact vertices) | Vintagium (`~/Dev/references/Vintagium`, Sodium 0.4 on 1.12.2 Forge) de-Forged and ported to Mixin 0.7.5 |
| Lithium | `world/`, `ai/` | game-logic optimizations with bit-identical state: collisions, entity lookups, hoppers, tick scheduler, explosions, AI, block-entity ticking, chunk save | carpetmod112 helpers, TISCarpet113 ports, own code from `docs/vanilla-audit.md` |
| FerriteCore | `memory/` | heap: blockstate tables, quad/model dedup, sprite frames, NBT canonicalization, `EntityDataManager`, `ItemStack.isEmpty` | FoamFix / VintageFix 1.12.2 code |

Flow: LiteLoader loads `litemod.json` → registers `mixins.liteoptimizer.json` → the
`IMixinConfigPlugin` (`config/`) reads `liteoptimizer.json` and answers `shouldApplyMixin` per
mixin package → Mixin applies each enabled optimization to its vanilla target at classload. Any
single optimization or whole pillar can be switched off without a rebuild — that is how a user
bisects a suspected parity bug, and how the Sodium pillar falls back to vanilla rendering on a GPU
without GL20/VAO.

Each optimization = one package under `mixin/<pillar>/<name>/` + helpers under `<pillar>/` +
one config key + one row in `docs/catalog.md` with its server-parity/client-boundary argument,
documented client tradeoff (if any), and measured gain.
Optimizations never depend on each other, except that render batch items (culling, textures)
hook Sodium's world renderer when the Sodium pillar is on and vanilla's when it is off.

Pillar packages: `render` (Sodium-like), `world` (Lithium-like), `ai` (Lithium-like), `memory`
(FerriteCore-like), `light` is **not** ours — `Newlight` already exists on LiteLoader; never
conflict with it.

## Configuration

- **Config file:** `<liteloader config dir>/liteoptimizer.json` — flat `{ "<optimization>": true|false }`.
  Missing keys use the `Option` default. Only gain/parity-validated options default on; a `true`
  value for an option still awaiting release evidence is normalized to `false`. Read once at
  startup; changes need a restart (mixins are classload-time).
- **Environment variables:** none.
- **Secrets:** none.

## Compatibility invariants

The owner's instance (`~/.local/share/PrismLauncher/instances/1.12.2 test ai`, 19 litemods:
malilib, litematica, minihud, tweakeroo, itemscroller, Carpet Client ×2, newlight, litetweaker,
UsefulMod, tellme, chatcalc, connect_to_1_12_x, voxelMap, worldeditcui, isorenders,
schematicpreview, BBOutlineReloaded, Model Gap Fix) **must keep working with every release**. Not a goal — a
gate. `docs/compat/checklist.md` is the per-mod smoke list; `docs/compat/instance-mixins.md`
(from `tools/compat-scan.py`) lists the vanilla members the other installed mods mixin into. Rules:

- Grep the target class + method in `instance-mixins.md` before writing a mixin.
- Never `@Overwrite` a method another installed mod injects into; `@Inject`+cancel instead.
- Never remove an invocation another mod `@Redirect`s (`call` rows) from a method we rewrite.
- Lighting methods are Newlight's (`@Overwrite`s) — off-limits.
- During development, verify the foreign hooks affected by the change; do not rerun the entire
  per-mod checklist for every optimization. The litemod must load with the full set and the full
  checklist must pass at the B1 pose before a version is tagged. Track development completion
  separately from release readiness in `PLAN.md`. Use disposable world copies for mutating checks.

## Security invariants

No trust boundary: no network, no file input beyond the config JSON. Only rule: a malformed
config must fall back to defaults, never crash the game.

## Conventions

- **Boundary first.** Before writing a mixin, record whether it is client-only or shared/server
  code in `docs/catalog.md`. Shared/server changes require identical state, RNG, order and
  side effects. Client-only changes require explicit tradeoffs, server isolation, vanilla
  protocol compatibility and unchanged player-action semantics. No argument → do not ship.
- Prefer `@Inject`/`@Redirect` over `@Overwrite`. `@Overwrite` only for a whole-method rewrite,
  and then the vanilla method body is pasted as a comment above for diffing.
- One optimization per package, one config key, same name in both, snake_case:
  `mixin/world/hopper_item_lookup/` ↔ `"hopper_item_lookup"`.
- Hot paths: no allocation, no boxing, no streams, no lambdas capturing state. This is the
  whole point of the mod.
- Client-side only mod; but singleplayer's integrated server runs inside the client, so
  `world`/`ai` mixins do run there. They must be safe when the class is loaded on a dedicated
  server anyway (LiteLoader is client-only, so this is theoretical — don't add server code).
  These existing optimizations may remain if behavior-preserving; new client behavior changes
  must not leak into them. Pure rendering math may change, shared simulation math may not.
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

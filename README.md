# LiteOptimizer

Optimization mod for Minecraft **1.12.2 on LiteLoader**, built for the technical community:
Sodium / Lithium / FerriteCore ideas, one `.litemod`, no dependencies, **vanilla mechanics
unchanged** — every optimization is proven against a bit-identical world-state check before it
ships (`docs/methodology.md`).

## Install

Drop `liteoptimizer-liteloader-1.12.2-<version>.litemod` into `mods/1.12.2/`. Works next to
Litematica, MaLiLib, MiniHUD, Tweakeroo, Carpet Client, Newlight, VoxelMap, … (the full list in
`docs/compat/checklist.md` is tested before every release).

## Configure

`liteconfig/common/liteoptimizer.json` is written on first start with every switch:

```json
{
  "world": true,               // pillar master switches: render, world, ai, memory
  "tile_entity_removal": true, // one key per optimization, described in "_help"
  ...
}
```

Set a key to `false` and restart to turn that optimization off — if you ever suspect one of
changing vanilla behaviour, bisect with these and report it with the world.

## Status

0.3 (Sodium I): the Sodium/Vintagium terrain renderer (`render/sodium`), pixel-identical to
vanilla at a fixed pose — terrain flight frame time −36 %, 1 % low −27 % with the frame cap
off; plus 0.2's 12 game-logic and memory optimizations, all parity-proven on item, hopper,
furnace and mob-farm worlds (hopper hall MSPT −14 %, item farm −39 %, witch farm −30 %). See
`docs/catalog.md` for every row, `docs/bench/README.md` for numbers, `docs/compat/sodium.md`
for what the render pillar changes and what it keeps.

Sodium's own switches live in `liteconfig/common/liteoptimizer-sodium.json`; the defaults are
the parity ones. `use_chunk_multidraw: true` is faster still but a sub-pixel off vanilla.

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
./gradlew build --offline     # → build/libs/*.litemod
```

Developer docs: `AGENTS.md` (architecture, commands), `PLAN.md` (roadmap), `docs/`.
LGPL-3.0 — see `LICENSE` for the upstream projects whose code is ported.

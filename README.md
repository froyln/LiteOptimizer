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

Current development candidate: **0.6.0-rc2**. Fixed-function terrain rendering, item-frame
model display lists, and the existing logic/memory optimizations remain. Entity graph culling,
TE graph occlusion, and generic TE frustum culling are excluded because their gain/parity
proofs were insufficient. Saved settings cannot reactivate the excluded rendering paths.

The candidate passes B3 hopper state parity (600 ticks). Its isolated B1 display-list test
reduces frame average 6.26 → 5.14 ms with zero differing pixels and a zero-pixel unchanged
control. Full release compatibility certification remains pending; this is an untagged candidate.
[Candidate evidence](docs/profiles/0.6.0-rc2-2026-10-13/README.md).

Sodium settings live in `liteconfig/common/liteoptimizer-sodium.json`. Production load/save
forces fixed-function vertices/backend, face/fog/entity graph culling off, and translucency
sorting on. The options UI exposes the retained settings.

## Build

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
./gradlew build --offline     # → build/libs/*.litemod
```

Developer docs: `AGENTS.md` (architecture, commands), `PLAN.md` (roadmap), `docs/`.
LGPL-3.0 — see `LICENSE` for the upstream projects whose code is ported.

# LiteOptimizer

A client optimization mod for **Minecraft 1.12.2 + LiteLoader**. Faster terrain rendering,
less repeated work in item-frame scenes, lower model/blockstate memory use, and targeted
optimizations for singleplayer's integrated server — packaged in one `.litemod` with no
additional runtime dependencies beyond LiteLoader.

## What it optimizes

These nine optimizations are enabled by default:

| Area | Optimization | What changes |
|---|---|---|
| Terrain | `sodium` | Vintagium/Sodium-derived terrain pipeline with worker meshing, chunk visibility traversal and a fixed-function rendering backend. |
| Item frames | `model_display_lists` | Reuses compiled item/block model geometry for item-frame draws instead of tessellating the same models each frame. |
| Block-entity rendering | `tile_entity_frustum` | Skips eligible block-entity draws outside conservative camera bounds. Ticking is unaffected. |
| Camera visibility | `camera_visgraph_cache` | Reuses the camera section's visibility flood fill until the camera block or section data changes. |
| Blockstate memory | `blockstate_property_storage` | Shares an indexed state-transition table per block container instead of a Guava table per state. |
| Model memory | `baked_quad_dedup` | Shares identical baked model quads during model loading. |
| Property lookups | `property_hashcode_cache` | Caches immutable block-property hash codes used by blockstate lookups. |
| Hopper searches | `hopper_entity_lookup` | Maintains per-section inventory-entity lists to narrow hopper searches while preserving candidate order. |
| Structure queries | `structure_lookup_index` | Indexes structure bounds by chunk for spawning-related position queries. |

World logic optimizations affect the **integrated server in singleplayer**. They cannot speed up
a remote server that does not run this mod. Memory savings and rendering improvements apply to
the client. Other candidate switches exist in the source, but remain disabled and are normalized
off in saved configs until their evidence requirements are met.

## Benchmarks

The following are **individual feature OFF → ON comparisons**, not a combined vanilla-versus-mod
score. Other settings and optimizations were held constant within each pair. Results depend on
the scene, render distance, resource packs, hardware and other installed mods; gains do not add
together. Recent runs used a technical LiteLoader mod stack rather than a bare vanilla client.

### Terrain rendering: retained frame-by-frame data

B6 flight fixture, 0.5 blocks/tick forward flight, 20 seconds warmup and 40 seconds recording.
The `sodium` switch was the only optimization setting changed. Recorded on the revision-95
0.6.0-rc2 candidate, October 15, 2026. FPS is calculated from the mean recorded frame time.

| Render distance | Average frame time | Frame time reduction | Average FPS | FPS increase | Slowest 1% mean frame time |
|---|---:|---:|---:|---:|---:|
| 16 chunks | 3.426 → 1.380 ms | **59.7%** | 291.9 → 724.5 | **148.2%** | 11.04 → 5.25 ms (**52.5% lower**) |
| 10 chunks | 0.945 → 0.734 ms | **22.4%** | 1,057.7 → 1,363.0 | **28.9%** | 5.46 → 3.27 ms (**40.2% lower**) |

The RD16 pair contains 11,677 OFF frames and 28,980 ON frames; RD10 contains 42,310 and 54,521.
These are short controlled workload samples, not expected FPS for every world. Long stalls remain:
RD16 maximum frame time was 96.1 → 81.4 ms. The RD10 and RD16 runs are different settings, not
repeats of the same workload. [Raw CSVs, calculations and measurement limits](docs/benchmarks/README.md).

### Other rendering work

| Feature / fixture | Average frame time OFF → ON | Reduction | Slowest 1% mean OFF → ON | Evidence |
|---|---:|---:|---:|---|
| Item-frame display lists / B1 storage | 6.260 → 5.144 ms | **17.8%** | 18.09 → 15.78 ms (12.7% lower) | Retained CSV pair; 20 s warmup / 40 s recording. |
| Block-entity frustum / B1 storage | 6.05 → 4.07 ms | **32.7%** | 23.65 → 9.54 ms (59.7% lower) | Historical measurement report; raw CSV unavailable in this repository. |
| Camera visibility cache / B6 flight | 0.97–0.99 → 0.95–0.96 ms | **3.4% median** across three pairs | **7.7% median reduction** | Historical report; small result near the 3% noise threshold; raw CSV unavailable here. |

The item-frame pair corresponds to approximately **159.8 → 194.4 FPS (+21.7%)**. Its full-image
OFF/ON comparison and an unchanged OFF/OFF control each recorded zero differing pixels at the
fixed test pose. This does not promise exact pixels for every resource pack or scene. Frustum
culling intentionally omits eligible off-screen draws; it does not skip block-entity simulation.

### Memory and integrated-server logic

These are older recorded measurements, with less complete raw-data retention than the terrain
and item-frame pairs. Treat them as workload-specific evidence rather than current whole-mod scores.

| Feature | Workload / metric | Recorded result |
|---|---|---|
| Blockstate property storage | B1 heap after forced GC / class histogram | About **10.5 MB fewer Guava map/table objects**; old-generation occupancy about **14 MB lower** in the recorded run. These are overlapping views, not additive savings. |
| Baked-quad deduplication | B1 model load | **43,690 → 29,214 unique quads**, **33.1% fewer**; approximately **2.2 MB** heap reduction. No FPS gain is claimed. |
| Property hash caching | B3 hopper fixture | Integrated-server mean **3.78 → 3.07 MSPT**, **18.8% lower**; historical report also records average frame time −21% and slowest 1% −50%. |
| Hopper inventory-entity lookup | B3 hopper fixture, isolated option | Integrated-server mean **3.75 → 3.22 MSPT**, **14.1% lower**. |
| Structure lookup index | B4 mob-farm fixture | Historical report records a gain, but its percentage and displayed timings disagree. **No reliable percentage is claimed here.** |

MSPT means milliseconds per simulation tick; lower is better. Reduced MSPT provides more tick
headroom, but does not increase simulation speed above the normal 20 TPS. No remote-server TPS,
combined heap saving or universal speedup is claimed. See [benchmark details](docs/benchmarks/README.md)
for provenance and formulas.

## Install

1. Install Minecraft **1.12.2** with **LiteLoader 1.12.2** and Java 8.
2. Build the mod below, or use an artifact when one is published.
3. Put `liteoptimizer-liteloader-1.12.2.litemod` in your instance's `mods/1.12.2/` folder.
4. Launch the game. No server plugin or mod is needed.

This is a LiteLoader mod, not a Forge `.jar` or Fabric mod. Do not install another Sodium/Vintagium
terrain renderer alongside it. Newlight provides lighting optimization separately; LiteOptimizer
does not replace its lighting engine.

## Configuration

The main config is `liteconfig/common/liteoptimizer.json` in the Minecraft instance. It is written
on first launch; `_help` describes each key. Pillar switches are `render`, `world`, `ai` and `memory`.
For example, this valid JSON disables only the item-frame display-list feature while leaving
missing keys at their defaults:

```json
{
  "model_display_lists": false
}
```

Restart after editing: mixins are selected at startup. Malformed config falls back to defaults.
An existing `true` for a candidate that has not cleared the evidence gate is rewritten to `false`.

Renderer settings are in `liteconfig/common/liteoptimizer-sodium.json` and the video options UI.
The production renderer fixes the backend and vertex format to the conservative fixed-function
path, disables face/fog/entity-graph culling, and keeps translucency sorting on. Legacy shader
backends and compact vertices are excluded from the production configuration.

## Build from source

Requires **JDK 8**. The Gradle 4.10.3 wrapper and ForgeGradle 2.3 do not support running this build
on a modern JDK. Set `JAVA_HOME` to your Java 8 installation:

```bash
export JAVA_HOME=/path/to/jdk8
./gradlew build
```

Output: `build/libs/liteoptimizer-liteloader-1.12.2-0.6.0-rc2.litemod`.
Once dependencies are cached, use `./gradlew build --offline`. A fresh checkout needs network
access to the legacy Forge/Sponge dependencies. Source targets Java 8, MCP `stable_39` and
LiteLoader's Mixin 0.7.5 runtime. No MixinExtras or MaLiLib dependency is required.

## Credits and license

Licensed under **LGPL-3.0-or-later**; bundled components retain their own licenses and copyright
notices. See [LICENSE](LICENSE) and [license texts](licenses/).

The terrain port derives from **Vintagium and Sodium 0.4**, originally by JellySquid/CaffeineMC
and contributors. Other code and ideas draw from **carpetmod112, FoamFix, VintageFix, Lithium,
FerriteCore, VanillaFix, ParticleCulling, Valkyrie and better-biome-blend**. Repackaged **JOML** is
MIT licensed. Upstream authorship and source headers are retained.

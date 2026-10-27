# Argentum method review — 2026-10-14

Argentum describes itself as a Minecraft 1.8.9 client-rendering mod built on the Celeritas
rendering engine. Its published feature list includes terrain meshing, entity and block-entity
instancing/baking, item and GUI rendering, font batching, environment rendering, HUD changes, and
entity/particle occlusion culling. LiteOptimizer already has Celeritas/Sodium-derived terrain
meshing, particle frustum culling, item-frame display lists, block-entity frustum culling, and
camera visibility reuse.

Argentum's own repository says **All Rights Reserved**. Its bundled Celeritas engine is LGPL-3.0,
but that does not license Argentum's additional implementation. This comparison uses feature
descriptions and observed vanilla behavior; no Argentum source code was copied.

| Argentum method | LiteOptimizer fit | Decision |
|---|---|---|
| Terrain meshing | Already supplied by the Sodium-based renderer and worker chunk builder. | No duplicate port. |
| Entity/block-entity instancing and baking | Could reduce repeated model submissions, but needs specialized geometry capture, per-type compatibility, ordering and GL capability handling. Existing item-frame display lists already address the measured B1 draw cost. | Defer until a repeatable dense-entity workload shows renderer CPU as a bottleneck. The current B2 capture had 95 entities and only 0.019 ms/frame in entity draws, so it is not that workload. |
| Entity/particle occlusion | Particle frustum culling already exists. Entity section culling was measured on B1 without moving entities and showed no gain; the B2 capture also lacks its intended dense scene. Argentum's occlusion queries need query lifetime, result delay, GL state and visual validation. | Defer until a correct dense-entity fixture is prepared. |
| Item baking / enchant glint | Item-frame models are already cached in display lists. GUI item rendering and glint were not sampled as meaningful costs in the current profiles. | Defer pending a GUI or enchanted-item workload with measured cost. |
| Font batching | Vanilla 1.12.2 issues an immediate-mode strip per default glyph. The B2 technical HUD spends about 0.12 ms/frame in its information overlay, with JFR samples in Malilib glyph rendering. | Rejected after two OFF/ON runs: the vertex-buffer version was 5.8% slower; a simpler immediate-mode batch was 1.9% slower and had worse 1% lows. The isolated glyph path did not save enough CPU to offset batching overhead. |
| Clouds, rain, snow, HUD/render customization | Many are distance/quality controls that intentionally change output. They do not fit a behavior-preserving optimization by default. | Keep as explicit user-controlled visual tradeoffs, not automatic optimizations. |
| Cera resource-pack extensions and options | Separate content/compatibility project; requires new resource behavior and dependencies. | Outside LiteOptimizer's scope. |

## Font-batch profile evidence

The vanilla profiler measured `InfoOverlay` at about 0.12 ms/frame. Two 30-second OFF/ON
comparisons of the candidate showed no improvement: the first version averaged 0.70 ms OFF versus
0.74 ms ON; the second averaged 0.71 ms OFF versus 0.72 ms ON. The second run's 1% low also fell
from 3.06 ms to 3.25 ms. The candidate has been removed from the production code.

The useful rendering methods retained from this review are `tile_entity_frustum` and
`camera_visgraph_cache`, each independently benchmarked and documented in `docs/catalog.md` and
their profile evidence directories. Font batching and Argentum's entity instancing/occlusion
remain excluded until an appropriate dense-entity workload demonstrates a bottleneck and gain.

Sources: [Argentum README and license statement](https://github.com/QuicksilverMC/Argentum#argentum),
[Celeritas README and license](https://git.taumc.org/embeddedt/celeritas).

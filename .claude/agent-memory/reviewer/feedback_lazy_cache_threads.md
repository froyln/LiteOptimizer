---
name: lazy-cache-threads
description: Reviewing lazily-cached values in mixins — IBlockState/IProperty/model code runs on chunk-render workers + client + integrated-server threads concurrently, so two-field (value + flag) lazy caches are a data race
metadata:
  type: project
---

Lazy caches injected into vanilla objects that are read from several threads (anything reached
from `IBlockState.getValue`, block models, `PropertyEnum.hashCode`, sprite/quad data) must use the
single-field sentinel idiom (`String.hashCode` style: `if (h != 0) return h;`), not
`value` + `boolean computed`.

**Why:** chunk render workers (vanilla `ChunkRenderDispatcher`, Sodium meshers), the client thread and
the integrated server thread all hit these at once; C2 may reorder the two plain stores, so a reader
can see `computed == true` with `value == 0`. For a property hash that means `ImmutableMap.get` misses
→ `getValue` throws → crash. Flagged 2026-09-18 on `world/property_hashcode_cache`.

**How to apply:** on any mixin adding `private X liteoptimizer$cached; private boolean liteoptimizer$has...`
to a class touched by rendering, report as bug with the one-field fix.

---
name: render-parity-coverage
description: What the screenshot-parity harness (tools/bench.sh --no-entities) actually covers — entity render paths are excluded, so "0 differing pixels" proves nothing for minecart/TNT/dropped-item/held-item draws
metadata:
  type: project
---

`tools/bench.sh … --no-entities` screenshots hide every entity except `EntityHanging` (item
frames, paintings, leash knots) — see `mixin/dev/bench/RenderManagerNoEntitiesMixin`. A "0 differing
pixels" claim therefore only covers terrain, block entities and item-frame contents/frames.

**Why:** dropped items spin from an unseeded client random, so entities had to be excluded; but
render optimizations (display lists, model caches) also hook paths only reached from
`RenderMinecart`, `RenderTNTPrimed`, `LayerHeldBlock`, `RenderEntityItem`, held-item rendering. The
owner already saw 1/255 lighting differences on the held-item/GUI display-list path (2026-09-18,
`render/model_display_lists`), so driver-dependent divergence on unmeasured paths is real, not
theoretical.

**How to apply:** for any `render/` optimization, list the vanilla callers of the hooked method
(grep the decompiled sources) and check each against what the screenshot pose can show; report
callers that are cached but not covered as a parity gap. Also note `RenderEntityItem` calls
`RenderItem.renderItem(stack, model)` directly, never `renderItemModel`, so gates keyed on
`TransformType` never see dropped items.

---
name: mixin-prod-checks
description: How to verify a mixin's obfuscated (Notch) targets and refmap when reviewing — srg location, refmap extraction from the built litemod
metadata:
  type: reference
---

Checks that catch "mixin silently does not apply in prod" during review:

- Notch names for aliases / `remap = false` targets: grep
  `~/.gradle/caches/minecraft/de/oceanlabs/mcp/mcp_stable/39/1.12.2/srgs/notch-mcp.srg`
  (`FD:` lines for fields, `MD:` for methods; inner classes look like `awu$a`).
- Refmap actually generated for a mixin:
  `unzip -p build/libs/liteoptimizer-liteloader-1.12.2-<ver>.litemod mixins.liteoptimizer.refmap.json`
  and look under `mappings` for the mixin class. `@Mixin(targets="...$Inner")` mixins get only the
  class name remapped; their members must carry Notch aliases by hand (confirmed 2026-09-18 on
  `blockstate_property_storage`).
- `@Invoker` on a non-private instance method emits INVOKEVIRTUAL (checked in
  `AccessorGeneratorMethodProxy` of mixin 0.7.4: private → INVOKESPECIAL, else INVOKEVIRTUAL), so an
  invoker for an abstract method such as `World.isChunkLoaded` dispatches correctly — not a bug.
- `@Shadow` fields do NOT appear in the refmap; they are reobfed in the mixin class itself via
  `build/tmp/compileJava/mcp-notch.srg` (`FD:` lines). Verify with `javap -p` on the class inside the
  litemod (field shows as e.g. `private bin f;`). Confirmed 2026-09-21 on `RenderItemMixin.itemColors`.
- Refmap check for the current build: `unzip -p build/libs/*.litemod mixins.liteoptimizer.refmap.json | python3 -c "..."`
  and filter `mappings` by package (confirmed 2026-09-18 for hopper_entity_lookup / property_hashcode_cache).
- Decompiled vanilla for the session usually sits in the scratchpad under `mc/net/minecraft/...`
  (extracted from `~/.gradle/caches/minecraft/net/minecraft/minecraft/1.12.2/stable/39/minecraftSrc-1.12.2.jar`).

**How to apply:** run these before trusting "parity PASS" claims on a mixin with `remap = false`
or `targets=`; a failed prod apply shows up as unchanged heap/MSPT, not as a crash, unless the
mixin cancels a vanilla builder (then it crashes with NPE on first use).

# Compatibility checklist — the instance's mod set is a hard requirement

Every release must load next to **all** of these (PrismLauncher instance `1.12.2 test ai`,
`minecraft/mods/1.12.2/`) and every line below must still work. Run it at the B1 pose after every
batch; a failure blocks the release, not the mod that failed.

| mod (file) | what to try | our mixins that touch its targets |
|---|---|---|
| malilib 0.54.0 | config screens open, hotkeys fire | none planned |
| litematica 0.31.4 | load a schematic, placement renders (all layers, translucent + overlay), verifier overlay, paste, material list, schematic browser | `RenderGlobal` (Sodium pillar), `ChunkProviderClient`, `CompiledChunk`, `ViewFrustum`, `Chunk`, `World`, `WorldClient` |
| minihud 0.41.1 | info lines, shapes (spawn sphere), light-level overlay, chunk-unload bookkeeping, structure boxes, spawn chunks, beacon range | `RenderGlobal`, `ChunkProviderServer`, `World`, `WorldClient`, `PathNavigate`, `TileEntityBeacon` |
| tweakeroo 0.31.0 | free camera, `tweakChunkRenderDistance`/`forceChunkRendersOnMainThread`? (its `RenderGlobal.updateChunks` redirect), flexible placement, fast placement/breaking, hand restock, shulker stacking (`EntityItem`), particle disable, TE render distance override, item frame/hologram tweaks | `RenderGlobal.setupTerrain`/`updateChunks` (**direct collision with Sodium**), `ChunkRenderDispatcher`, `ChunkRenderWorker`, `ParticleManager`, `TileEntityRendererDispatcher`, `RenderManager`, `EntityItem`, `Explosion`, `World`, `Entity` |
| itemscroller 0.31.1 | scroll items in a GUI | none |
| Carpet Client 1.4c **and** 1.4d (both present — confirm which is meant to load) | piston preview, `notifyLightSet` hook, `RenderChunkOptifine`, `TileEntityRenderDispatcher`, `BufferBuilder`, `World`/`WorldServer` rules sync | `RenderGlobal.notifyLightSet`, `BufferBuilder` (translucency sort), `World`, `WorldServer`, `EntityLivingBase`, `Entity` |
| newlight 1.0.2 | lighting still correct after block changes; chunk load light | must be zero overlap: `World`, `WorldServer`, `Chunk`, `ChunkProviderServer`, `ExtendedBlockStorage`, `SPacketChunkData`, `AnvilChunkLoader`, `RenderGlobal.notifyLightSet` |
| litetweaker 1.10.4 | its disables/tweaks (beacon beam, vignette, block hit…) | `TileEntityBeaconRenderer`, `Block`, `BlockRenderDispatcher`, `EntityRenderer` |
| UsefulMod 1.1.0 | client commands, item frame/shulker rendering tweaks, `RenderGlobal.isOutlineActive`/`playEvent` | `RenderGlobal`, `RenderItemFrame`, `EntityItem`, `ParticleManager`, `TileEntityRendererDispatcher`, `RenderManager` |
| tellme 0.6.3 | dump commands | `Block`, `Chunk`, `TileEntity` accessors only |
| chatcalc 1.0 | chat math | none |
| connect_to_1_12_x 1.5 | server list | none |
| voxelMap 1.7.1 | minimap renders, `renderSky`/`updateClouds` hooks | `RenderGlobal.renderSky/updateClouds` (Sodium sky mixin) |
| worldeditcui 1.12.1 | selection box renders | `RenderGlobal`? (check refmap — none listed) |
| isorenders 0.1.0, schematicpreview 0.1.0 (own mods) | isometric render; schematic preview widgets | `Framebuffer`/GL state only |
| BBOutlineReloaded 1.0.13 | structure outlines | `Chunk`, `WorldClient`, `WorldServer`, `MinecraftServer` |

The mechanical input for this table is `docs/compat/instance-mixins.md` (638 rows: every vanilla
member those mods inject into or redirect), regenerated with `python3 tools/compat-scan.py`
whenever a mod in the instance is updated. Rules:

1. Before landing any mixin, grep its target class **and** method in `instance-mixins.md`.
2. A foreign `member` row on a method we `@Overwrite` = **forbidden** unless the foreign injection
   is at `HEAD`/`RETURN` and our overwrite keeps a vanilla-shaped body for it to land in. Prefer
   `@Inject` + cancel over `@Overwrite` everywhere a foreign row exists.
3. A foreign `call` row (some mod redirects an invocation inside a method we rewrite) = the
   invocation must still exist in our version with the same owner/name/descriptor, or we hand
   that mod's redirect the same semantics some other way. Tweakeroo's
   `RenderGlobal.updateChunks → ChunkRenderDispatcher.updateChunkNow` and Litematica's
   `EntityRenderer → RenderGlobal.renderBlockLayer/renderEntities/updateChunks` are the known ones
   for the Sodium pillar.
4. Mixin application order across mods is by `priority`; ours default to 1000 like theirs. When a
   foreign mixin must see our change (or must not), set priority explicitly and write why in the
   catalog row.
5. Two `@Overwrite`s on one method (theirs + ours) crash at load — Newlight overwrites lighting
   methods; that is why lighting is off-limits.

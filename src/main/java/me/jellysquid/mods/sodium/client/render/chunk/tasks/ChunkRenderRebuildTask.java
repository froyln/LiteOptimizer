package me.jellysquid.mods.sodium.client.render.chunk.tasks;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkGraphicsState;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderContainer;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildResult;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkMeshData;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderBounds;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.render.pipeline.context.ChunkRenderCacheLocal;
import me.jellysquid.mods.sodium.client.util.MathUtil;
import me.jellysquid.mods.sodium.client.util.task.CancellationSource;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import me.jellysquid.mods.sodium.common.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldType;

/**
 * Rebuilds all the meshes of a chunk for each given render pass with non-occluded blocks. The result is then uploaded
 * to graphics memory on the main thread.
 *
 * This task takes a slice of the world from the thread it is created on. Since these slices require rather large
 * array allocations, they are pooled to ensure that the garbage collector doesn't become overloaded.
 */
public class ChunkRenderRebuildTask<T extends ChunkGraphicsState> extends ChunkRenderBuildTask<T> {
    private static final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();
    private final ChunkRenderContainer<T> render;
    private final long revision;
        
    private final BlockPos offset;

    private final ChunkRenderContext context;

    private Vec3d camera;

    private final boolean translucencySorting;

    public ChunkRenderRebuildTask(ChunkRenderContainer<T> render, long revision, ChunkRenderContext context, BlockPos offset) {
        this.render = render;
        this.revision = revision;
        this.offset = offset;
        this.context = context;
        this.camera = Vec3d.ZERO;
        this.translucencySorting = SodiumClientMod.options().advanced.translucencySorting;
    }

    public ChunkRenderRebuildTask<T> withCameraPosition(Vec3d camera) {
        this.camera = camera;
        return this;
    }

    @Override
    public ChunkBuildResult<T> performBuild(ChunkRenderCacheLocal cache, ChunkBuildBuffers buffers, CancellationSource cancellationSource) {
        // COMPATIBLITY NOTE: Oculus relies on the LVT of this method being unchanged, at least in 16.5
        ChunkRenderData.Builder renderData = new ChunkRenderData.Builder();
        VisGraph occluder = new VisGraph();
        ChunkRenderBounds.Builder bounds = new ChunkRenderBounds.Builder();

        buffers.init(renderData);

        cache.init(this.context);

        WorldSlice slice = cache.getWorldSlice();

        int baseX = this.render.getOriginX();
        int baseY = this.render.getOriginY();
        int baseZ = this.render.getOriginZ();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos renderOffset = this.offset;

        try {
            // LiteOptimizer: vanilla's BlockPos.getAllInBoxMutable order (x fastest, then y, then z) —
            // the translucent sort is stable, so the input order decides ties
            for (int relZ = 0; relZ < 16; relZ++) {
                if (cancellationSource.isCancelled()) {
                    return null;
                }

                for (int relY = 0; relY < 16; relY++) {
                    for (int relX = 0; relX < 16; relX++) {
                        IBlockState blockState = slice.getBlockStateRelative(relX + 16, relY + 16, relZ + 16);
                        Block block = blockState.getBlock();

                        // If the block is vanilla air, assume it renders nothing. Don't use isAir because mods
                        // can abuse it for all sorts of things
                        if (blockState.getMaterial() == Material.AIR) {
                            continue;
                        }

                        EnumBlockRenderType renderType = blockState.getRenderType();

                        pos.setPos(baseX + relX, baseY + relY, baseZ + relZ);
                        buffers.setRenderOffset(pos.getX() - renderOffset.getX(), pos.getY() - renderOffset.getY(), pos.getZ() - renderOffset.getZ());

                        if(renderType != EnumBlockRenderType.INVISIBLE) {
                            if (slice.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                                blockState = blockState.getActualState(slice, pos);
                            }

                            // LiteOptimizer: vanilla has one render layer per block (Block.getRenderLayer), no
                            // Forge canRenderInLayer / extended state. The model comes from
                            // BlockRendererDispatcher.getModelForState so mods that hook it (Tweakeroo,
                            // LiteTweaker) keep affecting terrain — see docs/compat/sodium.md.
                            {
                                BlockRenderLayer layer = block.getRenderLayer();

                                if (renderType == EnumBlockRenderType.MODEL && WorldUtil.toFluidBlock(block) == null) {
                                    IBakedModel model = cache.getBlockRenderDispatcher().getModelForState(blockState);

                                    // vanilla adds (blockPos + state offset) to every vertex as a double then
                                    // rounds once to float (BufferBuilder.putPosition)
                                    Vec3d stateOffset = blockState.getOffset(slice, pos);
                                    buffers.setRenderOffset(((double) pos.getX() + stateOffset.x) - renderOffset.getX(),
                                            ((double) pos.getY() + stateOffset.y) - renderOffset.getY(),
                                            ((double) pos.getZ() + stateOffset.z) - renderOffset.getZ());

                                    final long seed = net.minecraft.util.math.MathHelper.getPositionRandom(pos); // vanilla's seed for IBakedModel.getQuads

                                    if (cache.getBlockRenderer().renderModel(cache.getLocalSlice(), blockState, pos, model, buffers.get(layer), true, seed)) {
                                        bounds.addBlock(relX, relY, relZ);
                                    }

                                } else if (WorldUtil.toFluidBlock(block) != null) {
                                    buffers.setRenderOffset(0, 0, 0); // the fluid renderer writes render-relative floats itself
                                    if (cache.getFluidRenderer().render(cache.getLocalSlice(), blockState, pos, buffers.get(layer), renderOffset)) {
                                        bounds.addBlock(relX, relY, relZ);
                                    }
                                }
                            }
                        }

                        if (block.hasTileEntity()) {
                            TileEntity entity = slice.getTileEntity(pos);

                            if (entity != null) {
                                TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(entity);

                                if (renderer != null) {
                                    // vanilla RenderChunk.rebuildChunk: every renderable block entity goes into
                                    // the section's list, and a global one (beacon, end gateway) into the global
                                    // set as well — so vanilla draws a beacon beam twice per frame (the additive
                                    // glow is brighter); same here, or the pixels differ
                                    renderData.addBlockEntity(entity, true);
                                    if (renderer.isGlobalRenderer(entity)) {
                                        renderData.addBlockEntity(entity, false);
                                    }

                                    bounds.addBlock(relX, relY, relZ);
                                }
                            }
                        }

                        if (blockState.isOpaqueCube()) {
                            occluder.setOpaqueCube(pos);
                        }
                    }
                }
            }
        } catch (ReportedException ex) {
            // Propagate existing crashes (add context)
            throw fillCrashInfo(ex.getCrashReport(), slice, pos);
        } catch (Throwable ex) {
            // Create a new crash report for other exceptions (e.g. thrown in getQuads)
            throw fillCrashInfo(CrashReport.makeCrashReport(ex, "Encountered exception while building chunk meshes"), slice, pos);
        }


        render.setRebuildForTranslucents(false);
        for (BlockRenderPass pass : BlockRenderPass.VALUES) {
            ChunkMeshData mesh = buffers.createMesh(pass, (float)camera.x - offset.getX(), (float)camera.y - offset.getY(), (float)camera.z - offset.getZ(), this.translucencySorting);

            if (mesh != null) {
                if (dev.froyln.liteoptimizer.dev.VertexDump.ENABLED) {
                    dev.froyln.liteoptimizer.dev.VertexDump.dump(this.render.getOriginX(), this.render.getOriginY(), this.render.getOriginZ(), pass.getLayer().name(), mesh.getVertexData().buffer);
                }
                renderData.setMesh(pass, mesh);
                if(this.translucencySorting && pass.isTranslucent())
                    render.setRebuildForTranslucents(true);
            }
        }

        renderData.setOcclusionData(occluder.computeVisibility());
        renderData.setBounds(bounds.build(this.render.getChunkPos()));

        return new ChunkBuildResult<>(this.render, this.revision, renderData.build());
    }

    private ReportedException fillCrashInfo(CrashReport report, WorldSlice slice, BlockPos pos) {
        CrashReportCategory crashReportSection = report.makeCategoryDepth("Block being rendered", 1);

        IBlockState state = null;
        try {
            state = slice.getBlockState(pos);
        } catch (Exception ignored) {}
        CrashReportCategory.addBlockInfo(crashReportSection, pos, state);

        crashReportSection.addCrashSection("Chunk section", render);
        if (context != null) {
            crashReportSection.addCrashSection("Render context volume", context.getVolume());
        }

        return new ReportedException(report);
    }

    @Override
    public void releaseResources() {
        this.context.releaseResources();
    }
}

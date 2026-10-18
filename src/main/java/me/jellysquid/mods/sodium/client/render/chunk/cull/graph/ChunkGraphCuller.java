package me.jellysquid.mods.sodium.client.render.chunk.cull.graph;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumSet;
import java.util.Set;
import dev.froyln.liteoptimizer.config.Config;
import dev.froyln.liteoptimizer.config.Option;
import me.jellysquid.mods.sodium.client.render.chunk.cull.ChunkCuller;
import me.jellysquid.mods.sodium.client.util.math.ChunkSectionPos;
import me.jellysquid.mods.sodium.client.util.math.FrustumExtended;
import me.jellysquid.mods.sodium.common.util.DirectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


public class ChunkGraphCuller implements ChunkCuller {
    private final Long2ObjectMap<ChunkGraphNode> nodes = new Long2ObjectOpenHashMap<>();

    private final ChunkGraphIterationQueue visible = new ChunkGraphIterationQueue();
    private final World world;
    private final int renderDistance;

    private FrustumExtended frustum;
    private boolean useOcclusionCulling;

    private int activeFrame = 0;
    private int centerChunkX, centerChunkY, centerChunkZ;
    // Store exits as bits because initSearch removes a facing from the mutable set it receives.
    private boolean cameraVisibilityCached;
    private long cachedCameraBlock;
    private int cachedCameraSectionX, cachedCameraSectionY, cachedCameraSectionZ;
    private int cachedCameraFacings;

    public ChunkGraphCuller(World world, int renderDistance) {
        this.world = world;
        this.renderDistance = renderDistance;
    }

    /**
     * LiteOptimizer: this culler reproduces vanilla RenderGlobal.setupTerrain's graph search exactly —
     * same roots, same neighbour order, first path wins (no merging of culling state from later
     * parents), same chunk-in-a-solid-block rule, same wrapped root enumeration when the camera is
     * outside the world's height — so the set and the order of rendered sections are vanilla's.
     * Vanilla's search over-culls in places (a section reachable only through a second path is
     * skipped); that is kept on purpose: the pixels are the same.
     */
    @Override
    public IntArrayList computeVisible(Vec3d cameraPos, FrustumExtended frustum, int frame, boolean spectator) {
        boolean search = this.initSearch(cameraPos, frustum, frame, spectator);

        ChunkGraphIterationQueue queue = this.visible;

        for (int i = 0; search && i < queue.size(); i++) {
            ChunkGraphNode node = queue.getNode(i);
            short cullData = node.computeQueuePop();

            for (EnumFacing dir : DirectionUtil.ALL_DIRECTIONS) {
                if (useOcclusionCulling && (cullData & (1 << dir.ordinal())) == 0) {
                    continue;
                }

                ChunkGraphNode adj = node.getConnectedNode(dir);

                if (adj != null && this.isWithinRenderDistance(adj)) {
                    this.bfsEnqueue(node, adj, dir.getOpposite(), cullData);
                }
            }
        }

        return this.visible.getOrderedIdList();
    }

    /**
     * vanilla RenderGlobal.getRenderChunkOffset: the neighbour must be in the ViewFrustum grid and
     * within renderDistance * 16 blocks of the camera's chunk origin. The grid is (2·RD+1) chunks
     * wide but centred on {@code floor(playerX) - 8}, i.e. it covers origins
     * {@code [floor(px) - 16·RD - 16, floor(px) + 16·RD)}: half a chunk off-centre, so the +RD ring
     * is only rendered while the player stands past the first block of their chunk.
     */
    private boolean isWithinRenderDistance(ChunkGraphNode adj) {
        int ox = adj.getChunkX() << 4;
        int oz = adj.getChunkZ() << 4;

        if (ox < this.gridMinX || ox >= this.gridMinX + this.gridSize || oz < this.gridMinZ || oz >= this.gridMinZ + this.gridSize) {
            return false;
        }

        return Math.abs((this.centerChunkX << 4) - ox) <= this.renderDistance * 16
                && Math.abs((this.centerChunkZ << 4) - oz) <= this.renderDistance * 16;
    }

    // vanilla ViewFrustum.updateChunkPositions, applied by setupTerrain when the view entity changed
    // chunk or moved more than 4 blocks since the last time (frustumUpdatePos*)
    private int gridMinX, gridMinZ, gridSize;
    private double frustumUpdatePosX, frustumUpdatePosY, frustumUpdatePosZ;
    private int frustumUpdatePosChunkX = Integer.MIN_VALUE, frustumUpdatePosChunkY, frustumUpdatePosChunkZ;

    private void updateGrid() {
        net.minecraft.entity.Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        double d0 = viewEntity.posX - this.frustumUpdatePosX;
        double d1 = viewEntity.posY - this.frustumUpdatePosY;
        double d2 = viewEntity.posZ - this.frustumUpdatePosZ;

        if (this.frustumUpdatePosChunkX != viewEntity.chunkCoordX || this.frustumUpdatePosChunkY != viewEntity.chunkCoordY
                || this.frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D) {
            this.frustumUpdatePosX = viewEntity.posX;
            this.frustumUpdatePosY = viewEntity.posY;
            this.frustumUpdatePosZ = viewEntity.posZ;
            this.frustumUpdatePosChunkX = viewEntity.chunkCoordX;
            this.frustumUpdatePosChunkY = viewEntity.chunkCoordY;
            this.frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;

            this.gridSize = (this.renderDistance * 2 + 1) * 16;
            this.gridMinX = MathHelper.floor(viewEntity.posX) - 8 - this.gridSize / 2;
            this.gridMinZ = MathHelper.floor(viewEntity.posZ) - 8 - this.gridSize / 2;
        }
    }

    /** @return whether the graph is searched from the roots (vanilla: false when the camera sits in a closed pocket) */
    private boolean initSearch(Vec3d cameraPos, FrustumExtended frustum, int frame, boolean spectator) {
        this.activeFrame = frame;
        this.frustum = frustum;
        this.useOcclusionCulling = Minecraft.getMinecraft().renderChunksMany;

        this.visible.clear();
        this.updateGrid();

        BlockPos origin = new BlockPos(cameraPos.x, cameraPos.y, cameraPos.z);

        int chunkX = origin.getX() >> 4;
        int chunkY = origin.getY() >> 4;
        int chunkZ = origin.getZ() >> 4;

        this.centerChunkX = chunkX;
        this.centerChunkY = chunkY;
        this.centerChunkZ = chunkZ;

        // vanilla: ViewFrustum.getRenderChunk(camera) is null only outside the world's height
        if (chunkY >= 0 && chunkY < 16) {
            ChunkGraphNode rootNode = this.getNode(chunkX, chunkY, chunkZ);

            if (rootNode == null) {
                // the camera's section is not loaded yet: vanilla searches from a DUMMY compiled chunk
                // that is visible through nothing, i.e. nothing renders until it is built
                return false;
            }

            rootNode.resetCullingState();
            rootNode.setLastVisibleFrame(frame);
            this.visible.add(rootNode);

            // vanilla: flood fill from the camera block through its section; a lone exit facing
            // behind the view is dropped; no exit → only the camera's section (unless spectator)
            java.util.Set<EnumFacing> exits = this.getVisibleFacings(origin);

            if (exits.size() == 1) {
                exits.remove(this.getViewFacing().getOpposite());
            }

            if (exits.isEmpty() && !spectator) {
                return false;
            }

            if (spectator && this.world.getBlockState(origin).isOpaqueCube()) {
                this.useOcclusionCulling = false;
            }
        } else {
            chunkY = origin.getY() > 0 ? 15 : 0;

            // vanilla enumerates ViewFrustum.getRenderChunk((j << 4) + 8, y, (k << 4) + 8) for j, k in
            // ±renderDistance — absolute positions that the grid wraps modulo its width, so every
            // section of the grid row is visited once, in that wrapped order; no sorting
            for (int j = -this.renderDistance; j <= this.renderDistance; ++j) {
                int x = (this.gridMinX + Math.floorMod((j << 4) - this.gridMinX, this.gridSize)) >> 4;

                for (int k = -this.renderDistance; k <= this.renderDistance; ++k) {
                    int z = (this.gridMinZ + Math.floorMod((k << 4) - this.gridMinZ, this.gridSize)) >> 4;
                    ChunkGraphNode node = this.getNode(x, chunkY, z);

                    if (node == null || node.isCulledByFrustum(frustum)) {
                        continue;
                    }

                    node.resetCullingState();
                    node.setLastVisibleFrame(frame);

                    this.visible.add(node);
                }
            }
        }

        return true;
    }

    /** vanilla RenderGlobal.getVisibleFacings */
    private Set<EnumFacing> getVisibleFacings(BlockPos pos) {
        boolean cacheEnabled = Config.isActive(Option.CAMERA_VISGRAPH_CACHE);
        long cameraBlock = pos.toLong();
        if (cacheEnabled && this.cameraVisibilityCached && this.cachedCameraBlock == cameraBlock) {
            EnumSet<EnumFacing> facings = EnumSet.noneOf(EnumFacing.class);
            for (EnumFacing facing : DirectionUtil.ALL_DIRECTIONS) {
                if ((this.cachedCameraFacings & (1 << facing.ordinal())) != 0) {
                    facings.add(facing);
                }
            }
            return facings;
        }

        net.minecraft.client.renderer.chunk.VisGraph visgraph = new net.minecraft.client.renderer.chunk.VisGraph();
        BlockPos blockpos = new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
        net.minecraft.world.chunk.Chunk chunk = this.world.getChunk(blockpos);

        for (BlockPos.MutableBlockPos p : BlockPos.getAllInBoxMutable(blockpos, blockpos.add(15, 15, 15))) {
            if (chunk.getBlockState(p).isOpaqueCube()) {
                visgraph.setOpaqueCube(p);
            }
        }

        Set<EnumFacing> facings = visgraph.getVisibleFacings(pos);
        if (cacheEnabled) {
            int mask = 0;
            for (EnumFacing facing : facings) {
                mask |= 1 << facing.ordinal();
            }
            this.cachedCameraBlock = cameraBlock;
            this.cachedCameraSectionX = pos.getX() >> 4;
            this.cachedCameraSectionY = pos.getY() >> 4;
            this.cachedCameraSectionZ = pos.getZ() >> 4;
            this.cachedCameraFacings = mask;
            this.cameraVisibilityCached = true;
        }
        return facings;
    }

    public void invalidateCameraVisibility(int x, int y, int z) {
        if (this.cameraVisibilityCached && this.cachedCameraSectionX == x
                && this.cachedCameraSectionY == y && this.cachedCameraSectionZ == z) {
            this.cameraVisibilityCached = false;
        }
    }

    /** vanilla RenderGlobal.getViewVector → EnumFacing.getFacingFromVector, for the render view entity */
    private EnumFacing getViewFacing() {
        Minecraft mc = Minecraft.getMinecraft();
        net.minecraft.entity.Entity entity = mc.getRenderViewEntity();
        double partialTicks = mc.getRenderPartialTicks();
        float f = (float) ((double) entity.prevRotationPitch + (double) (entity.rotationPitch - entity.prevRotationPitch) * partialTicks);
        float f1 = (float) ((double) entity.prevRotationYaw + (double) (entity.rotationYaw - entity.prevRotationYaw) * partialTicks);

        if (mc.gameSettings.thirdPersonView == 2) {
            f += 180.0F;
        }

        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        return EnumFacing.getFacingFromVector(f3 * f4, f5, f2 * f4);
    }


    private void bfsEnqueue(ChunkGraphNode parent, ChunkGraphNode node, EnumFacing flow, short parentalData) {
        if (node.getLastVisibleFrame() == this.activeFrame) {
            // vanilla: a section is queued by its first parent only (RenderChunk.setFrameIndex)
            return;
        }
        node.setLastVisibleFrame(this.activeFrame);

        if (node.isCulledByFrustum(this.frustum)) {
            return;
        }

        node.setCullingState(parentalData);
        node.updateCullingState(flow, parentalData);

        this.visible.add(node);
    }

    private void connectNeighborNodes(ChunkGraphNode node) {
        for (EnumFacing dir : DirectionUtil.ALL_DIRECTIONS) {
            ChunkGraphNode adj = this.findAdjacentNode(node, dir);

            if (adj != null) {
                adj.setAdjacentNode(dir.getOpposite(), node);
            }

            node.setAdjacentNode(dir, adj);
        }
    }

    private void disconnectNeighborNodes(ChunkGraphNode node) {
        for (EnumFacing dir : DirectionUtil.ALL_DIRECTIONS) {
            ChunkGraphNode adj = node.getConnectedNode(dir);

            if (adj != null) {
                adj.setAdjacentNode(dir.getOpposite(), null);
            }

            node.setAdjacentNode(dir, null);
        }
    }

    private ChunkGraphNode findAdjacentNode(ChunkGraphNode node, EnumFacing dir) {
        return this.getNode(node.getChunkX() + dir.getXOffset(), node.getChunkY() + dir.getYOffset(), node.getChunkZ() + dir.getZOffset());
    }

    private ChunkGraphNode getNode(int x, int y, int z) {
        return this.nodes.get(ChunkSectionPos.asLong(x, y, z));
    }

    @Override
    public void onSectionStateChanged(int x, int y, int z, SetVisibility occlusionData) {
        this.invalidateCameraVisibility(x, y, z);
        ChunkGraphNode node = this.getNode(x, y, z);

        if (node != null) {
            node.setOcclusionData(occlusionData);
        }
    }

    @Override
    public void onSectionLoaded(int x, int y, int z, int id) {
        this.invalidateCameraVisibility(x, y, z);
        ChunkGraphNode node = new ChunkGraphNode(x, y, z, id);
        ChunkGraphNode prev;

        if ((prev = this.nodes.put(ChunkSectionPos.asLong(x, y, z), node)) != null) {
            this.disconnectNeighborNodes(prev);
        }

        this.connectNeighborNodes(node);
    }

    @Override
    public void onSectionUnloaded(int x, int y, int z) {
        this.invalidateCameraVisibility(x, y, z);
        ChunkGraphNode node = this.nodes.remove(ChunkSectionPos.asLong(x, y, z));

        if (node != null) {
            this.disconnectNeighborNodes(node);
        }
    }

    @Override
    public boolean isSectionVisible(int x, int y, int z) {
        ChunkGraphNode render = this.getNode(x, y, z);

        if (render == null) {
            return false;
        }

        return render.getLastVisibleFrame() == this.activeFrame;
    }
}

package dev.froyln.liteoptimizer.mixin.world.block_collisions;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Catalog row world/block_collisions (audit A2; after masa's Carpet blockCollisionsOptimization).
 *
 * Vanilla's block-collision scan resolves the chunk twice per block: isBlockLoaded per column
 * and World.getBlockState (→ getChunk hash lookup) per block. This is the same scan with the
 * chunk resolved once per column and reused while the column stays in it; it visits the same
 * blocks in the same x → z → y order, applies the same edge-skip and world-border rules, and calls
 * the same addCollisionBoxToList, so the output list is identical. Chunk.getBlockState returns
 * AIR outside 0..255 exactly like World.getBlockState, and getChunk on a loaded chunk has the
 * same unloadQueued side effect vanilla's per-block getChunk had.
 */
@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract WorldBorder getWorldBorder();
    @Shadow public abstract boolean isInsideWorldBorder(Entity entity);
    @Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);
    @Shadow protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;)Z", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$collisionBoxesPerChunk(@Nullable Entity entityIn, AxisAlignedBB aabb, boolean checkOnly, @Nullable List<AxisAlignedBB> outList, CallbackInfoReturnable<Boolean> cir) {
        // note: vanilla evaluates worldborder.contains(pos) only when !checkOnly; the hoisted form is
        // evaluated unconditionally but is side-effect free, so the observable behaviour is identical
        int i = MathHelper.floor(aabb.minX) - 1;
        int j = MathHelper.ceil(aabb.maxX) + 1;
        int k = MathHelper.floor(aabb.minY) - 1;
        int l = MathHelper.ceil(aabb.maxY) + 1;
        int i1 = MathHelper.floor(aabb.minZ) - 1;
        int j1 = MathHelper.ceil(aabb.maxZ) + 1;
        WorldBorder worldborder = this.getWorldBorder();
        boolean flag = entityIn != null && entityIn.isOutsideBorder();
        boolean flag1 = entityIn != null && this.isInsideWorldBorder(entityIn);
        IBlockState stone = Blocks.STONE.getDefaultState();
        // WorldBorder.contains recomputes min/max (4 × diameter math) per block; while the border is
        // stationary those are constants for the whole call, so hoist them (exact same doubles).
        // A moving border reads the wall clock inside getDiameter(), so keep vanilla's per-block call.
        boolean stationary = worldborder.getStatus() == net.minecraft.world.border.EnumBorderStatus.STATIONARY;
        double bMinX = stationary ? worldborder.minX() : 0;
        double bMaxX = stationary ? worldborder.maxX() : 0;
        double bMinZ = stationary ? worldborder.minZ() : 0;
        double bMaxZ = stationary ? worldborder.maxZ() : 0;
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        World self = (World) (Object) this;
        int lastCx = Integer.MIN_VALUE;
        int lastCz = Integer.MIN_VALUE;
        Chunk chunk = null;
        try {
            for (int x = i; x < j; ++x) {
                for (int z = i1; z < j1; ++z) {
                    boolean xIsEdge = x == i || x == j - 1;
                    boolean zIsEdge = z == i1 || z == j1 - 1;
                    if (xIsEdge && zIsEdge) {
                        continue;
                    }
                    int cx = x >> 4;
                    int cz = z >> 4;
                    if (cx != lastCx || cz != lastCz) {
                        lastCx = cx;
                        lastCz = cz;
                        chunk = this.isChunkLoaded(cx, cz, false) ? this.getChunk(cx, cz) : null;
                    }
                    if (chunk == null) {
                        continue;
                    }
                    for (int y = k; y < l; ++y) {
                        if (!xIsEdge && !zIsEdge || y != l - 1) {
                            if (checkOnly) {
                                if (x < -30000000 || x >= 30000000 || z < -30000000 || z >= 30000000) {
                                    cir.setReturnValue(true);
                                    return;
                                }
                            } else if (entityIn != null && flag == flag1) {
                                entityIn.setOutsideBorder(!flag1);
                            }
                            pos.setPos(x, y, z);
                            IBlockState state;
                            boolean inBorder = stationary
                                    ? (double) (x + 1) > bMinX && (double) x < bMaxX && (double) (z + 1) > bMinZ && (double) z < bMaxZ
                                    : worldborder.contains(pos);
                            if (!checkOnly && !inBorder && flag1) {
                                state = stone;
                            } else {
                                state = chunk.getBlockState(pos);
                            }
                            state.addCollisionBoxToList(self, pos, aabb, outList, entityIn, false);
                            if (checkOnly && !outList.isEmpty()) {
                                cir.setReturnValue(true);
                                return;
                            }
                        }
                    }
                }
            }
        } finally {
            pos.release();
        }
        cir.setReturnValue(!outList.isEmpty());
    }
}

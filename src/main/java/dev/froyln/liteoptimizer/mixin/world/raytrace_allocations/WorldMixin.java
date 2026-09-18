package dev.froyln.liteoptimizer.mixin.world.raytrace_allocations;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Catalog row world/raytrace_allocations (audit E8/A-side canEntityBeSeen; Lithium world.raycast).
 *
 * World.rayTraceBlocks allocates a BlockPos and a Vec3d on every step of the walk (up to 200),
 * for mob line-of-sight checks and the per-frame mouse-over. This is the same walk with the
 * position kept in a mutable BlockPos and the current point kept in three doubles; immutable
 * objects are created only where vanilla hands them to block code or stores them in a result.
 *
 * Parity: every arithmetic expression, comparison and branch is vanilla's, in the same order,
 * on the same doubles; the block-state reads happen for the same positions; the results are
 * built from the same values. Only object identity of intermediates changed.
 */
@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);

    @Inject(method = "rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$rayTrace(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, CallbackInfoReturnable<RayTraceResult> cir) {
        World self = (World) (Object) this;
        if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)
                || Double.isNaN(vec32.x) || Double.isNaN(vec32.y) || Double.isNaN(vec32.z)) {
            cir.setReturnValue(null);
            return;
        }
        int i = MathHelper.floor(vec32.x);
        int j = MathHelper.floor(vec32.y);
        int k = MathHelper.floor(vec32.z);
        int l = MathHelper.floor(vec31.x);
        int i1 = MathHelper.floor(vec31.y);
        int j1 = MathHelper.floor(vec31.z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(l, i1, j1);
        IBlockState iblockstate = this.getBlockState(pos);
        Block block = iblockstate.getBlock();
        if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(self, pos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
            RayTraceResult raytraceresult = iblockstate.collisionRayTrace(self, pos.toImmutable(), vec31, vec32);
            if (raytraceresult != null) {
                cir.setReturnValue(raytraceresult);
                return;
            }
        }
        // the current point of the walk, vanilla's vec31 after each step
        double x = vec31.x;
        double y = vec31.y;
        double z = vec31.z;
        Vec3d current = vec31;
        RayTraceResult raytraceresult2 = null;
        int k1 = 200;
        while (k1-- >= 0) {
            if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
                cir.setReturnValue(null);
                return;
            }
            if (l == i && i1 == j && j1 == k) {
                cir.setReturnValue(returnLastUncollidableBlock ? raytraceresult2 : null);
                return;
            }
            boolean flag2 = true;
            boolean flag = true;
            boolean flag1 = true;
            double d0 = 999.0D;
            double d1 = 999.0D;
            double d2 = 999.0D;
            if (i > l) {
                d0 = (double) l + 1.0D;
            } else if (i < l) {
                d0 = (double) l + 0.0D;
            } else {
                flag2 = false;
            }
            if (j > i1) {
                d1 = (double) i1 + 1.0D;
            } else if (j < i1) {
                d1 = (double) i1 + 0.0D;
            } else {
                flag = false;
            }
            if (k > j1) {
                d2 = (double) j1 + 1.0D;
            } else if (k < j1) {
                d2 = (double) j1 + 0.0D;
            } else {
                flag1 = false;
            }
            double d3 = 999.0D;
            double d4 = 999.0D;
            double d5 = 999.0D;
            double d6 = vec32.x - x;
            double d7 = vec32.y - y;
            double d8 = vec32.z - z;
            if (flag2) {
                d3 = (d0 - x) / d6;
            }
            if (flag) {
                d4 = (d1 - y) / d7;
            }
            if (flag1) {
                d5 = (d2 - z) / d8;
            }
            if (d3 == -0.0D) {
                d3 = -1.0E-4D;
            }
            if (d4 == -0.0D) {
                d4 = -1.0E-4D;
            }
            if (d5 == -0.0D) {
                d5 = -1.0E-4D;
            }
            EnumFacing enumfacing;
            if (d3 < d4 && d3 < d5) {
                enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                double ny = y + d7 * d3;
                double nz = z + d8 * d3;
                x = d0;
                y = ny;
                z = nz;
            } else if (d4 < d5) {
                enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                double nx = x + d6 * d4;
                double nz = z + d8 * d4;
                x = nx;
                y = d1;
                z = nz;
            } else {
                enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                double nx = x + d6 * d5;
                double ny = y + d7 * d5;
                x = nx;
                y = ny;
                z = d2;
            }
            current = null;
            l = MathHelper.floor(x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
            i1 = MathHelper.floor(y) - (enumfacing == EnumFacing.UP ? 1 : 0);
            j1 = MathHelper.floor(z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
            pos.setPos(l, i1, j1);
            IBlockState iblockstate1 = this.getBlockState(pos);
            Block block1 = iblockstate1.getBlock();
            if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(self, pos) != Block.NULL_AABB) {
                if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                    if (current == null) {
                        current = new Vec3d(x, y, z);
                    }
                    RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(self, pos.toImmutable(), current, vec32);
                    if (raytraceresult1 != null) {
                        cir.setReturnValue(raytraceresult1);
                        return;
                    }
                } else {
                    if (current == null) {
                        current = new Vec3d(x, y, z);
                    }
                    raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, current, enumfacing, pos.toImmutable());
                }
            }
        }
        cir.setReturnValue(returnLastUncollidableBlock ? raytraceresult2 : null);
    }
}

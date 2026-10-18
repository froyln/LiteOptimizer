package dev.froyln.liteoptimizer.mixin.world.entity_fluid_checks;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.froyln.liteoptimizer.world.IFluidCountedStorage;

/**
 * Catalog row world/entity_fluid_checks (audit A1).
 *
 * Every entity scans the blocks around it for water and then for lava on every tick
 * (handleWaterMovement / isInLava → handleMaterialAcceleration / isMaterialInBB). When every
 * chunk section the box touches is loaded and holds zero blocks of that material, the scan
 * cannot find anything and is skipped.
 *
 * Parity: the skipped scan only reads; its result would be false and its side effects
 * (velocity change, entity flags) only happen on true. If any chunk in range is not loaded the
 * vanilla method runs unchanged, so its side effects on unloaded chunks (isAreaLoaded → false, or
 * a chunk load through getBlockState) stay exactly as they were. Other materials fall through.
 */
@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow protected net.minecraft.world.chunk.IChunkProvider chunkProvider;

    /** True iff every section in the block range is loaded and has no block of the material. */
    private boolean liteoptimizer$provablyNone(Material material, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        boolean water = material == Material.WATER;
        if (!water && material != Material.LAVA) {
            return false;
        }
        int sy0 = Math.max(minY, 0) >> 4;
        int sy1 = Math.min(maxY - 1, 255) >> 4;
        for (int cx = minX >> 4; cx <= (maxX - 1) >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= (maxZ - 1) >> 4; cz++) {
                // one lookup instead of isChunkLoaded + getChunk; null ⇔ vanilla's isChunkLoaded(…, false) == false
                Chunk chunk = this.chunkProvider.getLoadedChunk(cx, cz);
                if (chunk == null) {
                    return false;
                }
                ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
                for (int sy = sy0; sy <= sy1; sy++) {
                    ExtendedBlockStorage s = sections[sy];
                    if (s == Chunk.NULL_BLOCK_STORAGE) {
                        continue;
                    }
                    IFluidCountedStorage counted = (IFluidCountedStorage) s;
                    if ((water ? counted.liteoptimizer$waterCount() : counted.liteoptimizer$lavaCount()) != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Inject(method = "handleMaterialAcceleration", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$skipAcceleration(AxisAlignedBB bb, Material material, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (liteoptimizer$provablyNone(material, MathHelper.floor(bb.minX), MathHelper.floor(bb.minY), MathHelper.floor(bb.minZ),
                MathHelper.ceil(bb.maxX), MathHelper.ceil(bb.maxY), MathHelper.ceil(bb.maxZ))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isMaterialInBB", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$skipMaterialScan(AxisAlignedBB bb, Material material, CallbackInfoReturnable<Boolean> cir) {
        if (liteoptimizer$provablyNone(material, MathHelper.floor(bb.minX), MathHelper.floor(bb.minY), MathHelper.floor(bb.minZ),
                MathHelper.ceil(bb.maxX), MathHelper.ceil(bb.maxY), MathHelper.ceil(bb.maxZ))) {
            cir.setReturnValue(false);
        }
    }
}

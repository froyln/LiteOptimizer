package dev.froyln.liteoptimizer.mixin.render.sodium.features.chunk_rendering;

import me.jellysquid.mods.sodium.client.world.SodiumBlockAccess;
import me.jellysquid.mods.sodium.client.world.biome.ColorResolver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * LiteOptimizer: the three public entry points route a chunk-build WorldSlice to its per-section
 * memo (BiomeColorCache); any other IBlockAccess gets vanilla's 3×3 average, re-stated here with
 * our resolver type because vanilla's is package-private. Same integer math either way.
 */
@Mixin(value = BiomeColorHelper.class, priority = 500)
public class MixinBiomeColorHelper {
    private static int liteoptimizer$colorAtPos(IBlockAccess blockAccess, BlockPos pos, ColorResolver resolver) {
        if (blockAccess instanceof SodiumBlockAccess) {
            return ((SodiumBlockAccess) blockAccess).getBlockTint(pos, resolver);
        }
        int i = 0;
        int j = 0;
        int k = 0;
        for (BlockPos.MutableBlockPos p : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
            int l = resolver.getColorAtPos(blockAccess.getBiome(p), p);
            i += (l & 16711680) >> 16;
            j += (l & 65280) >> 8;
            k += l & 255;
        }
        return (i / 9 & 255) << 16 | (j / 9 & 255) << 8 | k / 9 & 255;
    }

    /**
     * @author LiteOptimizer
     * @reason per-section memo during chunk builds; vanilla math otherwise
     */
    @Overwrite
    public static int getGrassColorAtPos(IBlockAccess blockAccess, BlockPos pos) {
        return liteoptimizer$colorAtPos(blockAccess, pos, ColorResolver.GRASS);
    }

    /**
     * @author LiteOptimizer
     * @reason see getGrassColorAtPos
     */
    @Overwrite
    public static int getFoliageColorAtPos(IBlockAccess blockAccess, BlockPos pos) {
        return liteoptimizer$colorAtPos(blockAccess, pos, ColorResolver.FOLIAGE);
    }

    /**
     * @author LiteOptimizer
     * @reason see getGrassColorAtPos
     */
    @Overwrite
    public static int getWaterColorAtPos(IBlockAccess blockAccess, BlockPos pos) {
        return liteoptimizer$colorAtPos(blockAccess, pos, ColorResolver.WATER);
    }
}

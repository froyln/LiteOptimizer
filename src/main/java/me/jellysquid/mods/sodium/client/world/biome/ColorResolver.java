package me.jellysquid.mods.sodium.client.world.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * LiteOptimizer: vanilla's BiomeColorHelper.ColorResolver is package-private (Forge opened it with
 * an access transformer). These are the same three resolvers vanilla defines, verbatim.
 */
public interface ColorResolver {
    int getColorAtPos(Biome biome, BlockPos pos);

    ColorResolver GRASS = new ColorResolver() {
        @Override
        public int getColorAtPos(Biome biome, BlockPos pos) {
            return biome.getGrassColorAtPos(pos);
        }
    };

    ColorResolver FOLIAGE = new ColorResolver() {
        @Override
        public int getColorAtPos(Biome biome, BlockPos pos) {
            return biome.getFoliageColorAtPos(pos);
        }
    };

    ColorResolver WATER = new ColorResolver() {
        @Override
        public int getColorAtPos(Biome biome, BlockPos pos) {
            return biome.getWaterColor();
        }
    };
}

package me.jellysquid.mods.sodium.client.world;

import me.jellysquid.mods.sodium.client.world.biome.ColorResolver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Contains extensions to the vanilla {@link IBlockAccess}.
 */
public interface SodiumBlockAccess extends IBlockAccess {
    int getBlockTint(BlockPos pos, ColorResolver resolver);
}

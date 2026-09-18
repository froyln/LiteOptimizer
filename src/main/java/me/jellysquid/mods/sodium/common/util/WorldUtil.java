package me.jellysquid.mods.sodium.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

/**
 * LiteOptimizer: the Forge fluid abstraction is gone; vanilla 1.12.2 has exactly the BlockLiquid
 * blocks, identified by their material. The fluid renderer is vanilla's algorithm, so this is all
 * that is left.
 */
public class WorldUtil {
    public static BlockLiquid toFluidBlock(Block block) {
        return block instanceof BlockLiquid ? (BlockLiquid) block : null;
    }

    /** The "fluid" of a state: its material for liquid blocks, null otherwise. */
    public static Material getFluid(IBlockState state) {
        return state.getBlock() instanceof BlockLiquid ? state.getMaterial() : null;
    }

    public static boolean shouldDisplayFluidOverlay(IBlockState block) {
        return !block.getMaterial().isOpaque() || block.getMaterial() == Material.LEAVES;
    }
}

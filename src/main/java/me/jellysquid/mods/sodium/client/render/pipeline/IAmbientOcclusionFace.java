package me.jellysquid.mods.sodium.client.render.pipeline;

import java.util.BitSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Public view of vanilla's package-private BlockModelRenderer.AmbientOcclusionFace (mixin-implemented). */
public interface IAmbientOcclusionFace {
    float[] liteoptimizer$vertexColorMultiplier();

    int[] liteoptimizer$vertexBrightness();

    void liteoptimizer$update(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face, float[] bounds, BitSet flags);
}

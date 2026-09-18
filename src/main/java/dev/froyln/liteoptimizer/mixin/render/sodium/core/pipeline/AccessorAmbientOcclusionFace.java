package dev.froyln.liteoptimizer.mixin.render.sodium.core.pipeline;

import java.util.BitSet;

import me.jellysquid.mods.sodium.client.render.pipeline.IAmbientOcclusionFace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Vanilla's AmbientOcclusionFace is package-private; this mixin lends it a public face so the
 * Sodium block renderer can drive vanilla's own AO math (see BlockRenderer).
 */
@Mixin(targets = "net.minecraft.client.renderer.BlockModelRenderer$AmbientOcclusionFace")
public abstract class AccessorAmbientOcclusionFace implements IAmbientOcclusionFace {
    // The annotation processor does not remap members of a `targets` (inner-class) mixin, so the
    // Notch names from mcp_stable_39 notch-mcp.srg are given as aliases (bvo$b: b, c, a).
    @Shadow(aliases = "b") private float[] vertexColorMultiplier;
    @Shadow(aliases = "c") private int[] vertexBrightness;

    @Shadow(aliases = "a") public abstract void updateVertexBrightness(IBlockAccess worldIn, IBlockState state, BlockPos centerPos, EnumFacing direction, float[] faceShape, BitSet shapeState);

    @Override
    public float[] liteoptimizer$vertexColorMultiplier() {
        return this.vertexColorMultiplier;
    }

    @Override
    public int[] liteoptimizer$vertexBrightness() {
        return this.vertexBrightness;
    }

    @Override
    public void liteoptimizer$update(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face, float[] bounds, BitSet flags) {
        this.updateVertexBrightness(world, state, pos, face, bounds, flags);
    }
}

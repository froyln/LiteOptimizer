package dev.froyln.liteoptimizer.mixin.render.sodium.core.pipeline;

import java.util.BitSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockModelRenderer.class)
public interface InvokerBlockModelRenderer {
    @Invoker("fillQuadBounds")
    void liteoptimizer$fillQuadBounds(IBlockState state, int[] vertexData, EnumFacing face, float[] quadBounds, BitSet boundsFlags);
}

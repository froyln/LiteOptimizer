package dev.froyln.liteoptimizer.mixin.dev.bench;

import dev.froyln.liteoptimizer.dev.VertexDump;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bench profile mode: dump vanilla's finished section meshes (see VertexDump). */
@Mixin(RenderChunk.class)
public abstract class RenderChunkDumpMixin {
    @Shadow public abstract BlockPos getPosition();

    @Inject(method = "postRenderBlocks", at = @At("RETURN"))
    private void liteoptimizer$dump(BlockRenderLayer layer, float x, float y, float z, BufferBuilder buffer, CompiledChunk compiled, CallbackInfo ci) {
        if (VertexDump.ENABLED) {
            BlockPos p = this.getPosition();
            VertexDump.dump(p.getX(), p.getY(), p.getZ(), layer.name(), buffer.getByteBuffer());
        }
    }
}

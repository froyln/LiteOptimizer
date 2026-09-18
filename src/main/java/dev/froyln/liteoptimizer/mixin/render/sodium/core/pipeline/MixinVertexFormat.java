package dev.froyln.liteoptimizer.mixin.render.sodium.core.pipeline;

import me.jellysquid.mods.sodium.client.gl.attribute.BufferVertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VertexFormat.class)
public abstract class MixinVertexFormat implements BufferVertexFormat {
    @Shadow
    public abstract int getSize();

    @Override
    public int getStride() {
        return this.getSize();
    }
}

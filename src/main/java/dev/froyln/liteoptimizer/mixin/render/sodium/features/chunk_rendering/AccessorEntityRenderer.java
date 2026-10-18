package dev.froyln.liteoptimizer.mixin.render.sodium.features.chunk_rendering;

import net.minecraft.client.renderer.EntityRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityRenderer.class)
public interface AccessorEntityRenderer {
    @Accessor("fogColorRed")
    float liteoptimizer$fogColorRed();

    @Accessor("fogColorGreen")
    float liteoptimizer$fogColorGreen();

    @Accessor("fogColorBlue")
    float liteoptimizer$fogColorBlue();
}

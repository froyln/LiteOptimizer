package dev.froyln.liteoptimizer.mixin.dev.bench;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bench/screenshot mode only: the torch flicker is a {@code Math.random()} walk per client tick,
 * so the block-light part of the lightmap differs by a level or two between two runs of the same
 * scene. Freezing it (torchFlickerX stays 0) makes the lightmap a function of world time alone.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererFlickerMixin {
    @Inject(method = "updateTorchFlicker", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$freezeFlicker(CallbackInfo ci) {
        ci.cancel();
    }
}

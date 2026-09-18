package dev.froyln.liteoptimizer.mixin.dev.bench;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bench/screenshot mode only: animated sprites (water, lava, fire, sea lantern, prismarine,
 * magma, portal…) advance per client tick since the texture load, which is wall-clock timing, so
 * two runs land on different frames. Freezing them makes the atlas identical in every run.
 */
@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteFreezeMixin {
    @Inject(method = "updateAnimation", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$freeze(CallbackInfo ci) {
        ci.cancel();
    }
}

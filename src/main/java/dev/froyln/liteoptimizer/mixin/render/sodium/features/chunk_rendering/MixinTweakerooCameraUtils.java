package dev.froyln.liteoptimizer.mixin.render.sodium.features.chunk_rendering;

import dev.froyln.liteoptimizer.render.TweakerooCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "fi.dy.masa.tweakeroo.util.CameraUtils", remap = false)
public abstract class MixinTweakerooCameraUtils {
    @Inject(method = "setFreeCameraSpectator(Z)V", at = @At("HEAD"), remap = false)
    private static void liteoptimizer$trackFreeCameraSpectator(boolean spectator, CallbackInfo ci) {
        TweakerooCompat.setFreeCameraSpectator(spectator);
    }
}

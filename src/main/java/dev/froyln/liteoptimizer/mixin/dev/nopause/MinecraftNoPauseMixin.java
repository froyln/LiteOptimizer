package dev.froyln.liteoptimizer.mixin.dev.nopause;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code -Dliteoptimizer.nopause}: never open the pause menu (the unfocused window would pause the
 * integrated server and freeze the harness/bench tick count).
 */
@Mixin(Minecraft.class)
public abstract class MinecraftNoPauseMixin {
    @Inject(method = "displayInGameMenu", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$noPause(CallbackInfo ci) {
        ci.cancel();
    }
}

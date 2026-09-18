package dev.froyln.liteoptimizer.mixin.dev.bench;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.dev.BenchRecorder;

@Mixin(Minecraft.class)
public abstract class MinecraftLoopMixin {
    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void liteoptimizer$frame(CallbackInfo ci) {
        BenchRecorder.onFrame((Minecraft) (Object) this);
    }
}

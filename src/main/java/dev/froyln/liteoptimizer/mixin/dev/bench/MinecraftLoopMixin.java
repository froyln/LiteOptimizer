package dev.froyln.liteoptimizer.mixin.dev.bench;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.dev.BenchRecorder;

@Mixin(Minecraft.class)
public abstract class MinecraftLoopMixin {
    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void liteoptimizer$frame(CallbackInfo ci) {
        BenchRecorder.onFrame((Minecraft) (Object) this);
    }

    /**
     * Screenshot runs render every frame at partial tick 0: the beacon beam, the end portal and
     * entity interpolation otherwise depend on where inside the tick the frame happens to fall.
     */
    @ModifyArg(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;updateCameraAndRender(FJ)V"), index = 0)
    private float liteoptimizer$partialTicks(float partialTicks) {
        return BenchRecorder.SCREENSHOT ? 0.0F : partialTicks;
    }

    @Inject(method = "runGameLoop", at = @At("RETURN"))
    private void liteoptimizer$frameEnd(CallbackInfo ci) {
        BenchRecorder.afterFrame((Minecraft) (Object) this);
    }
}

package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.dev.ParityRun;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerTickMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void liteoptimizer$countTick(CallbackInfo ci) {
        ParityRun.onServerTick((MinecraftServer) (Object) this);
    }
}

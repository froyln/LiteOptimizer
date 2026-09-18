package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParityRun;

/** Holds world ticking until the player is in, so every run simulates the same ticks. */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerWorldTickMixin {
    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;tick()V"))
    private void liteoptimizer$gateWorldTick(WorldServer world) {
        if (ParityRun.worldTicksAllowed()) {
            world.tick();
        }
    }

    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;updateEntities()V"))
    private void liteoptimizer$gateUpdateEntities(WorldServer world) {
        if (ParityRun.worldTicksAllowed()) {
            world.updateEntities();
        }
    }
}

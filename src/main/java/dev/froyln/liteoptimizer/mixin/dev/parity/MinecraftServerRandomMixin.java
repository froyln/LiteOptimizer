package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.Random;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds the unseeded {@code new Random()} in {@code MinecraftServer.<init>} (harness only). */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerRandomMixin {
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "java/util/Random"))
    private Random liteoptimizer$seededRandom() {
        return ParitySeeds.next();
    }
}

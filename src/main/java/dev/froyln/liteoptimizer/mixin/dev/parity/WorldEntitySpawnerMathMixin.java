package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.world.WorldEntitySpawner;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds {@code Math.random()} in WorldEntitySpawner (harness only). */
@Mixin(WorldEntitySpawner.class)
public abstract class WorldEntitySpawnerMathMixin {
    @Redirect(method = {"findChunksForSpawning"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D"))
    private double liteoptimizer$seededMathRandom() {
        return ParitySeeds.mathRandom();
    }
}

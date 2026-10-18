package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.Random;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds the unseeded {@code new Random()} in {@code World.<init>} (harness only). */
@Mixin(World.class)
public abstract class WorldRandomMixin {
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "java/util/Random"))
    private Random liteoptimizer$seededRandom() {
        return ParitySeeds.next();
    }
}

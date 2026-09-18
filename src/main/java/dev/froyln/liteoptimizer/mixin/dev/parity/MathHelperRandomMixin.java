package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.Random;

import net.minecraft.util.math.MathHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds the unseeded {@code new Random()} in {@code MathHelper.<clinit>} (harness only). */
@Mixin(MathHelper.class)
public abstract class MathHelperRandomMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "java/util/Random"))
    private static Random liteoptimizer$seededRandom() {
        return ParitySeeds.forStatic("MathHelper");
    }
}

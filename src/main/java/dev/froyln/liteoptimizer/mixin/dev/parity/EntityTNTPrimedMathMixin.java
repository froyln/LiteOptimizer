package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.entity.item.EntityTNTPrimed;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds {@code Math.random()} in EntityTNTPrimed (harness only). */
@Mixin(EntityTNTPrimed.class)
public abstract class EntityTNTPrimedMathMixin {
    @Redirect(method = {"<init>"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D"))
    private double liteoptimizer$seededMathRandom() {
        return ParitySeeds.mathRandom();
    }
}

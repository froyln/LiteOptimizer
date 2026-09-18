package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.entity.item.EntityItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds {@code Math.random()} in EntityItem (harness only). */
@Mixin(EntityItem.class)
public abstract class EntityItemMathMixin {
    @Redirect(method = {"<init>"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D"))
    private double liteoptimizer$seededMathRandom() {
        return ParitySeeds.mathRandom();
    }
}

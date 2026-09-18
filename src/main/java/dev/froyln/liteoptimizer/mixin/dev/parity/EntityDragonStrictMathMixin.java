package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.entity.boss.EntityDragon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Harness only: see EntityAILookIdleStrictMathMixin. */
@Mixin(EntityDragon.class)
public abstract class EntityDragonStrictMathMixin {
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Ljava/lang/Math;pow(DD)D"))
    private double liteoptimizer$strict_pow(double a, double b) {
        return StrictMath.pow(a, b);
    }
}

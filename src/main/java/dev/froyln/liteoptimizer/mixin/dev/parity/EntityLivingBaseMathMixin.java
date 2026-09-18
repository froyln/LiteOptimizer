package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds {@code Math.random()} in EntityLivingBase (harness only). */
@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMathMixin {
    @Redirect(method = {"<init>", "attackEntityFrom", "renderBrokenItemStack", "updateItemUse"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D"))
    private double liteoptimizer$seededMathRandom() {
        return ParitySeeds.mathRandom();
    }
}

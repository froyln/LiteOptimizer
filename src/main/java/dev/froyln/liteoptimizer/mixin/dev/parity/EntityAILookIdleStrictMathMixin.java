package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.entity.ai.EntityAILookIdle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Harness only: JDK 8 HotSpot computes Math.sin/cos/... differently in the interpreter (fdlibm)
 * and in C2-compiled code (x87 intrinsics), so results depend on JIT timing. StrictMath is
 * fdlibm always.
 */
@Mixin(EntityAILookIdle.class)
public abstract class EntityAILookIdleStrictMathMixin {
    @Redirect(method = {"startExecuting"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;sin(D)D"))
    private double liteoptimizer$strict_sin(double a) {
        return StrictMath.sin(a);
    }

    @Redirect(method = {"startExecuting"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;cos(D)D"))
    private double liteoptimizer$strict_cos(double a) {
        return StrictMath.cos(a);
    }
}

package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.inventory.SlotFurnaceOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds {@code Math.random()} in SlotFurnaceOutput (harness only). */
@Mixin(SlotFurnaceOutput.class)
public abstract class SlotFurnaceOutputMathMixin {
    @Redirect(method = {"onCrafting"}, at = @At(value = "INVOKE", target = "Ljava/lang/Math;random()D"))
    private double liteoptimizer$seededMathRandom() {
        return ParitySeeds.mathRandom();
    }
}

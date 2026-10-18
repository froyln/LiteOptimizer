package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.Random;

import net.minecraft.inventory.InventoryHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds the unseeded {@code new Random()} in {@code InventoryHelper.<clinit>} (harness only). */
@Mixin(InventoryHelper.class)
public abstract class InventoryHelperRandomMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "java/util/Random"))
    private static Random liteoptimizer$seededRandom() {
        return ParitySeeds.forStatic("InventoryHelper");
    }
}

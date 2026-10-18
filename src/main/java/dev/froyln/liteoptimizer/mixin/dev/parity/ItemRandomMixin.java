package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.Random;

import net.minecraft.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds the unseeded {@code new Random()} in {@code Item.<clinit>} (harness only). */
@Mixin(Item.class)
public abstract class ItemRandomMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "java/util/Random"))
    private static Random liteoptimizer$seededRandom() {
        return ParitySeeds.forStatic("Item");
    }
}

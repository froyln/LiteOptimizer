package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.Random;

import net.minecraft.tileentity.TileEntityDispenser;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds the unseeded {@code new Random()} in {@code TileEntityDispenser.<clinit>} (harness only). */
@Mixin(TileEntityDispenser.class)
public abstract class TileEntityDispenserRandomMixin {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "java/util/Random"))
    private static Random liteoptimizer$seededRandom() {
        return ParitySeeds.forStatic("TileEntityDispenser");
    }
}

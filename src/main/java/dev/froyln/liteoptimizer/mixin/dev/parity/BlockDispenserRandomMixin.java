package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.Random;

import net.minecraft.block.BlockDispenser;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds the unseeded {@code new Random()} in {@code BlockDispenser.<init>} (harness only). */
@Mixin(BlockDispenser.class)
public abstract class BlockDispenserRandomMixin {
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "java/util/Random"))
    private Random liteoptimizer$seededRandom() {
        return ParitySeeds.next();
    }
}

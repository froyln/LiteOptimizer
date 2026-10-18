package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.Random;

import net.minecraft.entity.item.EntityMinecartContainer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParitySeeds;

/** Seeds the unseeded {@code new Random()} in {@code EntityMinecartContainer.addLoot} (harness only). */
@Mixin(EntityMinecartContainer.class)
public abstract class EntityMinecartContainerRandomMixin {
    @Redirect(method = "addLoot", at = @At(value = "NEW", target = "java/util/Random"))
    private Random liteoptimizer$seededRandom() {
        return ParitySeeds.next();
    }
}

package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.pathfinding.PathNavigate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.dev.ParityRun;

/**
 * Harness only: PathNavigate.checkForStuck times out a path with the wall clock
 * (System.currentTimeMillis), so whether a mob abandons its path depends on real elapsed time
 * per tick. A tick-derived clock (50 ms per server tick) keeps the decision deterministic.
 */
@Mixin(PathNavigate.class)
public abstract class PathNavigateMixin {
    @Redirect(method = "checkForStuck", at = @At(value = "INVOKE", target = "Ljava/lang/System;currentTimeMillis()J"))
    private long liteoptimizer$tickClock() {
        return ParityRun.tickMillis();
    }
}

package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.server.management.PlayerChunkMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla loads player chunks with a 50 ms wall-clock budget per tick, so how many chunks exist at
 * tick N depends on disk/CPU speed. With a constant clock the budget never expires and the loop is
 * bounded only by its 49-chunks-per-tick counter — deterministic.
 */
@Mixin(PlayerChunkMap.class)
public abstract class PlayerChunkMapMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/lang/System;nanoTime()J"))
    private long liteoptimizer$constantClock() {
        return 0L;
    }
}

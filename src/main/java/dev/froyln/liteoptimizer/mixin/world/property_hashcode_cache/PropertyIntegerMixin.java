package dev.froyln.liteoptimizer.mixin.world.property_hashcode_cache;

import net.minecraft.block.properties.PropertyInteger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Catalog row world/property_hashcode_cache (B3-hoppers profile 2026-09-18; StellarCore has the
 * same fix). PropertyInteger.hashCode() re-hashes its allowed-value set (and, for enums, the
 * name→value HashMap) on every call, and IBlockState.getValue(property) hashes the property for
 * its ImmutableMap lookup — the single hottest frame of a chunk rebuild and of every
 * getValue on the server. All inputs are final fields set in the constructor, so the first
 * result is cached; the value is bit-identical, only computed once.
 */
@Mixin(PropertyInteger.class)
public abstract class PropertyIntegerMixin {
    // Single field, 0 = not yet computed (String.hashCode idiom): hashCode runs concurrently on
    // render workers and the server thread, and one plain int store is safe to race on.
    private int liteoptimizer$hash;

    @Inject(method = "hashCode", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$cachedHash(CallbackInfoReturnable<Integer> cir) {
        int hash = this.liteoptimizer$hash;
        if (hash != 0) {
            cir.setReturnValue(hash);
        }
    }

    @Inject(method = "hashCode", at = @At("RETURN"))
    private void liteoptimizer$storeHash(CallbackInfoReturnable<Integer> cir) {
        this.liteoptimizer$hash = cir.getReturnValue();
    }
}

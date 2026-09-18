package dev.froyln.liteoptimizer.mixin.dev.bench;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code -Dliteoptimizer.bench.noentities=1}: no entity is drawn (RenderManager.shouldRender is
 * false for all), so a screenshot compares terrain and block entities only — dropped items spin
 * from an unseeded client-side random and would differ between any two runs.
 */
@Mixin(RenderManager.class)
public abstract class RenderManagerNoEntitiesMixin {
    private static final boolean NO_ENTITIES = System.getProperty("liteoptimizer.bench.noentities") != null;

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$hideEntities(Entity entity, ICamera camera, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (NO_ENTITIES) {
            cir.setReturnValue(false);
        }
    }
}

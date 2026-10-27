package dev.froyln.liteoptimizer.mixin.render.tile_entity_frustum;

import dev.froyln.liteoptimizer.render.TileEntityFrustum;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    @Inject(method = "renderEntities", at = @At("HEAD"))
    private void liteoptimizer$enterFrame(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        TileEntityFrustum.enter(camera);
    }

    @Inject(method = "renderEntities", at = @At("RETURN"))
    private void liteoptimizer$exitFrame(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        TileEntityFrustum.exit();
    }
}

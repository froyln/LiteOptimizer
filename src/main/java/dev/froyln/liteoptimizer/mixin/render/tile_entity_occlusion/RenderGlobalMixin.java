package dev.froyln.liteoptimizer.mixin.render.tile_entity_occlusion;

import dev.froyln.liteoptimizer.render.TileEntityOcclusion;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catalog row render/tile_entity_occlusion: brackets renderEntities so
 * {@link TileEntityOcclusion} only culls with this frame's chunk-visibility graph, never a
 * stale one from outside the render pass. Independent of tile_entity_frustum's own bracket —
 * either optimization must work with the other switched off.
 */
@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    @Inject(method = "renderEntities", at = @At("HEAD"))
    private void liteoptimizer$enterFrame(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        TileEntityOcclusion.enter();
    }

    @Inject(method = "renderEntities", at = @At("RETURN"))
    private void liteoptimizer$exitFrame(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        TileEntityOcclusion.exit();
    }
}

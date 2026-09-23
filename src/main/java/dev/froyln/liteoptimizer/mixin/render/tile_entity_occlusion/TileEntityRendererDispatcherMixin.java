package dev.froyln.liteoptimizer.mixin.render.tile_entity_occlusion;

import dev.froyln.liteoptimizer.render.TileEntityOcclusion;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catalog row render/tile_entity_occlusion: skips the world-pass render of a TE whose draw box
 * is fully outside this frame's chunk-visibility graph. Same target as tile_entity_frustum and
 * Tweakeroo's HEAD cancel (order-independent: all three only cancel).
 */
@Mixin(TileEntityRendererDispatcher.class)
public abstract class TileEntityRendererDispatcherMixin {
    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$cullOccluded(TileEntity te, float partialTicks, int destroyStage, CallbackInfo ci) {
        if (TileEntityOcclusion.isOccluded(te)) {
            ci.cancel();
        }
    }
}

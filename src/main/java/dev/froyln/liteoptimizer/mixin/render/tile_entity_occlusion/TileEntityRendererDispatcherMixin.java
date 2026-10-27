package dev.froyln.liteoptimizer.mixin.render.tile_entity_occlusion;

import dev.froyln.liteoptimizer.render.TileEntityFrustum;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class TileEntityRendererDispatcherMixin {
    // Run after the renderer distance checks and other HEAD hooks have already executed.
    @Inject(
            method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V"
            ),
            cancellable = true
    )
    private void liteoptimizer$cullOccluded(TileEntity te, float partialTicks, int destroyStage, CallbackInfo ci) {
        if (TileEntityFrustum.isOccluded(te)) {
            ci.cancel();
        }
    }
}

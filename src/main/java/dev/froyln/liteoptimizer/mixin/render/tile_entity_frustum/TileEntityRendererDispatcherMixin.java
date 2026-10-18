package dev.froyln.liteoptimizer.mixin.render.tile_entity_frustum;

import dev.froyln.liteoptimizer.render.TileEntityFrustum;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class TileEntityRendererDispatcherMixin {
    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$cullOutsideFrustum(TileEntity te, float partialTicks, int destroyStage, CallbackInfo ci) {
        if (TileEntityFrustum.isOutside(te)) {
            ci.cancel();
        }
    }
}

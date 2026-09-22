package dev.froyln.liteoptimizer.mixin.dev.bench;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code -Dliteoptimizer.bench.skiptesr=TileEntityChest,TileEntitySign}: does not render block
 * entities of those classes (simple class name). Experiment tool to attribute the
 * {@code entities.blockentities} profiler section to renderer types, since JFR cannot sample GL.
 */
@Mixin(TileEntityRendererDispatcher.class)
public abstract class TileEntityRendererSkipMixin {
    private static final String SKIP = System.getProperty("liteoptimizer.bench.skiptesr");

    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$skip(TileEntity te, float partialTicks, int destroyStage, CallbackInfo ci) {
        if (SKIP != null && ("," + SKIP + ",").contains("," + te.getClass().getSimpleName() + ",")) {
            ci.cancel();
        }
    }
}

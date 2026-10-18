package dev.froyln.liteoptimizer.mixin.render.model_display_lists;

import dev.froyln.liteoptimizer.render.BlockModelLists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catalog row render/model_display_lists, block-model part: renderModelBrightnessColor (item
 * frame frames, minecart contents, primed TNT) draws one quad per Tessellator.draw; compiled once
 * per (state, model, brightness, colour) into a display list. See {@link BlockModelLists}.
 */
@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {
    private static final String TARGET = "renderModelBrightnessColor(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/renderer/block/model/IBakedModel;FFFF)V";

    @Inject(method = TARGET, at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$replayList(IBlockState state, IBakedModel model, float brightness, float red, float green, float blue, CallbackInfo ci) {
        if (BlockModelLists.begin(state, model, brightness, red, green, blue)) {
            ci.cancel();
        }
    }

    @Inject(method = TARGET, at = @At("RETURN"))
    private void liteoptimizer$finishList(IBlockState state, IBakedModel model, float brightness, float red, float green, float blue, CallbackInfo ci) {
        BlockModelLists.end();
    }
}

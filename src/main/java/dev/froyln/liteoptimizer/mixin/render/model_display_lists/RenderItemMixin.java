package dev.froyln.liteoptimizer.mixin.render.model_display_lists;

import dev.froyln.liteoptimizer.render.ItemModelLists;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catalog row render/model_display_lists, item part (B1 profile 2026-09-18: 338 item frames =
 * 11 ms/frame of entity rendering, nearly all of it RenderItem.renderModel re-tessellating
 * the same item models every frame plus BlockModelRenderer drawing the frame quad by quad).
 *
 * The quads renderModel feeds the BufferBuilder are a pure function of (model, colour, ItemColors
 * per tint index); {@link ItemModelLists} compiles that draw into a display list once and
 * replays it. Same vertex bytes, same draw call, GL state untouched — pixel-identical, like
 * vanilla's own ModelRenderer display lists.
 */
@Mixin(RenderItem.class)
public abstract class RenderItemMixin {
    @Shadow @Final private ItemColors itemColors;

    @Inject(method = "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$replayList(IBakedModel model, int color, ItemStack stack, CallbackInfo ci) {
        if (ItemModelLists.begin(model, color, stack, this.itemColors)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V", at = @At("RETURN"))
    private void liteoptimizer$finishList(IBakedModel model, int color, ItemStack stack, CallbackInfo ci) {
        ItemModelLists.end();
    }

    // Only item-frame items (FIXED) are served from lists. The held item and GUI icons are lit
    // through a scaled modelview where NVIDIA's display-list path rounds fixed-function lighting
    // differently by 1/255 on some pixels (measured at B1); the item-frame pixels were identical,
    // and those are where the time goes. Dropped items never come through renderItemModel
    // (RenderEntityItem calls renderItem directly), so nothing else is gated in.
    @Inject(method = "renderItemModel", at = @At("HEAD"))
    private void liteoptimizer$enterTransform(ItemStack stack, IBakedModel model, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo ci) {
        ItemModelLists.setEnabled(transform == ItemCameraTransforms.TransformType.FIXED);
    }

    @Inject(method = "renderItemModel", at = @At("RETURN"))
    private void liteoptimizer$leaveTransform(ItemStack stack, IBakedModel model, ItemCameraTransforms.TransformType transform, boolean leftHanded, CallbackInfo ci) {
        ItemModelLists.setEnabled(false);
    }

    @Inject(method = "onResourceManagerReload", at = @At("HEAD"))
    private void liteoptimizer$dropLists(IResourceManager resourceManager, CallbackInfo ci) {
        ItemModelLists.clear();
        dev.froyln.liteoptimizer.render.BlockModelLists.clear();
    }
}

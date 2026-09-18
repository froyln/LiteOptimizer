package dev.froyln.liteoptimizer.mixin.world.item_merging;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catalog row world/item_merging (audit A7).
 *
 * Every 25 ticks (or whenever it crossed a block boundary) an item entity queries all item
 * entities in its neighbourhood and calls combineItems on each. combineItems can never merge a
 * full stack: for a smaller partner it recurses with the roles swapped and then fails the
 * {@code count + count > maxStackSize} test; for an equal or larger partner it fails that test
 * directly. Every branch before that test is a pure comparison, so skipping the query for a
 * full stack has no observable effect. Stacks with max size 1 are not skipped: Tweakeroo's
 * shulker-box stacking tweak hooks combineItems for those.
 */
@Mixin(EntityItem.class)
public abstract class EntityItemMixin {
    @Shadow public abstract ItemStack getItem();

    @Inject(method = "searchForOtherItemsNearby", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$skipFullStacks(CallbackInfo ci) {
        ItemStack stack = this.getItem();
        int max = stack.getMaxStackSize();
        if (max > 1 && stack.getCount() >= max) {
            ci.cancel();
        }
    }
}

package dev.froyln.liteoptimizer.mixin.memory.itemstack_is_empty;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Catalog row memory/itemstack_is_empty (audit H1, FoamFix fasterAirItemLookup).
 *
 * ItemStack.isEmpty() compares the item against Item.getItemFromBlock(Blocks.AIR), an
 * IdentityHashMap lookup, on every call — and isEmpty is called from every inventory, hopper
 * and rendering path. The lookup's result never changes once items are registered, so it is
 * cached after Items.AIR exists (before that, during bootstrap, the real lookup is kept so the
 * answer is identical at every point in time).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    private static Item liteoptimizer$air;

    @Redirect(method = "isEmpty", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getItemFromBlock(Lnet/minecraft/block/Block;)Lnet/minecraft/item/Item;"))
    private Item liteoptimizer$cachedAirItem(Block air) {
        Item cached = liteoptimizer$air;
        if (cached == null) {
            cached = Item.getItemFromBlock(air);
            if (Items.AIR != null) {
                liteoptimizer$air = cached;
            }
        }
        return cached;
    }
}

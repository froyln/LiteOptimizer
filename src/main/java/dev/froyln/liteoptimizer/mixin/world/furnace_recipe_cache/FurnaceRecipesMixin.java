package dev.froyln.liteoptimizer.mixin.world.furnace_recipe_cache;

import java.util.IdentityHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Catalog row world/furnace_recipe_cache (audit B4; FastFurnace).
 *
 * getSmeltingResult scans every recipe with compareItemStacks; TileEntityFurnace.canSmelt calls
 * it every tick while a furnace burns. The scan only depends on (item, metadata) of the input,
 * so its result — including "no recipe" — is memoized per (item, metadata) and the memo is
 * dropped on addSmeltingRecipe. Once getSmeltingList exposes the mutable map, caching stops:
 * callers can retain and edit that map without calling FurnaceRecipes again.
 *
 * Parity: a memo of exactly what the scan returned for that key in this run; the scan itself is
 * unchanged and still runs on every miss.
 */
@Mixin(FurnaceRecipes.class)
public abstract class FurnaceRecipesMixin {
    private static final ItemStack NO_RECIPE = ItemStack.EMPTY;
    private final Map<Item, Int2ObjectOpenHashMap<ItemStack>> liteoptimizer$memo = new IdentityHashMap<Item, Int2ObjectOpenHashMap<ItemStack>>();
    private boolean liteoptimizer$exposed;

    @Inject(method = "getSmeltingResult", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$memoHit(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (this.liteoptimizer$exposed) {
            return;
        }
        Int2ObjectOpenHashMap<ItemStack> byMeta = this.liteoptimizer$memo.get(stack.getItem());
        if (byMeta != null) {
            ItemStack hit = byMeta.get(stack.getMetadata());
            if (hit != null) {
                cir.setReturnValue(hit);
            }
        }
    }

    @Inject(method = "getSmeltingResult", at = @At("RETURN"))
    private void liteoptimizer$memoStore(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (this.liteoptimizer$exposed) {
            return;
        }
        Int2ObjectOpenHashMap<ItemStack> byMeta = this.liteoptimizer$memo.get(stack.getItem());
        if (byMeta == null) {
            this.liteoptimizer$memo.put(stack.getItem(), byMeta = new Int2ObjectOpenHashMap<ItemStack>());
        }
        ItemStack result = cir.getReturnValue();
        byMeta.putIfAbsent(stack.getMetadata(), result.isEmpty() ? NO_RECIPE : result);
    }

    @Inject(method = "addSmeltingRecipe", at = @At("HEAD"))
    private void liteoptimizer$invalidateOnAdd(CallbackInfo ci) {
        this.liteoptimizer$memo.clear();
    }

    @Inject(method = "getSmeltingList", at = @At("HEAD"))
    private void liteoptimizer$invalidateOnExpose(CallbackInfoReturnable<Map<ItemStack, ItemStack>> cir) {
        this.liteoptimizer$exposed = true;
        this.liteoptimizer$memo.clear();
    }
}

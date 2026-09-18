package dev.froyln.liteoptimizer.mixin.memory.baked_quad_dedup;

import dev.froyln.liteoptimizer.memory.BakedQuadDedup;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Catalog row memory/baked_quad_dedup (FoamFix deduplicate, FerriteCore §8).
 *
 * ModelBakery.bakeModel bakes a fresh SimpleBakedModel for every blockstate variant and item
 * model with no cache, so the same (model, rotation) pair — stone#normal and stone#inventory,
 * every facing of a full cube, every stair/slab sharing a texture — yields identical BakedQuads
 * over and over. Return the first identical quad instead. Nothing in vanilla mutates a quad or
 * compares quads by identity, so the models render the same.
 */
@Mixin(FaceBakery.class)
public abstract class FaceBakeryMixin {
    @Inject(method = "makeBakedQuad", at = @At("RETURN"), cancellable = true)
    private void liteoptimizer$internQuad(CallbackInfoReturnable<BakedQuad> cir) {
        cir.setReturnValue(BakedQuadDedup.intern(cir.getReturnValue()));
    }
}

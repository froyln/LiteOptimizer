package dev.froyln.liteoptimizer.mixin.memory.baked_quad_dedup;

import dev.froyln.liteoptimizer.memory.BakedQuadDedup;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Drops the dedup map once the bake is over (see {@link FaceBakeryMixin}); also at HEAD, in case the previous bake threw. */
@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {
    @Inject(method = "setupModelRegistry", at = {@At("HEAD"), @At("RETURN")})
    private void liteoptimizer$finishDedup(CallbackInfoReturnable<IRegistry<ModelResourceLocation, IBakedModel>> cir) {
        BakedQuadDedup.finish();
    }
}

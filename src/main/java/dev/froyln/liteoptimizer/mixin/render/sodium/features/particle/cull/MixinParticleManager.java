package dev.froyln.liteoptimizer.mixin.render.sodium.features.particle.cull;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    private Frustum cullingFrustum;

    @Inject(method = {"renderParticles", "renderLitParticles"}, at = @At("HEAD"))
    private void preRenderParticles(Entity entity, float partialTicks, CallbackInfo ci) {
        Frustum frustum = SodiumWorldRenderer.getInstance().getFrustum();
        boolean useCulling = SodiumClientMod.options().advanced.useParticleCulling;

        // Setup the frustum state before rendering particles
        if (useCulling && frustum != null) {
            this.cullingFrustum = frustum;
        } else {
            this.cullingFrustum = null;
        }
    }

    /** Mixin 0.7: a plain redirect instead of MixinExtras' WrapWithCondition. */
    @org.spongepowered.asm.mixin.injection.Redirect(method = {"renderParticles", "renderLitParticles"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/Particle;renderParticle(Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void filterParticleList(Particle particle, BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        if (this.cullingFrustum != null) {
            AxisAlignedBB box = particle.getBoundingBox();
            // Hack: Grow the particle's bounding box in order to work around mis-behaved particles
            if (!this.cullingFrustum.isBoxInFrustum(box.minX - 1.0D, box.minY - 1.0D, box.minZ - 1.0D, box.maxX + 1.0D, box.maxY + 1.0D, box.maxZ + 1.0D)) {
                return;
            }
        }
        particle.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }
}
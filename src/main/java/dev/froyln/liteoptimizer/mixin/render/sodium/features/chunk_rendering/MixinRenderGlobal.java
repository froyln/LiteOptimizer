package dev.froyln.liteoptimizer.mixin.render.sodium.features.chunk_rendering;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {
    @Shadow @Final private RenderManager renderManager;

    @Shadow @Final private Minecraft mc;

    @Shadow private int countEntitiesRendered;

    @Shadow private WorldClient world;

    @Shadow protected abstract boolean isOutlineActive(Entity entityIn, Entity viewer, ICamera camera);

    /**
     * @author embeddedt
     * @reason reimplement entity render loop because vanilla's relies on the renderInfos list
     */
    // shortcut: unused mapping callback logs a fail-soft warning; replace if Mixin adds mapping-aware local capture.
    // Development and obfuscated targets keep different locals alive at this point.
    @Inject(method = "renderEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderInfos:Ljava/util/List;", ordinal = 0),
            locals = org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILSOFT)
    private void renderEntitiesProductionLocals(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci,
                                double renderViewX, double renderViewY, double renderViewZ, Entity viewEntity,
                                List<Entity> outlineEntityList, List<Entity> multipassEntityList, BlockPos.PooledMutableBlockPos pooledPos) {
        // The production LVT drops the loaded-entity list here, so read the same world-owned list.
        List<Entity> loadedEntityList = this.world.getLoadedEntityList();
        this.renderEntitiesSodium(renderViewEntity, camera, partialTicks, renderViewX, renderViewY, renderViewZ,
                loadedEntityList, outlineEntityList, multipassEntityList);
    }

    @Inject(method = "renderEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderInfos:Ljava/util/List;", ordinal = 0),
            locals = org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILSOFT)
    private void renderEntitiesDevelopmentLocals(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci,
                                double renderViewX, double renderViewY, double renderViewZ, Entity viewEntity,
                                double entityX, double entityY, double entityZ, List<Entity> loadedEntityList,
                                List<Entity> outlineEntityList, List<Entity> multipassEntityList, BlockPos.PooledMutableBlockPos pooledPos) {
        this.renderEntitiesSodium(renderViewEntity, camera, partialTicks, renderViewX, renderViewY, renderViewZ,
                loadedEntityList, outlineEntityList, multipassEntityList);
    }

    private void renderEntitiesSodium(Entity renderViewEntity, ICamera camera, float partialTicks,
                                     double renderViewX, double renderViewY, double renderViewZ,
                                     List<Entity> loadedEntityList, List<Entity> outlineEntityList,
                                     List<Entity> multipassEntityList) {
        EntityPlayerSP player = this.mc.player;
        BlockPos.MutableBlockPos entityBlockPos = new BlockPos.MutableBlockPos();
        // Apply entity distance scaling
        Entity.setRenderDistanceWeight(MathHelper.clamp((double)this.mc.gameSettings.renderDistanceChunks / 8.0D, 1.0D, 2.5D) * SodiumClientMod.options().quality.entityDistanceScaling);
        for(Entity entity : loadedEntityList) {

            // Do regular vanilla checks for visibility
            if(!this.renderManager.shouldRender(entity, camera, renderViewX, renderViewY, renderViewZ) && !entity.isRidingOrBeingRiddenBy(player)) {
                continue;
            }

            boolean isSleeping = renderViewEntity instanceof EntityLivingBase && ((EntityLivingBase) renderViewEntity).isPlayerSleeping();

            if ((entity != renderViewEntity || this.mc.gameSettings.thirdPersonView != 0 || isSleeping)
                    && (entity.posY < 0.0D || entity.posY >= 256.0D || this.world.isBlockLoaded(entityBlockPos.setPos(entity))))
            {
                ++this.countEntitiesRendered;
                this.renderManager.renderEntityStatic(entity, partialTicks, false);

                if (this.isOutlineActive(entity, renderViewEntity, camera))
                {
                    outlineEntityList.add(entity);
                }

                if (this.renderManager.isRenderMultipass(entity)) {
                    multipassEntityList.add(entity);
                }
            }
        }
    }
}

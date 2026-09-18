package dev.froyln.liteoptimizer.mixin.world.hard_hitbox_entity_list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.world.IHardHitboxChunk;

/**
 * Catalog row world/hard_hitbox_entity_list (audit A2 entity part; TISCM
 * optimizedHardHitBoxEntityCollision, Lithium chunk.entity_class_groups).
 *
 * World.getCollisionBoxes(Entity, AABB) lists every entity around the mover and keeps only
 * those whose getCollisionBoundingBox() (boats, live shulkers) or the mover's
 * getCollisionBox(entity) (only minecarts and boats override it) is non-null. With thousands
 * of item entities in a farm that scan is quadratic. For any mover that is not a minecart or
 * boat only the boats/shulkers can contribute, and those are read from the per-section lists
 * ChunkMixin maintains.
 *
 * Parity: same chunk range, same section range, same intersects/exclusion/spectator tests as
 * getEntitiesInAABBexcluding, same relative order (the side lists are appended and removed in
 * step with the full lists); every entity skipped would have yielded null boxes. Minecart and
 * boat movers take the vanilla path untouched.
 */
@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entityIn, AxisAlignedBB bb);

    @Redirect(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;"))
    private List<Entity> liteoptimizer$hardHitboxCandidates(World self, Entity entityIn, AxisAlignedBB box) {
        if (entityIn instanceof EntityMinecart || entityIn instanceof EntityBoat) {
            return this.getEntitiesWithinAABBExcludingEntity(entityIn, box);
        }
        List<Entity> list = null;
        int j2 = MathHelper.floor((box.minX - 2.0D) / 16.0D);
        int k2 = MathHelper.floor((box.maxX + 2.0D) / 16.0D);
        int l2 = MathHelper.floor((box.minZ - 2.0D) / 16.0D);
        int i3 = MathHelper.floor((box.maxZ + 2.0D) / 16.0D);
        int i = MathHelper.clamp(MathHelper.floor((box.minY - 2.0D) / 16.0D), 0, 15);
        int j = MathHelper.clamp(MathHelper.floor((box.maxY + 2.0D) / 16.0D), 0, 15);
        for (int j3 = j2; j3 <= k2; ++j3) {
            for (int k3 = l2; k3 <= i3; ++k3) {
                if (!this.isChunkLoaded(j3, k3, true)) {
                    continue;
                }
                IHardHitboxChunk chunk = (IHardHitboxChunk) this.getChunk(j3, k3);
                for (int k = i; k <= j; ++k) {
                    List<Entity> hard = chunk.liteoptimizer$hardHitboxEntities(k);
                    if (hard == null) {
                        continue;
                    }
                    for (int n = 0, size = hard.size(); n < size; n++) {
                        Entity entity = hard.get(n);
                        if (entity.getEntityBoundingBox().intersects(box) && entity != entityIn && EntitySelectors.NOT_SPECTATING.apply(entity)) {
                            if (list == null) {
                                list = new ArrayList<Entity>();
                            }
                            list.add(entity);
                        }
                    }
                }
            }
        }
        return list == null ? Collections.<Entity>emptyList() : list;
    }
}

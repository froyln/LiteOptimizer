package dev.froyln.liteoptimizer.mixin.world.hard_hitbox_entity_list;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.world.IHardHitboxChunk;

/**
 * Mirrors Chunk.addEntity / removeEntityAtIndex for the two vanilla entity types that override
 * getCollisionBoundingBox (EntityBoat, EntityShulker), so a collision query can skip everything
 * else. Same section index rules as vanilla (chunkCoordY is what vanilla just computed).
 */
@Mixin(Chunk.class)
public abstract class ChunkMixin implements IHardHitboxChunk {
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;

    @SuppressWarnings("unchecked")
    private final List<Entity>[] liteoptimizer$hard = new List[16];

    private static boolean liteoptimizer$isHard(Entity entity) {
        return entity instanceof EntityBoat || entity instanceof EntityShulker;
    }

    @Override
    public List<Entity> liteoptimizer$hardHitboxEntities(int section) {
        return this.liteoptimizer$hard[section];
    }

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void liteoptimizer$onAdd(Entity entity, CallbackInfo ci) {
        if (liteoptimizer$isHard(entity)) {
            int k = entity.chunkCoordY;
            List<Entity> list = this.liteoptimizer$hard[k];
            if (list == null) {
                this.liteoptimizer$hard[k] = list = new ArrayList<Entity>(4);
            }
            list.add(entity);
        }
    }

    @Inject(method = "removeEntityAtIndex", at = @At("RETURN"))
    private void liteoptimizer$onRemove(Entity entity, int index, CallbackInfo ci) {
        if (liteoptimizer$isHard(entity)) {
            if (index < 0) {
                index = 0;
            }
            if (index >= this.entityLists.length) {
                index = this.entityLists.length - 1;
            }
            List<Entity> list = this.liteoptimizer$hard[index];
            if (list != null) {
                list.remove(entity);
                if (list.isEmpty()) {
                    this.liteoptimizer$hard[index] = null;
                }
            }
        }
    }
}

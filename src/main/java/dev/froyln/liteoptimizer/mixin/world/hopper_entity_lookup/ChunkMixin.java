package dev.froyln.liteoptimizer.mixin.world.hopper_entity_lookup;

import java.util.ArrayList;
import java.util.List;

import dev.froyln.liteoptimizer.world.IInventoryEntityChunk;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mirrors Chunk.addEntity / removeEntityAtIndex for entities that are IInventory (chest and
 * hopper minecarts), so a hopper's inventory search can skip every item, mob and player. Same
 * section index rules as vanilla; the side list is appended and removed in step with the
 * section's full list, so relative order is identical.
 */
@Mixin(Chunk.class)
public abstract class ChunkMixin implements IInventoryEntityChunk {
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;

    @SuppressWarnings("unchecked")
    private final List<Entity>[] liteoptimizer$inventories = new List[16];

    @Override
    public List<Entity> liteoptimizer$inventoryEntities(int section) {
        return this.liteoptimizer$inventories[section];
    }

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void liteoptimizer$onAdd(Entity entity, CallbackInfo ci) {
        if (entity instanceof IInventory) {
            int k = entity.chunkCoordY;
            List<Entity> list = this.liteoptimizer$inventories[k];
            if (list == null) {
                this.liteoptimizer$inventories[k] = list = new ArrayList<Entity>(4);
            }
            list.add(entity);
        }
    }

    @Inject(method = "removeEntityAtIndex", at = @At("RETURN"))
    private void liteoptimizer$onRemove(Entity entity, int index, CallbackInfo ci) {
        if (entity instanceof IInventory) {
            if (index < 0) {
                index = 0;
            }
            if (index >= this.entityLists.length) {
                index = this.entityLists.length - 1;
            }
            List<Entity> list = this.liteoptimizer$inventories[index];
            if (list != null) {
                list.remove(entity);
                if (list.isEmpty()) {
                    this.liteoptimizer$inventories[index] = null;
                }
            }
        }
    }
}

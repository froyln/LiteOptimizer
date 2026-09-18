package dev.froyln.liteoptimizer.world;

import java.util.List;

import net.minecraft.entity.Entity;

/**
 * Implemented on Chunk by the hopper_entity_lookup mixin: per section, the entities that are
 * IInventory (chest/hopper minecarts), in the same relative order as the section's full list.
 */
public interface IInventoryEntityChunk {
    /** null when the section has none. */
    List<Entity> liteoptimizer$inventoryEntities(int section);
}

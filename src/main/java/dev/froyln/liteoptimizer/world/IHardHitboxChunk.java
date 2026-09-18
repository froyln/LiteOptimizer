package dev.froyln.liteoptimizer.world;

import java.util.List;

import net.minecraft.entity.Entity;

/**
 * Implemented on Chunk by the hard_hitbox_entity_list mixin: per section, the entities whose
 * getCollisionBoundingBox() can be non-null (boats, shulkers), in the same relative order as the
 * section's full entity list.
 */
public interface IHardHitboxChunk {
    /** null when the section has none. */
    List<Entity> liteoptimizer$hardHitboxEntities(int section);
}

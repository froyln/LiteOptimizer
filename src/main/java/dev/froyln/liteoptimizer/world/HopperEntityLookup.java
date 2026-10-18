package dev.froyln.liteoptimizer.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.froyln.liteoptimizer.mixin.world.hopper_entity_lookup.WorldInvoker;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** Helper for world/hopper_entity_lookup (the accessor cast has to live outside the mixin package). */
public final class HopperEntityLookup {
    private HopperEntityLookup() {}

    /**
     * World.getEntitiesInAABBexcluding(null, box, EntitySelectors.HAS_INVENTORY) over the per-section
     * inventory-entity lists: same chunk range and order, same section clamp, same intersects and
     * isEntityAlive tests, so the list is element-for-element what vanilla builds.
     */
    public static List<Entity> inventoryEntities(World world, AxisAlignedBB box) {
        List<Entity> list = null;
        int j2 = MathHelper.floor((box.minX - 2.0D) / 16.0D);
        int k2 = MathHelper.floor((box.maxX + 2.0D) / 16.0D);
        int l2 = MathHelper.floor((box.minZ - 2.0D) / 16.0D);
        int i3 = MathHelper.floor((box.maxZ + 2.0D) / 16.0D);
        int i = MathHelper.clamp(MathHelper.floor((box.minY - 2.0D) / 16.0D), 0, 15);
        int j = MathHelper.clamp(MathHelper.floor((box.maxY + 2.0D) / 16.0D), 0, 15);
        for (int j3 = j2; j3 <= k2; ++j3) {
            for (int k3 = l2; k3 <= i3; ++k3) {
                if (!((WorldInvoker) world).liteoptimizer$isChunkLoaded(j3, k3, true)) {
                    continue;
                }
                IInventoryEntityChunk chunk = (IInventoryEntityChunk) world.getChunk(j3, k3);
                for (int k = i; k <= j; ++k) {
                    List<Entity> side = chunk.liteoptimizer$inventoryEntities(k);
                    if (side == null) {
                        continue;
                    }
                    for (int n = 0, size = side.size(); n < size; n++) {
                        Entity entity = side.get(n);
                        if (entity.getEntityBoundingBox().intersects(box) && entity.isEntityAlive()) {
                            if (list == null) {
                                list = new ArrayList<Entity>(4);
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

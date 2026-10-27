package dev.froyln.liteoptimizer.world;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

/**
 * Per-world counters behind World.countEntities(Class): one int per class ever asked for,
 * maintained on entity add/remove and on every persistence flag change. The vanilla
 * predicate is reproduced exactly: an entity counts for a class iff the class is assignable
 * from its class and it is not an EntityLiving with isNoDespawnRequired().
 */
public final class EntityTypeCounts {
    private final Map<Class<?>, int[]> counts = new IdentityHashMap<Class<?>, int[]>();

    /** Same predicate as the loop in World.countEntities. */
    public static boolean counts(Class<?> type, Entity entity) {
        return (!(entity instanceof EntityLiving) || !((EntityLiving) entity).isNoDespawnRequired()) && type.isAssignableFrom(entity.getClass());
    }

    public int count(Class<?> type, List<Entity> loadedEntities) {
        int[] c = this.counts.get(type);
        if (c == null) {
            c = new int[1];
            for (int i = 0, n = loadedEntities.size(); i < n; i++) {
                if (counts(type, loadedEntities.get(i))) {
                    c[0]++;
                }
            }
            this.counts.put(type, c);
        }
        return c[0];
    }

    public void added(Entity entity) {
        if (this.counts.isEmpty()) {
            return;
        }
        for (Map.Entry<Class<?>, int[]> e : this.counts.entrySet()) {
            if (counts(e.getKey(), entity)) {
                e.getValue()[0]++;
            }
        }
    }

    public void removed(Entity entity) {
        if (this.counts.isEmpty()) {
            return;
        }
        for (Map.Entry<Class<?>, int[]> e : this.counts.entrySet()) {
            if (counts(e.getKey(), entity)) {
                e.getValue()[0]--;
            }
        }
    }

}

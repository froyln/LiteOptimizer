package dev.froyln.liteoptimizer.mixin.memory.entity_data_manager;

import java.util.Arrays;
import java.util.Map;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catalog row memory/entity_data_manager (audit A3; FoamFix fasterEntityDataManager, Lithium
 * entity.data_tracker).
 *
 * Every entity flag read ({@code isSneaking}, {@code isInvisible}, item-entity {@code getItem}, …)
 * goes through {@code getEntry}: a ReentrantReadWriteLock acquire/release around a
 * {@code HashMap<Integer, …>} lookup with a boxed key. The entries are also mirrored into an array
 * indexed by the parameter id (ids are dense, assigned per class at class-init), and the read
 * path uses the array without the lock.
 *
 * Parity: the same DataEntry object is returned (or null for an unregistered key, as vanilla).
 * The map, the lock and every other method are untouched, so dirty tracking, packet contents and
 * their order are unchanged. Thread-safety: vanilla only mutates a manager from the thread that
 * owns the entity; the lock only ever serialised a reference read against that same thread's
 * writes, and reading an array slot is atomic.
 */
@Mixin(EntityDataManager.class)
public abstract class EntityDataManagerMixin {
    @Shadow @Final private Map<Integer, EntityDataManager.DataEntry<?>> entries;

    private EntityDataManager.DataEntry<?>[] liteoptimizer$byId = new EntityDataManager.DataEntry<?>[0];

    @Inject(method = "setEntry", at = @At("RETURN"))
    private <T> void liteoptimizer$mirror(DataParameter<T> key, T value, CallbackInfo ci) {
        int id = key.getId();
        if (id >= this.liteoptimizer$byId.length) {
            this.liteoptimizer$byId = Arrays.copyOf(this.liteoptimizer$byId, Math.max(id + 1, this.liteoptimizer$byId.length * 2));
        }
        this.liteoptimizer$byId[id] = this.entries.get(Integer.valueOf(id));
    }

    /**
     * @author froyln (LiteOptimizer)
     * @reason lock-free, allocation-free read of a per-id slot; see class javadoc.
     */
    @Overwrite
    private <T> EntityDataManager.DataEntry<T> getEntry(DataParameter<T> key) {
        int id = key.getId();
        EntityDataManager.DataEntry<?>[] byId = this.liteoptimizer$byId;
        @SuppressWarnings("unchecked")
        EntityDataManager.DataEntry<T> entry = id < byId.length ? (EntityDataManager.DataEntry<T>) byId[id] : null;
        return entry;
    }
}

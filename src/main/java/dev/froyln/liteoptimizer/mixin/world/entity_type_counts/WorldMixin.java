package dev.froyln.liteoptimizer.mixin.world.entity_type_counts;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.froyln.liteoptimizer.world.EntityTypeCounts;
import dev.froyln.liteoptimizer.world.ICountedEntity;
import dev.froyln.liteoptimizer.world.ICountingWorld;

/**
 * Catalog row world/entity_type_counts (audit H4; Lithium collections.entity_by_type).
 *
 * WorldEntitySpawner calls World.countEntities(class) once per creature type per tick — a full
 * scan of loadedEntityList with isAssignableFrom. The counts are maintained incrementally on
 * onEntityAdded / onEntityRemoved (the exact points where vanilla adds to / removes from
 * loadedEntityList) and whenever an EntityLiving's persistence flag changes.
 *
 * Parity: the counter equals what the scan would return at every point in time; the first call
 * for a class is the scan itself. Only the count is used by the caller; no RNG, no order.
 */
@Mixin(World.class)
public abstract class WorldMixin implements ICountingWorld {
    @Shadow @Final public List<Entity> loadedEntityList;

    private final EntityTypeCounts liteoptimizer$counts = new EntityTypeCounts();

    @Override
    public EntityTypeCounts liteoptimizer$counts() {
        return this.liteoptimizer$counts;
    }

    @Inject(method = "countEntities(Ljava/lang/Class;)I", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$countFromCounters(Class<?> type, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.liteoptimizer$counts.count(type, this.loadedEntityList));
    }

    @Inject(method = "onEntityAdded", at = @At("HEAD"))
    private void liteoptimizer$onAdded(Entity entity, CallbackInfo ci) {
        ((ICountedEntity) entity).liteoptimizer$setCounted(true);
        this.liteoptimizer$counts.added(entity);
    }

    @Inject(method = "onEntityRemoved", at = @At("HEAD"))
    private void liteoptimizer$onRemoved(Entity entity, CallbackInfo ci) {
        ((ICountedEntity) entity).liteoptimizer$setCounted(false);
        this.liteoptimizer$counts.removed(entity);
    }
}

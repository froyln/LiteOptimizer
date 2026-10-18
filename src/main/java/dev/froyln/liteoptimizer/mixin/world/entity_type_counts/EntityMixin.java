package dev.froyln.liteoptimizer.mixin.world.entity_type_counts;

import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;

import dev.froyln.liteoptimizer.world.ICountedEntity;

@Mixin(Entity.class)
public abstract class EntityMixin implements ICountedEntity {
    private boolean liteoptimizer$counted;

    @Override
    public boolean liteoptimizer$isCounted() {
        return this.liteoptimizer$counted;
    }

    @Override
    public void liteoptimizer$setCounted(boolean counted) {
        this.liteoptimizer$counted = counted;
    }
}

package dev.froyln.liteoptimizer.mixin.world.entity_type_counts;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.froyln.liteoptimizer.world.ICountedEntity;
import dev.froyln.liteoptimizer.world.ICountingWorld;
import dev.froyln.liteoptimizer.world.EntityTypeCounts;

/** Mirror every vanilla persistence write, including /entitydata on an already loaded mob. */
@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin extends EntityLivingBase {
    @Shadow private boolean persistenceRequired;

    private EntityLivingMixin(World world) {
        super(world);
    }

    @Redirect(method = {"enablePersistence", "updateEquipmentIfNeeded", "readEntityFromNBT"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLiving;persistenceRequired:Z", opcode = org.spongepowered.asm.lib.Opcodes.PUTFIELD))
    private void liteoptimizer$setPersistence(EntityLiving entity, boolean persistent) {
        EntityTypeCounts counts = ((ICountedEntity) this).liteoptimizer$isCounted()
                ? ((ICountingWorld) this.world).liteoptimizer$counts() : null;
        if (counts != null) {
            counts.removed(entity);
        }
        this.persistenceRequired = persistent;
        if (counts != null) {
            counts.added(entity);
        }
    }
}

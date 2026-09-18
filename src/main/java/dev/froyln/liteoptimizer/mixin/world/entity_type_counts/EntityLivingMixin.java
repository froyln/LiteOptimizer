package dev.froyln.liteoptimizer.mixin.world.entity_type_counts;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.world.ICountedEntity;
import dev.froyln.liteoptimizer.world.ICountingWorld;

/** The two places vanilla flips persistenceRequired to true on a live entity. */
@Mixin(EntityLiving.class)
public abstract class EntityLivingMixin extends EntityLivingBase {
    @Shadow private boolean persistenceRequired;

    private EntityLivingMixin(World world) {
        super(world);
    }

    @Inject(method = "enablePersistence", at = @At("HEAD"))
    private void liteoptimizer$beforeEnablePersistence(CallbackInfo ci) {
        liteoptimizer$flip();
    }

    @Inject(method = "updateEquipmentIfNeeded", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLiving;persistenceRequired:Z", opcode = org.spongepowered.asm.lib.Opcodes.PUTFIELD))
    private void liteoptimizer$beforePickupPersistence(EntityItem item, CallbackInfo ci) {
        liteoptimizer$flip();
    }

    /** Called just before the flag is set: if it was false and the entity is counted, uncount it. */
    private void liteoptimizer$flip() {
        if (!this.persistenceRequired && ((ICountedEntity) this).liteoptimizer$isCounted()) {
            ((ICountingWorld) this.world).liteoptimizer$counts().becamePersistent((EntityLiving) (Object) this);
        }
    }
}

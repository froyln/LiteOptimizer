package dev.froyln.liteoptimizer.mixin.world.hopper_entity_lookup;

import java.util.List;

import com.google.common.base.Predicate;
import dev.froyln.liteoptimizer.world.HopperEntityLookup;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Catalog row world/entity_lookup, hopper part (Lithium chunk.entity_class_groups, FoamFix
 * fasterEntityLookup).
 *
 * TileEntityHopper.getInventoryAtPosition, when there is no inventory block, lists every entity
 * in the sections around the hopper and keeps the IInventory ones — every tick the hopper is
 * not on cooldown, for every hopper with air above or in front of it. A storage hall full of
 * item entities makes that quadratic. The IInventory entities come from ChunkMixin's per-section
 * side lists instead; same chunk/section range, same tests, same order, same random pick.
 */
@Mixin(TileEntityHopper.class)
public abstract class TileEntityHopperMixin {
    @Redirect(method = "getInventoryAtPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    private static List<Entity> liteoptimizer$inventoryEntities(World world, Entity excluded, AxisAlignedBB box, Predicate<? super Entity> predicate) {
        return HopperEntityLookup.inventoryEntities(world, box);
    }
}

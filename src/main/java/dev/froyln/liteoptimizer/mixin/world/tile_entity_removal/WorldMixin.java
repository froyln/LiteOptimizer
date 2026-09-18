package dev.froyln.liteoptimizer.mixin.world.tile_entity_removal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.world.IdentityRemoval;

/**
 * Catalog row world/tile_entity_removal (audit B1, Carpet optimizedTileEntityRemoval).
 *
 * World.updateEntities removes entities and tile entities from its ArrayLists with
 * removeAll(list) — O(n·m) equals scans — and removes each tile entity that turned invalid during
 * the tick with iterator.remove() + loadedTileEntityList.remove(te), two O(n) shifts per removal.
 * Here every removeAll goes through one identity-set pass, and the per-tile-entity removals are
 * collected during the loop and applied once after it.
 *
 * Parity: the same elements are removed and the survivors keep their order. Nothing reads
 * loadedTileEntityList or tickableTileEntities during the tick loop except this method (the
 * game's own lookups go through the chunk's tile entity map), so deferring the removal to the
 * end of the loop is unobservable; the loop never revisits an element it already passed.
 */
@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow @Final public List<TileEntity> loadedTileEntityList;
    @Shadow @Final public List<TileEntity> tickableTileEntities;

    private final List<TileEntity> liteoptimizer$invalidated = new ArrayList<TileEntity>();

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;removeAll(Ljava/util/Collection;)Z"))
    private boolean liteoptimizer$removeAllByIdentity(List<Object> list, Collection<?> toRemove) {
        return IdentityRemoval.removeAll(list, toRemove);
    }

    /** iterator.remove() on tickableTileEntities → deferred. */
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
    private void liteoptimizer$deferIteratorRemove(Iterator<TileEntity> iterator) {
    }

    /** loadedTileEntityList.remove(tileentity) → deferred. */
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
    private boolean liteoptimizer$deferListRemove(List<TileEntity> list, Object tileEntity) {
        this.liteoptimizer$invalidated.add((TileEntity) tileEntity);
        return true;
    }

    /** Right after the tick loop: {@code this.processingLoadedTiles = false} (second store). */
    @Inject(method = "updateEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;processingLoadedTiles:Z", opcode = org.spongepowered.asm.lib.Opcodes.PUTFIELD, ordinal = 1, shift = At.Shift.AFTER))
    private void liteoptimizer$applyDeferredRemovals(CallbackInfo ci) {
        if (!this.liteoptimizer$invalidated.isEmpty()) {
            IdentityRemoval.removeAll(this.tickableTileEntities, this.liteoptimizer$invalidated);
            IdentityRemoval.removeAll(this.loadedTileEntityList, this.liteoptimizer$invalidated);
            this.liteoptimizer$invalidated.clear();
        }
    }
}

package dev.froyln.liteoptimizer.mixin.world.entity_fluid_checks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.world.IFluidCountedStorage;

/**
 * Counts water and lava blocks per section, maintained at exactly the two points vanilla
 * maintains its own blockRefCount/tickRefCount: every single-block write goes through set(),
 * every bulk write (chunk load from NBT, chunk packet) is followed by recalculateRefCounts().
 */
@Mixin(ExtendedBlockStorage.class)
public abstract class ExtendedBlockStorageMixin implements IFluidCountedStorage {
    @Shadow public abstract IBlockState get(int x, int y, int z);

    private int liteoptimizer$water;
    private int liteoptimizer$lava;

    @Override
    public int liteoptimizer$waterCount() {
        return this.liteoptimizer$water;
    }

    @Override
    public int liteoptimizer$lavaCount() {
        return this.liteoptimizer$lava;
    }

    @Inject(method = "set", at = @At("HEAD"))
    private void liteoptimizer$countSet(int x, int y, int z, IBlockState state, CallbackInfo ci) {
        Material old = this.get(x, y, z).getMaterial();
        Material now = state.getMaterial();
        if (old != now) {
            if (old == Material.WATER) {
                this.liteoptimizer$water--;
            } else if (old == Material.LAVA) {
                this.liteoptimizer$lava--;
            }
            if (now == Material.WATER) {
                this.liteoptimizer$water++;
            } else if (now == Material.LAVA) {
                this.liteoptimizer$lava++;
            }
        }
    }

    @Inject(method = "recalculateRefCounts", at = @At("RETURN"))
    private void liteoptimizer$recount(CallbackInfo ci) {
        int water = 0;
        int lava = 0;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    Material m = this.get(x, y, z).getMaterial();
                    if (m == Material.WATER) {
                        water++;
                    } else if (m == Material.LAVA) {
                        lava++;
                    }
                }
            }
        }
        this.liteoptimizer$water = water;
        this.liteoptimizer$lava = lava;
    }
}

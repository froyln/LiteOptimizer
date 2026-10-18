package dev.froyln.liteoptimizer.mixin.world.hopper_entity_lookup;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldInvoker {
    @Invoker("isChunkLoaded")
    boolean liteoptimizer$isChunkLoaded(int x, int z, boolean allowEmpty);
}

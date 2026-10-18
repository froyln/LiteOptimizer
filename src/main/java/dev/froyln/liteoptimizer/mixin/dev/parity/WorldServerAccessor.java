package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.TreeSet;

import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldServer.class)
public interface WorldServerAccessor {
    @Accessor("pendingTickListEntriesTreeSet")
    TreeSet<NextTickListEntry> liteoptimizer$pendingTicks();
}

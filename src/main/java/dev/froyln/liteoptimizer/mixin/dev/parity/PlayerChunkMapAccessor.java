package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.util.List;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerChunkMap.class)
public interface PlayerChunkMapAccessor {
    @Accessor("entries")
    List<PlayerChunkMapEntry> liteoptimizer$entries();
}

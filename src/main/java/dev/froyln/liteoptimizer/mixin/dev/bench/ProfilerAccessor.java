package dev.froyln.liteoptimizer.mixin.dev.bench;

import java.util.Map;

import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Profiler.class)
public interface ProfilerAccessor {
    @Accessor("profilingMap")
    Map<String, Long> liteoptimizer$profilingMap();
}

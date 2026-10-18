package dev.froyln.liteoptimizer.mixin.dev.bench;

import java.util.List;

import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderGlobal.class)
public interface RenderGlobalAccessor {
    @Accessor("renderInfos")
    List<?> liteoptimizer$renderInfos();
}

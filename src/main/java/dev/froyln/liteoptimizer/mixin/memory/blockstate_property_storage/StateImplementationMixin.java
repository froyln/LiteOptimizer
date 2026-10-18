package dev.froyln.liteoptimizer.mixin.memory.blockstate_property_storage;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import dev.froyln.liteoptimizer.memory.StateTable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Catalog row memory/blockstate_property_storage (FerriteCore §2, FoamFix smallPropertyStorage,
 * VintageFix blockstates).
 *
 * Vanilla gives every blockstate its own ImmutableTable of (property, value) → neighbour state:
 * O(Σ allowed values) cells per state, O(states × Σ values) per block. One {@link StateTable}
 * per container holds the same information as an array plus an int per state. withProperty
 * returns the identical state instance (or null → vanilla's own exception); getValue,
 * getProperties, hashCode and iteration order are untouched.
 */
@Mixin(targets = "net.minecraft.block.state.BlockStateContainer$StateImplementation")
public abstract class StateImplementationMixin {
    // Inner-class (`targets`) mixin: the annotation processor does not remap members, so every member
    // carries its Notch name from mcp_stable_39 notch-mcp.srg (awu$a) as an alias / second name.
    @Shadow(aliases = "b") @Final private ImmutableMap<IProperty<?>, Comparable<?>> properties;

    private StateTable liteoptimizer$table;
    private int liteoptimizer$index;

    @Inject(method = {"buildPropertyValueTable", "a(Ljava/util/Map;)V"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void liteoptimizer$buildTable(Map<Map<IProperty<?>, Comparable<?>>, ? extends IBlockState> map, CallbackInfo ci) {
        StateTable table = StateTable.forContainer(map, this.properties.keySet());
        this.liteoptimizer$table = table;
        this.liteoptimizer$index = table.indexOf(this.properties);
        ci.cancel();
    }

    @Redirect(method = {"withProperty", "a(Laxj;Ljava/lang/Comparable;)Lawt;"}, at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableTable;get(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private Object liteoptimizer$neighbour(ImmutableTable<?, ?, ?> unused, Object property, Object value) {
        return this.liteoptimizer$table.with(this.liteoptimizer$index, (IProperty<?>) property, value);
    }
}

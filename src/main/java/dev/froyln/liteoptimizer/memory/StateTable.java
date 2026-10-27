package dev.froyln.liteoptimizer.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

/**
 * Helper for memory/blockstate_property_storage (FerriteCore's FastMap idea, own code).
 *
 * One instance per BlockStateContainer replaces the ImmutableTable&lt;IProperty, Comparable, IBlockState&gt;
 * that vanilla builds in <em>every</em> state: all states of a container live in one array in
 * mixed-radix order (property order = the container's sorted property map, value order =
 * {@code getAllowedValues()}), and "this state with property p set to v" is index arithmetic.
 * Value lookup uses the same equals/hashCode as vanilla's HashBasedTable did, so an unknown value
 * still yields null and vanilla's own IllegalArgumentException.
 */
public final class StateTable {
    private final IProperty<?>[] props;
    private final int[] stride;
    private final int[] size;
    private final HashMap<Comparable<?>, Integer>[] valueIndex;
    private final IBlockState[] states;

    // The states map is passed once per state; all states of one container pass the same map.
    private static Map<?, ?> lastMap;
    private static StateTable lastTable;

    @SuppressWarnings("unchecked")
    private StateTable(Iterable<IProperty<?>> properties, Map<Map<IProperty<?>, Comparable<?>>, ? extends IBlockState> all) {
        int n = 0;
        for (IProperty<?> p : properties) n++;
        props = new IProperty<?>[n];
        stride = new int[n];
        size = new int[n];
        valueIndex = new HashMap[n];
        int total = 1, i = 0;
        for (IProperty<?> p : properties) {
            props[i] = p;
            stride[i] = total;
            HashMap<Comparable<?>, Integer> idx = new HashMap<Comparable<?>, Integer>();
            for (Comparable<?> v : p.getAllowedValues()) {
                idx.put(v, idx.size());
            }
            valueIndex[i] = idx;
            size[i] = idx.size();
            total *= idx.size();
            i++;
        }
        states = new IBlockState[total];
        for (Map.Entry<Map<IProperty<?>, Comparable<?>>, ? extends IBlockState> e : all.entrySet()) {
            states[indexOf(e.getKey())] = e.getValue();
        }
    }

    /** The table for the container whose states are {@code all}; built once, shared by every state of it. */
    public static StateTable forContainer(Map<Map<IProperty<?>, Comparable<?>>, ? extends IBlockState> all, Iterable<IProperty<?>> properties) {
        if (all != lastMap) {
            lastTable = new StateTable(properties, all);
            lastMap = all;  // ponytail: keeps the last block's temp map alive (a few KB); harmless
        }
        return lastTable;
    }

    public int indexOf(Map<IProperty<?>, Comparable<?>> values) {
        int index = 0;
        for (int i = 0; i < props.length; i++) {
            index += valueIndex[i].get(values.get(props[i])) * stride[i];
        }
        return index;
    }

    /** Neighbour of the state at {@code index} with {@code property} = {@code value}, or null if the value is not allowed. */
    public IBlockState with(int index, IProperty<?> property, Object value) {
        for (int i = 0; i < props.length; i++) {
            if (props[i].equals(property)) {
                Integer vi = valueIndex[i].get(value);
                if (vi == null) return null;
                int cur = (index / stride[i]) % size[i];
                return states[index + (vi - cur) * stride[i]];
            }
        }
        return null;
    }
}

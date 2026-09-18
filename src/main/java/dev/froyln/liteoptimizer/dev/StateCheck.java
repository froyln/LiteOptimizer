package dev.froyln.liteoptimizer.dev;

import dev.froyln.liteoptimizer.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

/**
 * Dev check for memory/blockstate_property_storage, run once when {@code -Dliteoptimizer.dev.statecheck}
 * is set: for every valid state of every block and every (property, allowed value), withProperty must
 * return a valid state of the same block whose property values are the old ones with exactly that
 * one replaced. Vanilla has exactly one state object per value combination, so this pins the result
 * to the identical instance vanilla's table would have returned.
 */
public final class StateCheck {
    private static boolean done = !Boolean.getBoolean("liteoptimizer.dev.statecheck");

    private StateCheck() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void runOnce() {
        if (done) {
            return;
        }
        done = true;
        int states = 0, lookups = 0, failures = 0;
        for (Block block : Block.REGISTRY) {
            for (IBlockState s : block.getBlockState().getValidStates()) {
                states++;
                for (IProperty p : s.getPropertyKeys()) {
                    for (Comparable v : (Iterable<Comparable>) p.getAllowedValues()) {
                        lookups++;
                        IBlockState t = s.withProperty(p, v);
                        boolean ok = t.getBlock() == block && t.getValue(p).equals(v)
                            && block.getBlockState().getValidStates().contains(t)
                            && (v.equals(s.getValue(p)) == (t == s));
                        for (IProperty q : s.getPropertyKeys()) {
                            ok &= q == p || t.getValue(q).equals(s.getValue(q));
                        }
                        if (!ok && failures++ < 10) {
                            Config.LOG.error("[LiteOptimizer] statecheck FAIL: {} with {}={} -> {}", s, p.getName(), v, t);
                        }
                    }
                }
                if (!s.getPropertyKeys().isEmpty()) {
                    try {  // a value no property allows must still raise vanilla's IllegalArgumentException
                        s.withProperty((IProperty) s.getPropertyKeys().iterator().next(), (Comparable) "not-a-value");
                        failures++;
                        Config.LOG.error("[LiteOptimizer] statecheck FAIL: no exception for a bad value on {}", s);
                    } catch (IllegalArgumentException expected) {
                    }
                }
            }
        }
        Config.LOG.info("[LiteOptimizer] statecheck: {} states, {} lookups, {} failures", states, lookups, failures);
    }
}

package dev.froyln.liteoptimizer.dev;

import java.util.Random;

/**
 * Deterministic replacement for vanilla's unseeded {@code new Random()} sites, active only with
 * {@code -Dliteoptimizer.parity=<seed>}. Instance-level sources (World, Entity, Explosion,
 * MinecraftServer) get {@code seed + n} where n counts constructions on the current thread — the
 * server thread and the client thread interleave nondeterministically, so each thread has its
 * own counter and its own offset. Static sources get a seed derived from their owner's name so
 * class-initialisation order does not matter.
 */
public final class ParitySeeds {
    public static final long SEED = Long.getLong("liteoptimizer.parity", 0L);
    private static final ThreadLocal<long[]> COUNTER = new ThreadLocal<long[]>() {
        @Override
        protected long[] initialValue() {
            return new long[] {0L};
        }
    };

    private ParitySeeds() {
    }

    private static final boolean TRACE = System.getProperty("liteoptimizer.parity.trace") != null;

    public static Random next() {
        long[] c = COUNTER.get();
        long threadOffset = Thread.currentThread().getName().hashCode() * 1_000_000_007L;
        long seed = SEED + threadOffset + c[0]++;
        return TRACE ? new TracingRandom(seed) : new Random(seed);
    }

    private static final ThreadLocal<Random> MATH = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            return new Random(SEED ^ 0x4d617468L ^ Thread.currentThread().getName().hashCode());
        }
    };

    /** Replacement for {@code Math.random()} (a global unseeded Random in the JDK). */
    public static double mathRandom() {
        return MATH.get().nextDouble();
    }

    public static Random forStatic(String owner) {
        return new Random(SEED ^ owner.hashCode());
    }
}

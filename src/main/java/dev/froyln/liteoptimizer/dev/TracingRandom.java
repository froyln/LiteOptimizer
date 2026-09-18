package dev.froyln.liteoptimizer.dev;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A {@link Random} that counts calls per call site (harness only, {@code -Dliteoptimizer.parity.trace}).
 * Diffing two histograms names the code path that consumed a different number of random values.
 */
public final class TracingRandom extends Random {
    private static final long serialVersionUID = 1L;
    private final Map<String, int[]> histogram = new HashMap<String, int[]>();
    public long calls;

    public TracingRandom(long seed) {
        super(seed);
    }

    @Override
    protected int next(int bits) {
        StackTraceElement[] st = new Throwable().getStackTrace();
        StringBuilder key = new StringBuilder();
        int depth = 0;
        for (int i = 1; i < st.length && depth < 5; i++) {
            String c = st[i].getClassName();
            if (c.startsWith("java.util.Random") || c.startsWith("dev.froyln.liteoptimizer.dev")) {
                continue;
            }
            key.append(c).append('.').append(st[i].getMethodName()).append(':').append(st[i].getLineNumber()).append(" < ");
            depth++;
        }
        calls++;
        synchronized (histogram) {
            int[] n = histogram.get(key.toString());
            if (n == null) {
                histogram.put(key.toString(), n = new int[1]);
            }
            n[0]++;
        }
        return super.next(bits);
    }

    public void dump(Writer w) throws IOException {
        List<String> keys;
        synchronized (histogram) {
            keys = new ArrayList<String>(histogram.keySet());
            Collections.sort(keys);
            for (String k : keys) {
                w.write("rand " + histogram.get(k)[0] + " " + k + "\n");
            }
        }
    }
}

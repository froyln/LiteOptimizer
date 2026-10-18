package dev.froyln.liteoptimizer.dev;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.regex.Pattern;

/** Reads parity settings without relying on launcher JVM arguments, which may be cached. */
public final class ParityOptions {
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_-]+");
    private static final String[] KEYS = {
            "liteoptimizer.parity", "liteoptimizer.parity.world", "liteoptimizer.parity.ticks",
            "liteoptimizer.parity.label", "liteoptimizer.parity.pose", "liteoptimizer.parity.verbose",
            "liteoptimizer.parity.trace", "liteoptimizer.nopause", "liteoptimizer.bench",
            "liteoptimizer.bench.warmup", "liteoptimizer.bench.seconds", "liteoptimizer.bench.quit",
            "liteoptimizer.bench.profile", "liteoptimizer.bench.fly", "liteoptimizer.bench.noentities",
            "liteoptimizer.screenshot"
    };

    private ParityOptions() {
    }

    public static boolean apply(File file) {
        Properties options = read(file);
        if (options == null) {
            return false;
        }
        for (String key : KEYS) {
            String value = options.getProperty(key);
            if (value != null) {
                System.setProperty(key, value);
            }
        }
        return true;
    }

    private static Properties read(File file) {
        if (!file.isFile()) {
            return null;
        }
        Properties options = new Properties();
        try {
            Reader reader = new FileReader(file);
            try {
                options.load(reader);
            } finally {
                reader.close();
            }
            String world = options.getProperty("liteoptimizer.parity.world", "");
            String label = options.getProperty("liteoptimizer.parity.label", "");
            int ticks = Integer.parseInt(options.getProperty("liteoptimizer.parity.ticks", ""));
            Long.parseLong(options.getProperty("liteoptimizer.parity", ""));
            if (!NAME.matcher(world).matches() || !NAME.matcher(label).matches() || ticks < 1) {
                return null;
            }
            String pose = options.getProperty("liteoptimizer.parity.pose");
            if (pose != null && !pose.matches("[-+0-9._]+")) {
                return null;
            }
            options.setProperty("liteoptimizer.nopause", "1");
            return options;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    /** Self-check: {@code java -ea dev.froyln.liteoptimizer.dev.ParityOptions}. */
    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("liteoptimizer-parity", ".properties");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write("liteoptimizer.parity=17\n"
                    + "liteoptimizer.parity.world=_parity\n"
                    + "liteoptimizer.parity.ticks=600\n"
                    + "liteoptimizer.parity.label=off\n"
                    + "liteoptimizer.parity.pose=-1_64_2_90_0\n"
                    + "liteoptimizer.bench=off\n"
                    + "liteoptimizer.bench.warmup=20\n");
            writer.close();
            if (!apply(file) || !"17".equals(System.getProperty("liteoptimizer.parity"))
                    || !"_parity".equals(System.getProperty("liteoptimizer.parity.world"))
                    || !"off".equals(System.getProperty("liteoptimizer.bench"))
                    || !"20".equals(System.getProperty("liteoptimizer.bench.warmup"))
                    || !"1".equals(System.getProperty("liteoptimizer.nopause"))) {
                throw new AssertionError("valid parity settings were not applied");
            }
            writer = new FileWriter(file);
            writer.write("liteoptimizer.parity=17\nliteoptimizer.parity.world=../outside\n"
                    + "liteoptimizer.parity.ticks=600\nliteoptimizer.parity.label=off\n");
            writer.close();
            if (apply(file)) {
                throw new AssertionError("unsafe world name was accepted");
            }
            System.out.println("ParityOptions self-check OK");
        } finally {
            for (String key : KEYS) {
                System.clearProperty(key);
            }
            file.delete();
        }
    }
}

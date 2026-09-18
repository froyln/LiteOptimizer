package dev.froyln.liteoptimizer.dev;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import net.minecraft.client.Minecraft;
import net.minecraft.server.integrated.IntegratedServer;

import dev.froyln.liteoptimizer.config.Config;

/**
 * {@code -Dliteoptimizer.bench=<label> [-Dliteoptimizer.bench.warmup=30] [-Dliteoptimizer.bench.seconds=60]}
 * (no commas anywhere — PrismLauncher splits JvmArgs on commas): once a world is loaded,
 * waits the warm-up, then records every frame's duration and, when an integrated server is running,
 * its last tick time, to {@code bench/<label>.csv}; then quits if {@code -Dliteoptimizer.bench.quit}
 * is set. Combine with {@code -Dliteoptimizer.parity.world=<save>} to auto-load the world.
 */
public final class BenchRecorder {
    private static final String SPEC = System.getProperty("liteoptimizer.bench");
    private static String label;
    private static long warmupNs = 30_000_000_000L;
    private static long durationNs = 60_000_000_000L;
    private static long worldSeenAt = -1;
    private static long lastFrame = -1;
    private static long startedAt = -1;
    private static int lastTick = -1;
    private static Writer out;
    private static boolean done;

    static {
        if (SPEC != null) {
            label = SPEC;
            warmupNs = (long) (Double.parseDouble(System.getProperty("liteoptimizer.bench.warmup", "30")) * 1e9);
            durationNs = (long) (Double.parseDouble(System.getProperty("liteoptimizer.bench.seconds", "60")) * 1e9);
        }
    }

    private BenchRecorder() {
    }

    /** Called at the head of every {@code Minecraft.runGameLoop()}. */
    public static void onFrame(Minecraft mc) {
        if (SPEC == null || done) {
            return;
        }
        long now = System.nanoTime();
        if (mc.world == null || mc.player == null) {
            worldSeenAt = -1;
            return;
        }
        if (worldSeenAt < 0) {
            worldSeenAt = now;
        }
        if (now - worldSeenAt < warmupNs) {
            lastFrame = -1;
            return;
        }
        try {
            if (out == null) {
                File f = new File("bench", label + ".csv");
                f.getParentFile().mkdirs();
                out = new FileWriter(f);
                out.write("frame_ns,mspt_ms\n");
                startedAt = now;
                Config.LOG.info("[LiteOptimizer] bench '{}' recording for {} s", label, durationNs / 1e9);
            }
            if (lastFrame > 0) {
                String mspt = "";
                IntegratedServer server = mc.getIntegratedServer();
                if (server != null) {
                    int tick = server.getTickCounter();
                    if (tick != lastTick) {
                        lastTick = tick;
                        mspt = String.format("%.3f", server.tickTimeArray[tick % server.tickTimeArray.length] / 1e6);
                    }
                }
                out.write((now - lastFrame) + "," + mspt + "\n");
            }
            lastFrame = now;
            if (now - startedAt >= durationNs) {
                done = true;
                out.close();
                Config.LOG.info("[LiteOptimizer] bench '{}' written to bench/{}.csv", label, label);
                if (System.getProperty("liteoptimizer.bench.quit") != null) {
                    mc.shutdown();
                }
            }
        } catch (IOException e) {
            done = true;
            Config.LOG.error("[LiteOptimizer] bench failed", e);
        }
    }
}

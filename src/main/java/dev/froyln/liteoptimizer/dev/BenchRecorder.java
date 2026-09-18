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
 * {@code -Dliteoptimizer.bench.profile=1} also keeps vanilla's section profiler running for the
 * recording window and writes {@code bench/<label>.profile.txt} (ms per frame per section).
 */
public final class BenchRecorder {
    private static final String SPEC = System.getProperty("liteoptimizer.bench");
    private static String label;
    private static long warmupNs = 30_000_000_000L;
    private static long shotAtTick;
    private static boolean shotTaken;
    private static long durationNs = 60_000_000_000L;
    private static long worldSeenAt = -1;
    private static long lastFrame = -1;
    private static long startedAt = -1;
    private static int lastTick = -1;
    private static Writer out;
    private static boolean done;
    private static final boolean PROFILE = System.getProperty("liteoptimizer.bench.profile") != null;
    private static int frames;

    static {
        if (SPEC != null) {
            label = SPEC;
            warmupNs = (long) (Double.parseDouble(System.getProperty("liteoptimizer.bench.warmup", "30")) * 1e9);
            durationNs = (long) (Double.parseDouble(System.getProperty("liteoptimizer.bench.seconds", "60")) * 1e9);
        }
    }

    private BenchRecorder() {
    }

    /** Called at the end of every {@code Minecraft.runGameLoop()}, after vanilla switched the profiler off. */
    public static void afterFrame(Minecraft mc) {
        if (PROFILE && startedAt > 0 && !done) {
            mc.profiler.profilingEnabled = true;
        }
    }

    private static void writeProfile(Minecraft mc) throws IOException {
        java.util.Map<String, Long> map = ((dev.froyln.liteoptimizer.mixin.dev.bench.ProfilerAccessor) mc.profiler).liteoptimizer$profilingMap();
        long root = map.getOrDefault("root", 1L);
        java.util.List<java.util.Map.Entry<String, Long>> rows = new java.util.ArrayList<>(map.entrySet());
        rows.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        try (Writer w = new FileWriter(new File("bench", label + ".profile.txt"))) {
            w.write(String.format("# %s: %d frames, %.3f ms/frame in profiled sections; ms/frame  %%root  section%n", label, frames, root / 1e6 / Math.max(1, frames)));
            w.write("# renders: " + mc.renderGlobal.getDebugInfoRenders() + "\n");
            for (java.util.Map.Entry<String, Long> e : rows) {
                if (e.getValue() * 1000 < root) {
                    break; // < 0.1 %
                }
                w.write(String.format("%8.3f %6.2f  %s%n", e.getValue() / 1e6 / Math.max(1, frames), 100.0 * e.getValue() / root, e.getKey()));
            }
        }
        Config.LOG.info("[LiteOptimizer] profile written to bench/{}.profile.txt", label);
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
            shotAtTick = mc.world.getTotalWorldTime() + (long) (warmupNs / 1e9) * 20L;
        }
        String shot = System.getProperty("liteoptimizer.screenshot");
        if (shot != null && !shotTaken && mc.world.getTotalWorldTime() >= shotAtTick) {
            // the frame of a fixed client tick, from the main framebuffer: the render-parity
            // evidence. A fixed tick (not wall clock) so the lightmap and world time are the same
            // in every run; see EntityRendererFlickerMixin for the torch flicker.
            shotTaken = true;
            net.minecraft.util.ScreenShotHelper.saveScreenshot(mc.gameDir, shot + ".png", mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
            Config.LOG.info("[LiteOptimizer] screenshot {} saved at client tick {}", shot, shotAtTick);
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
                if (PROFILE) {
                    mc.profiler.clearProfiling();
                    mc.profiler.profilingEnabled = true;
                }
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
                frames++;
            }
            lastFrame = now;
            if (now - startedAt >= durationNs) {
                done = true;
                out.close();
                Config.LOG.info("[LiteOptimizer] bench '{}' written to bench/{}.csv", label, label);
                if (PROFILE) {
                    writeProfile(mc);
                }
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

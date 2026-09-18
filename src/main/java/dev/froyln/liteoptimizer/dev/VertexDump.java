package dev.froyln.liteoptimizer.dev;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Bench profile mode: every built terrain section's vertex bytes (vanilla's BLOCK layout, 28 B per
 * vertex) go to {@code bench/vtx-<label>/<x>_<y>_<z>_<layer>.bin}, from vanilla's BufferBuilder or
 * from Sodium's mesh, so the two renderers' meshes can be compared vertex by vertex
 * ({@code tools/vtx-diff.py}).
 */
public final class VertexDump {
    public static final boolean ENABLED = System.getProperty("liteoptimizer.bench.profile") != null && System.getProperty("liteoptimizer.bench") != null;
    private static final File DIR = new File("bench", "vtx-" + System.getProperty("liteoptimizer.bench"));

    private VertexDump() {
    }

    public static void dump(int x, int y, int z, String layer, ByteBuffer data) {
        if (!ENABLED) {
            return;
        }
        DIR.mkdirs();
        ByteBuffer copy = data.duplicate();
        try (FileChannel ch = new FileOutputStream(new File(DIR, x + "_" + y + "_" + z + "_" + layer + ".bin")).getChannel()) {
            ch.write(copy);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

package dev.froyln.liteoptimizer.render;

import java.util.IdentityHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import org.lwjgl.opengl.GL11;

/**
 * Helper for render/model_display_lists, BlockModelRenderer.renderModelBrightnessColor part:
 * vanilla issues one Tessellator draw per quad (an item frame's frame is ~50 draws, every frame
 * per frame). The output depends only on (state, model, brightness, r, g, b); it is compiled once
 * into a display list and replayed. Only the item-frame call (state == null, the 4-arg overload
 * from RenderItemFrame) is served: the other callers (primed TNT, minecart contents, enderman
 * block, mooshroom/golem layers) are entity poses the parity screenshot cannot cover yet. Entries per model are a short array scanned linearly — a
 * model has a handful of (state, colour) variants, and a lookup allocates nothing.
 */
public final class BlockModelLists {
    private static final int MAX_LISTS = 4096;

    private static final class Entry {
        IBlockState state;
        int brightness, red, green, blue;  // float bits
        int list;
    }

    private static final IdentityHashMap<IBakedModel, Entry[]> BY_MODEL = new IdentityHashMap<IBakedModel, Entry[]>();
    private static int listCount;
    private static boolean compiling;
    private static Entry pending;
    private static IBakedModel pendingModel;

    private BlockModelLists() {}

    /** true = replayed (caller returns); false = vanilla runs, then {@link #end()} must be called. */
    public static boolean begin(IBlockState state, IBakedModel model, float brightness, float red, float green, float blue) {
        if (compiling || state != null) {
            return false;  // not the item-frame call: unmeasured pose
        }
        int b = Float.floatToRawIntBits(brightness), r = Float.floatToRawIntBits(red), g = Float.floatToRawIntBits(green), bl = Float.floatToRawIntBits(blue);
        Entry[] entries = BY_MODEL.get(model);
        if (entries != null) {
            for (int i = 0; i < entries.length; i++) {
                Entry e = entries[i];
                if (e.state == state && e.brightness == b && e.red == r && e.green == g && e.blue == bl) {
                    GlStateManager.callList(e.list);
                    GlStateManager.resetColor();  // a colour-array draw leaves the current colour undefined: vanilla\'s uploader resets too
                    return true;
                }
            }
            if (entries.length >= 16) {
                return false;  // a model with that many variants is not worth caching
            }
        }
        if (listCount >= MAX_LISTS) {
            clear();
        }
        Entry e = new Entry();
        e.state = state;
        e.brightness = b;
        e.red = r;
        e.green = g;
        e.blue = bl;
        e.list = GLAllocation.generateDisplayLists(1);
        pending = e;
        pendingModel = model;
        compiling = true;
        GlStateManager.glNewList(e.list, GL11.GL_COMPILE);
        return false;
    }

    public static void end() {
        if (!compiling) {
            return;
        }
        GlStateManager.glEndList();
        compiling = false;
        GlStateManager.callList(pending.list);
        GlStateManager.resetColor();  // a colour-array draw leaves the current colour undefined: vanilla\'s uploader resets too
        Entry[] entries = BY_MODEL.get(pendingModel);
        Entry[] grown = new Entry[entries == null ? 1 : entries.length + 1];
        if (entries != null) {
            System.arraycopy(entries, 0, grown, 0, entries.length);
        }
        grown[grown.length - 1] = pending;
        BY_MODEL.put(pendingModel, grown);
        listCount++;
        pending = null;
        pendingModel = null;
    }

    public static void clear() {
        for (Entry[] entries : BY_MODEL.values()) {
            for (Entry e : entries) {
                GLAllocation.deleteDisplayLists(e.list);
            }
        }
        BY_MODEL.clear();
        listCount = 0;
    }
}

package dev.froyln.liteoptimizer.render;

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

/**
 * Helper for render/model_display_lists. RenderItem.renderModel tessellates every quad of
 * an item model through the BufferBuilder on every call — per item frame per frame. The vertex data it produces depends only on (model, colour argument, the
 * ItemColors result per tint index), so the draw is compiled once into a display list keyed
 * by exactly that and replayed — the same trick vanilla's ModelRenderer uses for entity parts.
 * GL state (matrices, lightmap, texture, GlStateManager colour) stays outside the list, as in
 * vanilla. Lists are dropped on resource reload (models are rebaked) and when the cache grows
 * past {@link #MAX_LISTS}.
 */
public final class ItemModelLists {
    private static final int MAX_LISTS = 8192;
    private static final int UNTINTED_DEFAULT = -1;
    private static final int GLINT = -8372020;

    private static final class Lists {
        int[] tintIndices;            // distinct tint indices used by the model's quads, sorted; empty = none
        int plain = 0;                // colour -1, no tints (or tints unused because stack is empty)
        int glint = 0;                // colour -8372020
        HashMap<TintKey, Integer> tinted;  // colour -1 with tints: key = resolved colours
    }

    private static final class TintKey {
        int[] colors;
        int hash;

        TintKey(int[] colors) {
            this.colors = colors;
            this.hash = Arrays.hashCode(colors);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof TintKey && Arrays.equals(colors, ((TintKey) o).colors);
        }
    }

    private static final IdentityHashMap<IBakedModel, Lists> BY_MODEL = new IdentityHashMap<IBakedModel, Lists>();
    private static int listCount;
    private static boolean compiling;
    private static boolean enabled;
    // reused for lookups so a hit allocates nothing (one scratch array per tint count)
    private static final int[][] SCRATCH_COLORS = new int[8][];
    private static final TintKey SCRATCH = new TintKey(new int[0]);
    // where the list being compiled goes once RenderItem.renderModel returns
    private static Lists pendingLists;
    private static int pendingSlot;  // 0 = plain, 1 = glint, 2 = tinted
    private static TintKey pendingKey;
    private static int pendingId;

    private ItemModelLists() {}

    /**
     * Called at the head of RenderItem.renderModel. Returns true when the call was served from a
     * display list (the caller must return); false when vanilla must run — and if a list is
     * being compiled around it, {@link #end()} must be called when it returns.
     */
    public static boolean begin(IBakedModel model, int color, ItemStack stack, ItemColors itemColors) {
        if (compiling || !enabled) {
            return false;
        }
        Lists lists = BY_MODEL.get(model);
        if (lists == null) {
            lists = new Lists();
            lists.tintIndices = tintIndices(model);
            BY_MODEL.put(model, lists);
        }
        int slot;
        TintKey key = null;
        int id;
        if (color == UNTINTED_DEFAULT && !stack.isEmpty() && lists.tintIndices.length > 0) {
            int[] tints = lists.tintIndices;
            if (tints.length >= SCRATCH_COLORS.length) {
                return false;  // absurd tint count (mods): not cached
            }
            int[] colors = SCRATCH_COLORS[tints.length];
            if (colors == null) {
                SCRATCH_COLORS[tints.length] = colors = new int[tints.length];
            }
            for (int i = 0; i < tints.length; i++) {
                int k = itemColors.colorMultiplier(stack, tints[i]);
                if (EntityRenderer.anaglyphEnable) {
                    k = TextureUtil.anaglyphColor(k);
                }
                colors[i] = k | -16777216;
            }
            SCRATCH.colors = colors;
            SCRATCH.hash = Arrays.hashCode(colors);
            Integer boxed = lists.tinted == null ? null : lists.tinted.get(SCRATCH);
            id = boxed == null ? 0 : boxed;
            slot = 2;
            if (id == 0) {
                key = new TintKey(colors.clone());
            }
        } else if (color == UNTINTED_DEFAULT) {
            id = lists.plain;
            slot = 0;
        } else if (color == GLINT) {
            id = lists.glint;
            slot = 1;
        } else {
            return false;  // some other colour (mods): not cached
        }
        if (id != 0) {
            GlStateManager.callList(id);
            GlStateManager.resetColor();  // a colour-array draw leaves the current colour undefined: vanilla\'s uploader resets too
            return true;
        }
        if (listCount >= MAX_LISTS) {
            clear();
            lists = new Lists();
            lists.tintIndices = tintIndices(model);
            BY_MODEL.put(model, lists);
        }
        pendingId = GLAllocation.generateDisplayLists(1);
        pendingLists = lists;
        pendingSlot = slot;
        pendingKey = key;
        compiling = true;
        GlStateManager.glNewList(pendingId, GL11.GL_COMPILE);
        return false;
    }

    /** Set by RenderItemMixin around renderItemModel: lists only for the item-frame (FIXED) transform. */
    public static void setEnabled(boolean on) {
        enabled = on;
    }

    /** Called at the return of RenderItem.renderModel: finishes the list started by {@link #begin} and draws it. */
    public static void end() {
        if (!compiling) {
            return;
        }
        GlStateManager.glEndList();
        compiling = false;
        GlStateManager.callList(pendingId);
        GlStateManager.resetColor();  // a colour-array draw leaves the current colour undefined: vanilla\'s uploader resets too
        listCount++;
        if (pendingSlot == 0) {
            pendingLists.plain = pendingId;
        } else if (pendingSlot == 1) {
            pendingLists.glint = pendingId;
        } else {
            if (pendingLists.tinted == null) {
                pendingLists.tinted = new HashMap<TintKey, Integer>(4);
            }
            pendingLists.tinted.put(pendingKey, pendingId);
        }
        pendingLists = null;
        pendingKey = null;
    }

    /** Resource reload (models rebaked) or cache full: free every list. */
    public static void clear() {
        for (Lists lists : BY_MODEL.values()) {
            if (lists.plain != 0) GLAllocation.deleteDisplayLists(lists.plain);
            if (lists.glint != 0) GLAllocation.deleteDisplayLists(lists.glint);
            if (lists.tinted != null) {
                for (int id : lists.tinted.values()) {
                    GLAllocation.deleteDisplayLists(id);
                }
            }
        }
        BY_MODEL.clear();
        listCount = 0;
    }

    private static int[] tintIndices(IBakedModel model) {
        int[] found = new int[0];
        EnumFacing[] facings = EnumFacing.values();
        for (int f = -1; f < 6; f++) {
            List<BakedQuad> quads = model.getQuads((IBlockState) null, f < 0 ? null : facings[f], 0L);
            for (int i = 0, n = quads.size(); i < n; i++) {
                BakedQuad quad = quads.get(i);
                if (quad.hasTintIndex() && Arrays.binarySearch(found, quad.getTintIndex()) < 0) {
                    found = Arrays.copyOf(found, found.length + 1);
                    found[found.length - 1] = quad.getTintIndex();
                    Arrays.sort(found);
                }
            }
        }
        return found;
    }
}

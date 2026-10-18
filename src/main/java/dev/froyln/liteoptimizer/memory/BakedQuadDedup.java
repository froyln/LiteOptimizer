package dev.froyln.liteoptimizer.memory;

import java.util.Arrays;
import java.util.HashMap;

import dev.froyln.liteoptimizer.config.Config;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Helper for memory/baked_quad_dedup. Lives only while ModelBakery.setupModelRegistry runs: every
 * quad FaceBakery bakes is replaced by the first quad with the same vertex data, tint, face and
 * sprite. BakedQuad is immutable in vanilla (every reader copies vertexData), so sharing one
 * instance across models cannot change what is rendered.
 */
public final class BakedQuadDedup {
    private static HashMap<Key, BakedQuad> quads;
    private static int seen;

    private BakedQuadDedup() {}

    public static BakedQuad intern(BakedQuad quad) {
        if (quad.getClass() != BakedQuad.class) {
            return quad;  // subclasses (BakedQuadRetextured) carry extra state — leave them alone
        }
        HashMap<Key, BakedQuad> map = quads;
        if (map == null) {
            quads = map = new HashMap<Key, BakedQuad>(1 << 14);
        }
        seen++;
        Key key = new Key(quad);
        BakedQuad canonical = map.get(key);
        if (canonical == null) {
            map.put(key, quad);
            return quad;
        }
        return canonical;
    }

    /** Called when baking is done: drop the map, log the numbers. */
    public static void finish() {
        if (quads != null && seen > 0) {
            Config.LOG.info("baked_quad_dedup: {} quads baked, {} unique ({} shared)", seen, quads.size(), seen - quads.size());
        }
        quads = null;
        seen = 0;
    }

    private static final class Key {
        private final BakedQuad quad;
        private final int hash;

        Key(BakedQuad quad) {
            this.quad = quad;
            int h = Arrays.hashCode(quad.getVertexData());
            h = h * 31 + quad.getTintIndex();
            h = h * 31 + quad.getFace().ordinal();
            h = h * 31 + System.identityHashCode(quad.getSprite());
            this.hash = h;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key)) return false;
            BakedQuad a = quad, b = ((Key) o).quad;
            return a.getTintIndex() == b.getTintIndex()
                && a.getFace() == b.getFace()
                && a.getSprite() == b.getSprite()
                && Arrays.equals(a.getVertexData(), b.getVertexData());
        }
    }
}

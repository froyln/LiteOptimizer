package me.jellysquid.mods.sodium.client.render.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * LiteOptimizer: the on-demand animated-texture hook (LoliASM/FermiumASM in Vintagium) is our own
 * render/animated_textures_visible_only row; until it lands this is a no-op.
 */
public class SpriteUtil {
    public static void markSpriteActive(TextureAtlasSprite sprite) {
    }
}

package me.jellysquid.mods.sodium.client.gl.compat;

import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import dev.froyln.liteoptimizer.mixin.render.sodium.features.chunk_rendering.AccessorEntityRenderer;

/**
 * LiteOptimizer: Vintagium read GlStateManager's private fog cache through a Forge access
 * transformer; LiteLoader has none, so the values come straight from the driver (a handful of
 * glGet calls per terrain pass — GlStateManager applies every fog change to GL immediately, so the
 * two are always in sync).
 */
public class FogHelper {
    private static final float FAR_PLANE_THRESHOLD_EXP = (float) Math.log(1.0f / 0.0019f);
    private static final float FAR_PLANE_THRESHOLD_EXP2 = MathHelper.sqrt(FAR_PLANE_THRESHOLD_EXP);

    public static float getFogEnd() {
        return GL11.glGetFloat(GL11.GL_FOG_END);
    }

    public static float getFogStart() {
        return GL11.glGetFloat(GL11.GL_FOG_START);
    }

    public static float getFogDensity() {
        return GL11.glGetFloat(GL11.GL_FOG_DENSITY);
    }

    private static int getGlFogMode() {
        return GL11.glGetInteger(GL11.GL_FOG_MODE);
    }

    /**
     * Retrieves the current fog mode from the fixed-function pipeline.
     */
    public static ChunkFogMode getFogMode() {
        if (!GL11.glIsEnabled(GL11.GL_FOG)) {
            return ChunkFogMode.NONE;
        }
        int mode = getGlFogMode();
        switch (mode) {
            case GL11.GL_EXP2:
            case GL11.GL_EXP:
                return ChunkFogMode.EXP2;
            case GL11.GL_LINEAR:
                return ChunkFogMode.LINEAR;
            default:
                throw new UnsupportedOperationException("Unknown fog mode: " + mode);
        }
    }

    public static float getFogCutoff() {
        int mode = getGlFogMode();
        switch (mode) {
            case GL11.GL_LINEAR:
                return getFogEnd();
            case GL11.GL_EXP:
                return FAR_PLANE_THRESHOLD_EXP / getFogDensity();
            case GL11.GL_EXP2:
                return FAR_PLANE_THRESHOLD_EXP2 / getFogDensity();
            default:
                return 0.0f;
        }
    }

    public static float[] getFogColor() {
        EntityRenderer entityRenderer = Minecraft.getMinecraft().entityRenderer;
        AccessorEntityRenderer accessor = (AccessorEntityRenderer) entityRenderer;
        return new float[] {accessor.liteoptimizer$fogColorRed(), accessor.liteoptimizer$fogColorGreen(), accessor.liteoptimizer$fogColorBlue(), 1.0F};
    }
}

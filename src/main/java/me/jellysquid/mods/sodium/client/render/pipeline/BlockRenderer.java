package me.jellysquid.mods.sodium.client.render.pipeline;

import java.util.BitSet;
import java.util.List;

import dev.froyln.liteoptimizer.mixin.render.sodium.core.pipeline.InvokerBlockModelRenderer;
import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.blender.BiomeColorBlender;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * LiteOptimizer: vanilla 1.12.2's {@code BlockModelRenderer.renderModelSmooth/renderModelFlat},
 * step for step, writing into Sodium's chunk buffers. The ambient-occlusion values come from
 * vanilla's own {@code AmbientOcclusionFace} (the class itself, not a re-implementation), the
 * quad bounds from vanilla's {@code fillQuadBounds}, the tint from {@code BlockColors} at the block
 * position (one value per block, as vanilla), the state offset is added by the caller through the
 * chunk offset (rounded once, as putPosition), the shade is the one FaceBakery already baked into
 * the vertex colours, and the colour arithmetic is BufferBuilder.putColorMultiplier's
 * (int-truncated per channel). Vertices keep their baked order. Sodium's own light pipelines,
 * per-vertex biome blending and quad re-orientation are not used for terrain: every one of them
 * changes pixels relative to vanilla.
 */
public class BlockRenderer {
    private final BlockColors blockColors;
    private final BlockModelRenderer vanillaRenderer;
    private final IAmbientOcclusionFace aoFace;
    private final float[] quadBounds = new float[EnumFacing.values().length * 2];
    private final BitSet boundsFlags = new BitSet(3);

    public BlockRenderer(Minecraft client, LightPipelineProvider lighters, BiomeColorBlender biomeColorBlender) {
        this.blockColors = client.getBlockColors();
        this.vanillaRenderer = client.getBlockRendererDispatcher().getBlockModelRenderer();
        this.aoFace = newAmbientOcclusionFace(this.vanillaRenderer);
    }

    /**
     * Vanilla's AmbientOcclusionFace is a package-private inner class, so it is found structurally
     * (the only nested class of BlockModelRenderer with a float[] and an int[] field) and
     * instantiated reflectively; the mixin on it supplies the public interface.
     */
    private static IAmbientOcclusionFace newAmbientOcclusionFace(BlockModelRenderer outer) {
        for (Class<?> nested : BlockModelRenderer.class.getDeclaredClasses()) {
            boolean floats = false;
            boolean ints = false;
            for (java.lang.reflect.Field f : nested.getDeclaredFields()) {
                floats |= f.getType() == float[].class;
                ints |= f.getType() == int[].class;
            }
            if (floats && ints && IAmbientOcclusionFace.class.isAssignableFrom(nested)) {
                try {
                    java.lang.reflect.Constructor<?> c = nested.getDeclaredConstructor(BlockModelRenderer.class);
                    c.setAccessible(true);
                    return (IAmbientOcclusionFace) c.newInstance(outer);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("cannot instantiate AmbientOcclusionFace", e);
                }
            }
        }
        throw new IllegalStateException("BlockModelRenderer.AmbientOcclusionFace not found (mixin not applied?)");
    }

    /**
     * @param seed vanilla's {@code MathHelper.getPositionRandom(pos)} — it is passed to
     *             {@code IBakedModel.getQuads} unchanged, which is what selects weighted variants
     */
    public boolean renderModel(IBlockAccess world, IBlockState state, BlockPos pos, IBakedModel model, ChunkModelBuffers buffers, boolean cull, long seed) {
        boolean smooth = Minecraft.isAmbientOcclusionEnabled() && state.getLightValue() == 0 && model.isAmbientOcclusion();
        boolean rendered = false;

        for (EnumFacing dir : EnumFacing.values()) {
            List<BakedQuad> quads = model.getQuads(state, dir, seed);
            if (!quads.isEmpty() && (!cull || state.shouldSideBeRendered(world, pos, dir))) {
                if (smooth) {
                    this.renderQuadsSmooth(world, state, pos, buffers, quads, dir);
                } else {
                    int brightness = state.getPackedLightmapCoords(world, pos.offset(dir));
                    this.renderQuadsFlat(world, state, pos, brightness, false, buffers, quads, dir);
                }
                rendered = true;
            }
        }

        List<BakedQuad> quads = model.getQuads(state, null, seed);
        if (!quads.isEmpty()) {
            if (smooth) {
                this.renderQuadsSmooth(world, state, pos, buffers, quads, null);
            } else {
                this.renderQuadsFlat(world, state, pos, -1, true, buffers, quads, null);
            }
            rendered = true;
        }

        return rendered;
    }

    /** vanilla renderQuadsSmooth */
    private void renderQuadsSmooth(IBlockAccess world, IBlockState state, BlockPos pos, ChunkModelBuffers buffers, List<BakedQuad> quads, EnumFacing cullFace) {
        ModelVertexSink sink = buffers.getSink(cullFace == null ? ModelQuadFacing.UNASSIGNED : ModelQuadFacing.fromDirection(cullFace));
        sink.ensureCapacity(quads.size() * 4);
        ChunkRenderData.Builder renderData = buffers.getRenderData();
        IAmbientOcclusionFace ao = this.aoFace;
        InvokerBlockModelRenderer bounds = (InvokerBlockModelRenderer) this.vanillaRenderer;

        for (int i = 0, n = quads.size(); i < n; i++) {
            BakedQuad bakedquad = quads.get(i);
            bounds.liteoptimizer$fillQuadBounds(state, bakedquad.getVertexData(), bakedquad.getFace(), this.quadBounds, this.boundsFlags);
            ao.liteoptimizer$update(world, state, pos, bakedquad.getFace(), this.quadBounds, this.boundsFlags);
            float[] colorMultiplier = ao.liteoptimizer$vertexColorMultiplier();
            int[] brightness = ao.liteoptimizer$vertexBrightness();

            float f;
            float f1;
            float f2;
            if (bakedquad.hasTintIndex()) {
                int k = this.blockColors.colorMultiplier(state, world, pos, bakedquad.getTintIndex());
                if (EntityRenderer.anaglyphEnable) {
                    k = TextureUtil.anaglyphColor(k);
                }
                f = (float) (k >> 16 & 255) / 255.0F;
                f1 = (float) (k >> 8 & 255) / 255.0F;
                f2 = (float) (k & 255) / 255.0F;
            } else {
                f = 1.0F;
                f1 = 1.0F;
                f2 = 1.0F;
            }

            ModelQuadView src = (ModelQuadView) bakedquad;
            for (int v = 0; v < 4; v++) {
                float m = colorMultiplier[v];
                int color = bakedquad.hasTintIndex()
                        ? ColorABGR.mul(src.getColor(v), m * f, m * f1, m * f2)
                        : ColorABGR.mul(src.getColor(v), m, m, m);
                sink.writeQuad(src.getX(v), src.getY(v), src.getZ(v),
                        color, src.getTexU(v), src.getTexV(v), brightness[v]);
            }
            this.addSprite(renderData, src);
        }

        sink.flush();
    }

    /** vanilla renderQuadsFlat */
    private void renderQuadsFlat(IBlockAccess world, IBlockState state, BlockPos pos, int brightnessIn, boolean ownBrightness, ChunkModelBuffers buffers, List<BakedQuad> quads, EnumFacing cullFace) {
        ModelVertexSink sink = buffers.getSink(cullFace == null ? ModelQuadFacing.UNASSIGNED : ModelQuadFacing.fromDirection(cullFace));
        sink.ensureCapacity(quads.size() * 4);
        ChunkRenderData.Builder renderData = buffers.getRenderData();
        InvokerBlockModelRenderer bounds = (InvokerBlockModelRenderer) this.vanillaRenderer;

        for (int i = 0, n = quads.size(); i < n; i++) {
            BakedQuad bakedquad = quads.get(i);
            int brightness = brightnessIn;
            if (ownBrightness) {
                bounds.liteoptimizer$fillQuadBounds(state, bakedquad.getVertexData(), bakedquad.getFace(), null, this.boundsFlags);
                BlockPos blockpos = this.boundsFlags.get(0) ? pos.offset(bakedquad.getFace()) : pos;
                brightness = state.getPackedLightmapCoords(world, blockpos);
            }

            ModelQuadView src = (ModelQuadView) bakedquad;
            if (bakedquad.hasTintIndex()) {
                int k = this.blockColors.colorMultiplier(state, world, pos, bakedquad.getTintIndex());
                if (EntityRenderer.anaglyphEnable) {
                    k = TextureUtil.anaglyphColor(k);
                }
                float f = (float) (k >> 16 & 255) / 255.0F;
                float f1 = (float) (k >> 8 & 255) / 255.0F;
                float f2 = (float) (k & 255) / 255.0F;
                for (int v = 0; v < 4; v++) {
                    sink.writeQuad(src.getX(v), src.getY(v), src.getZ(v),
                            ColorABGR.mul(src.getColor(v), f, f1, f2), src.getTexU(v), src.getTexV(v), brightness);
                }
            } else {
                for (int v = 0; v < 4; v++) {
                    sink.writeQuad(src.getX(v), src.getY(v), src.getZ(v),
                            src.getColor(v), src.getTexU(v), src.getTexV(v), brightness);
                }
            }
            this.addSprite(renderData, src);
        }

        sink.flush();
    }

    private void addSprite(ChunkRenderData.Builder renderData, ModelQuadView src) {
        TextureAtlasSprite sprite = src.rubidium$getSprite();
        if (sprite != null) {
            renderData.addSprite(sprite);
        }
    }
}

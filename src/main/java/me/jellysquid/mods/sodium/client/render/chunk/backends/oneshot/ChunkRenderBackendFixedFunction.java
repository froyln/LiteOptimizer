package me.jellysquid.mods.sodium.client.render.chunk.backends.oneshot;

import java.nio.FloatBuffer;

import me.jellysquid.mods.sodium.client.gl.buffer.GlBufferTarget;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.util.BufferSlice;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkCameraContext;
import me.jellysquid.mods.sodium.client.render.chunk.format.sfp.SFPModelVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterator;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

/**
 * LiteOptimizer: the parity backend. Sodium builds the meshes (worker threads, chunk-graph
 * culling, one VBO per section, per-face slices), but every section is drawn exactly the way
 * vanilla's {@code VboRenderList.renderChunkLayer} draws it: through the fixed-function pipeline,
 * with vanilla's {@code DefaultVertexFormats.BLOCK} layout, the per-chunk translation on the
 * modelview stack and {@code RenderChunk}'s 1.000001 seam-hiding matrix. Same GL state, same
 * vertex bytes, same draws, so the driver produces the same pixels — no shader, no uniforms.
 * Vanilla's caller ({@code RenderGlobal.renderBlockLayer}) has already bound the atlas, the
 * lightmap and enabled the client arrays.
 */
public class ChunkRenderBackendFixedFunction extends ChunkRenderBackendOneshot {
    private final int stride;

    private static FloatBuffer seamMatrix;

    public ChunkRenderBackendFixedFunction(ChunkVertexType vertexType) {
        super(vertexType);

        if (!(vertexType instanceof SFPModelVertexType)) {
            throw new IllegalArgumentException("the fixed-function backend needs the vanilla (float) vertex layout");
        }

        this.stride = vertexType.getCustomVertexFormat().getStride();
    }

    /** vanilla RenderChunk.initModelviewMatrix, captured through the same GL calls so the matrix is bit-identical */
    private static FloatBuffer seamMatrix() {
        if (seamMatrix == null) {
            FloatBuffer buf = GLAllocation.createDirectFloatBuffer(16);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(-8.0F, -8.0F, -8.0F);
            GlStateManager.scale(1.000001F, 1.000001F, 1.000001F);
            GlStateManager.translate(8.0F, 8.0F, 8.0F);
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, buf);
            GlStateManager.popMatrix();
            seamMatrix = buf;
        }
        return seamMatrix;
    }

    @Override
    public void createShaders(RenderDevice device) {
        // fixed function
    }

    /** vanilla RenderGlobal.renderBlockLayer(BlockRenderLayer): the client arrays around renderChunkLayer */
    @Override
    public void begin() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
    }

    @Override
    public void end() {
        // vanilla VboRenderList.renderChunkLayer tail, then the element loop of renderBlockLayer
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
        GlStateManager.resetColor();

        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GlStateManager.resetColor();
    }

    @Override
    protected boolean needsTessellation() {
        return false;
    }

    @Override
    public void render(CommandList commandList, ChunkRenderListIterator<ChunkOneshotGraphicsState> it, ChunkCameraContext camera) {
        int stride = this.stride;

        while (it.hasNext()) {
            ChunkOneshotGraphicsState state = it.getGraphicsState();

            int draws = this.buildRanges(state, it.getVisibleFaces());

            if (draws > 0) {
                // vanilla VboRenderList.renderChunkLayer: push, translate, seam matrix, pointers, draw, pop
                GlStateManager.pushMatrix();
                GlStateManager.translate(camera.getChunkModelOffset(state.getX(), camera.x),
                        camera.getChunkModelOffset(state.getY(), camera.y),
                        camera.getChunkModelOffset(state.getZ(), camera.z));
                GlStateManager.multMatrix(seamMatrix());

                commandList.bindBuffer(GlBufferTarget.ARRAY_BUFFER, state.getVertexBuffer());
                GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
                GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 12);
                GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 16);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, stride, 24);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

                for (int i = 0; i < draws; i++) {
                    GlStateManager.glDrawArrays(GL11.GL_QUADS, this.first[i], this.count[i]);
                }

                GlStateManager.popMatrix();
            }

            it.advance();
        }

        commandList.unbindBuffer(GlBufferTarget.ARRAY_BUFFER);
    }

    private final int[] first = new int[ModelQuadFacing.COUNT];
    private final int[] count = new int[ModelQuadFacing.COUNT];

    /**
     * The visible face slices as draw ranges; adjacent slices are merged (the buffer is laid out in
     * facing order), so a section is usually one or two glDrawArrays like vanilla's one.
     * glMultiDrawArrays is not used: on the NVIDIA driver it costs ~10x a plain draw per range.
     */
    private int buildRanges(ChunkOneshotGraphicsState state, int visibleFaces) {
        int n = 0;

        for (int i = 0; i < ModelQuadFacing.COUNT; i++) {
            if ((visibleFaces & (1 << i)) == 0) {
                continue;
            }

            long part = state.getModelPart(i);
            int start = BufferSlice.unpackStart(part);
            int len = BufferSlice.unpackLength(part);

            if (len == 0) {
                continue;
            }

            if (n > 0 && this.first[n - 1] + this.count[n - 1] == start) {
                this.count[n - 1] += len;
            } else {
                this.first[n] = start;
                this.count[n] = len;
                n++;
            }
        }

        return n;
    }

    @Override
    public String getRendererName() {
        return "FixedFunction";
    }
}

package me.jellysquid.mods.sodium.client.render.chunk.format.sfp;

import me.jellysquid.mods.sodium.client.util.CompatMemoryUtil;

import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.render.chunk.format.DefaultModelVertexFormats;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;

public class SFPModelVertexBufferWriterUnsafe extends VertexBufferWriterUnsafe implements ModelVertexSink {
    public SFPModelVertexBufferWriterUnsafe(VertexBufferView backingBuffer) {
        super(backingBuffer, DefaultModelVertexFormats.MODEL_VERTEX_SFP);
    }

    @Override
    public void writeQuad(float x, float y, float z, int color, float u, float v, int light) {
        long i = this.writePointer;

        CompatMemoryUtil.memPutFloat(i, x);
        CompatMemoryUtil.memPutFloat(i + 4, y);
        CompatMemoryUtil.memPutFloat(i + 8, z);
        CompatMemoryUtil.memPutInt(i + 12, color);
        CompatMemoryUtil.memPutFloat(i + 16, u);
        CompatMemoryUtil.memPutFloat(i + 20, v);
        CompatMemoryUtil.memPutInt(i + 24, light); // LiteOptimizer: vanilla's raw (sky << 16 | block) short pair, as putBrightness4

        this.advance();
    }

}

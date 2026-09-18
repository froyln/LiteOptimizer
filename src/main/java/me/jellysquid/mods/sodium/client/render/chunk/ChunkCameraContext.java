package me.jellysquid.mods.sodium.client.render.chunk;

public class ChunkCameraContext {
    public final double x, y, z;

    public ChunkCameraContext(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * LiteOptimizer: vanilla's per-chunk translation is
     * {@code (float)(chunkBlockPos - viewEntityPos)} (VboRenderList.preRenderChunk) — one rounding
     * of the exact double difference, so the same float reaches the GPU.
     */
    public float getChunkModelOffset(int chunkBlockPos, double cameraPos) {
        return (float) (chunkBlockPos - cameraPos);
    }
}

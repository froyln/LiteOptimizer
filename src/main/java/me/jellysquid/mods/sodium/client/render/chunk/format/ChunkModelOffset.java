package me.jellysquid.mods.sodium.client.render.chunk.format;

public class ChunkModelOffset {
    public float x, y, z;

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** vanilla BufferBuilder.putPosition: (float)(x + xOffset) with x = blockPos + state offset, both doubles */
    public void set(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }
}

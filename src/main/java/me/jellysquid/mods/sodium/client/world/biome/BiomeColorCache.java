package me.jellysquid.mods.sodium.client.world.biome;

import java.util.Arrays;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * Per-section memo of vanilla's BiomeColorHelper.getColorAtPos: the 3×3 average of a biome colour
 * around a position. LiteOptimizer: vanilla's resolvers depend on the block's y (temperature
 * changes above y 64), so both the raw and the blended memo are 3-D over the 16 rows of the
 * section; the radius is vanilla's 1 and the average is vanilla's integer arithmetic, so the
 * numbers are the ones vanilla would compute — this only avoids computing them again for every
 * face of every block.
 */
public class BiomeColorCache {
    private static final int RADIUS = 1;
    private static final int PAD = RADIUS + 2;
    private static final int RAW_DIM = 16 + 2 * PAD;
    private static final int BLENDED_DIM = 16 + 2 * 2;
    private static final int ROWS = 16;

    private final ColorResolver resolver;
    private final WorldSlice slice;
    private final int[] raw;
    private final int[] blended;
    private final int rawMinX, rawMinZ, blendedMinX, blendedMinZ, minY;
    private final BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();

    public BiomeColorCache(ColorResolver resolver, WorldSlice slice) {
        this.resolver = resolver;
        this.slice = slice;
        ChunkPos origin = slice.getOrigin().toChunkPos();
        this.rawMinX = (origin.x << 4) - PAD;
        this.rawMinZ = (origin.z << 4) - PAD;
        this.blendedMinX = (origin.x << 4) - 2;
        this.blendedMinZ = (origin.z << 4) - 2;
        this.minY = slice.getOrigin().getMinY();
        this.raw = new int[RAW_DIM * RAW_DIM * ROWS];
        this.blended = new int[BLENDED_DIM * BLENDED_DIM * ROWS];
        Arrays.fill(this.raw, -1);
        Arrays.fill(this.blended, -1);
    }

    public int getBlendedColor(BlockPos pos) {
        int y = pos.getY() - this.minY;
        int x = pos.getX() - this.blendedMinX;
        int z = pos.getZ() - this.blendedMinZ;
        if (y < 0 || y >= ROWS || x < 0 || x >= BLENDED_DIM || z < 0 || z >= BLENDED_DIM) {
            return this.calculateBlendedColor(pos.getX(), pos.getY(), pos.getZ());
        }
        int index = (y * BLENDED_DIM + x) * BLENDED_DIM + z;
        int color = this.blended[index];
        if (color == -1) {
            this.blended[index] = color = this.calculateBlendedColor(pos.getX(), pos.getY(), pos.getZ());
        }
        return color;
    }

    /** Vanilla: sum over the 3×3 (x,z) neighbourhood at the same y, integer-divided by 9. */
    private int calculateBlendedColor(int posX, int posY, int posZ) {
        int i = 0;
        int j = 0;
        int k = 0;
        for (int x = posX - RADIUS; x <= posX + RADIUS; x++) {
            for (int z = posZ - RADIUS; z <= posZ + RADIUS; z++) {
                int l = this.getColor(x, posY, z);
                i += (l & 16711680) >> 16;
                j += (l & 65280) >> 8;
                k += l & 255;
            }
        }
        return (i / 9 & 255) << 16 | (j / 9 & 255) << 8 | k / 9 & 255;
    }

    private int getColor(int x, int y, int z) {
        int ly = y - this.minY;
        int lx = x - this.rawMinX;
        int lz = z - this.rawMinZ;
        if (ly < 0 || ly >= ROWS || lx < 0 || lx >= RAW_DIM || lz < 0 || lz >= RAW_DIM) {
            return this.calculateColor(x, y, z);
        }
        int index = (ly * RAW_DIM + lx) * RAW_DIM + lz;
        int color = this.raw[index];
        if (color == -1) {
            this.raw[index] = color = this.calculateColor(x, y, z);
        }
        return color;
    }

    private int calculateColor(int x, int y, int z) {
        this.scratch.setPos(x, y, z);
        return this.resolver.getColorAtPos(this.slice.getBiome(x, y, z), this.scratch);
    }
}

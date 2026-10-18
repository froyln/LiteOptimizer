package dev.froyln.liteoptimizer.dev;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;

import dev.froyln.liteoptimizer.config.Config;
import dev.froyln.liteoptimizer.mixin.dev.parity.WorldServerAccessor;

/**
 * Hashes the whole loaded state of a server world so two runs can be diffed: per chunk (sorted by
 * x,z) block data + light, tile entities (sorted by position), entities (sorted by id); then the
 * scheduled tick list, world time and the internal seed of {@code World.rand}. One line per chunk so
 * a mismatch names the chunk; one final line with the overall hash.
 */
public final class ParityDump {
    private static final boolean VERBOSE = System.getProperty("liteoptimizer.parity.verbose") != null;
    private static Writer detail;

    private ParityDump() {
    }

    public static File run(WorldServer world, String label) throws Exception {
        File out = new File("parity", label + ".txt");
        out.getParentFile().mkdirs();
        MessageDigest total = MessageDigest.getInstance("SHA-256");
        Writer w = new FileWriter(out);
        detail = VERBOSE ? new FileWriter(new File("parity", label + ".detail.txt")) : null;
        try {
            List<Chunk> chunks = new ArrayList<Chunk>(((ChunkProviderServer) world.getChunkProvider()).getLoadedChunks());
            Collections.sort(chunks, new Comparator<Chunk>() {
                @Override
                public int compare(Chunk a, Chunk b) {
                    return a.x != b.x ? Integer.compare(a.x, b.x) : Integer.compare(a.z, b.z);
                }
            });
            w.write("chunks " + chunks.size() + "\n");
            for (Chunk chunk : chunks) {
                String h = hashChunk(chunk);
                total.update(h.getBytes(StandardCharsets.UTF_8));
                w.write("chunk " + chunk.x + " " + chunk.z + " " + h + "\n");
            }
            StringBuilder ticks = new StringBuilder();
            for (NextTickListEntry e : ((WorldServerAccessor) world).liteoptimizer$pendingTicks()) {
                ticks.append(e.position.toLong()).append(':').append(net.minecraft.block.Block.getIdFromBlock(e.getBlock()))
                        .append(':').append(e.scheduledTime).append(':').append(e.priority).append('\n');
            }
            String tickHash = sha(ticks.toString());
            total.update(tickHash.getBytes(StandardCharsets.UTF_8));
            w.write("pendingTicks " + ticks.length() + " " + tickHash + "\n");
            String time = world.getTotalWorldTime() + " " + world.getWorldTime() + " " + randSeed(world.rand);
            total.update(time.getBytes(StandardCharsets.UTF_8));
            w.write("time " + time + "\n");
            w.write("total " + hex(total.digest()) + "\n");
            if (detail != null) {
                detail.write(ParityRun.SERIES.toString());
            }
            if (detail != null && world.rand instanceof TracingRandom) {
                ((TracingRandom) world.rand).dump(detail);
            }
        } finally {
            w.close();
            if (detail != null) {
                detail.close();
            }
        }
        Config.LOG.info("[LiteOptimizer] parity dump written to {}", out.getAbsolutePath());
        return out;
    }

    private static String hashChunk(Chunk chunk) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        for (ExtendedBlockStorage s : chunk.getBlockStorageArray()) {
            if (s == Chunk.NULL_BLOCK_STORAGE) {
                md.update((byte) 0);
                continue;
            }
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            s.getData().write(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            buf.release();
            md.update(bytes);
            md.update(s.getBlockLight().getData());
            if (s.getSkyLight() != null) {
                md.update(s.getSkyLight().getData());
            }
        }
        List<Map.Entry<BlockPos, TileEntity>> tes = new ArrayList<Map.Entry<BlockPos, TileEntity>>(chunk.getTileEntityMap().entrySet());
        Collections.sort(tes, new Comparator<Map.Entry<BlockPos, TileEntity>>() {
            @Override
            public int compare(Map.Entry<BlockPos, TileEntity> a, Map.Entry<BlockPos, TileEntity> b) {
                return Long.compare(a.getKey().toLong(), b.getKey().toLong());
            }
        });
        for (Map.Entry<BlockPos, TileEntity> e : tes) {
            String nbt = e.getValue().writeToNBT(new NBTTagCompound()).toString();
            md.update(nbt.getBytes(StandardCharsets.UTF_8));
            if (detail != null) {
                detail.write("te " + chunk.x + " " + chunk.z + " " + nbt + "\n");
            }
        }
        List<Entity> entities = new ArrayList<Entity>();
        for (ClassInheritanceMultiMap<Entity> section : chunk.getEntityLists()) {
            for (Entity entity : section) {
                entities.add(entity);
            }
        }
        Collections.sort(entities, new Comparator<Entity>() {
            @Override
            public int compare(Entity a, Entity b) {
                return Integer.compare(a.getEntityId(), b.getEntityId());
            }
        });
        for (Entity entity : entities) {
            NBTTagCompound nbt = new NBTTagCompound();
            entity.writeToNBT(nbt);
            if (entity instanceof EntityLivingBase) {
                canonicalizeEffects(nbt);
            }
            md.update((entity.getEntityId() + " " + nbt).getBytes(StandardCharsets.UTF_8));
            if (detail != null) {
                detail.write("entity " + chunk.x + " " + chunk.z + " " + entity.getEntityId() + " " + nbt + "\n");
            }
        }
        return hex(md.digest());
    }

    private static void canonicalizeEffects(NBTTagCompound nbt) {
        NBTTagList effects = nbt.getTagList("ActiveEffects", 10);
        List<NBTTagCompound> sorted = new ArrayList<NBTTagCompound>(effects.tagCount());
        for (int i = 0; i < effects.tagCount(); i++) {
            sorted.add(effects.getCompoundTagAt(i));
        }
        Collections.sort(sorted, new Comparator<NBTTagCompound>() {
            @Override
            public int compare(NBTTagCompound a, NBTTagCompound b) {
                int id = Integer.compare(a.getByte("Id") & 0xff, b.getByte("Id") & 0xff);
                return id != 0 ? id : Integer.compare(a.getByte("Amplifier") & 0xff, b.getByte("Amplifier") & 0xff);
            }
        });
        NBTTagList canonical = new NBTTagList();
        for (NBTTagCompound effect : sorted) {
            canonical.appendTag(effect);
        }
        nbt.setTag("ActiveEffects", canonical);
    }

    private static String randSeed(Random random) {
        try {
            Field f = Random.class.getDeclaredField("seed");
            f.setAccessible(true);
            return Long.toString(((AtomicLong) f.get(random)).get());
        } catch (Exception e) {
            return "?";
        }
    }

    private static String sha(String s) throws Exception {
        return hex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 15, 16)).append(Character.forDigit(b & 15, 16));
        }
        return sb.toString();
    }
}

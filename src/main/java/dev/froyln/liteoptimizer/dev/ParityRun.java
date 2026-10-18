package dev.froyln.liteoptimizer.dev;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;

import dev.froyln.liteoptimizer.mixin.dev.parity.PlayerChunkMapAccessor;

import dev.froyln.liteoptimizer.config.Config;

/**
 * Drives one parity run: {@code -Dliteoptimizer.parity=<seed> -Dliteoptimizer.parity.world=<save>
 * -Dliteoptimizer.parity.ticks=<n> -Dliteoptimizer.parity.label=<name> [-Dliteoptimizer.parity.pose=x_y_z_yaw_pitch] (underscores: PrismLauncher splits JvmArgs on commas and spaces)}.
 * The main menu loads the world; once a player is in, ticks are counted; at tick 0 the player is
 * teleported to the pose; at tick n the overworld is dumped and the game exits.
 */
public final class ParityRun {
    public static final String WORLD = System.getProperty("liteoptimizer.parity.world");
    public static final int TICKS = Integer.getInteger("liteoptimizer.parity.ticks", 6000);
    public static final String LABEL = System.getProperty("liteoptimizer.parity.label", "run");
    public static final String POSE = System.getProperty("liteoptimizer.parity.pose");
    private static int tick = -1;
    private static boolean joined;
    private static boolean done;
    /** per-tick "loaded sent" chunk counts, written to the detail file in verbose mode */
    public static final StringBuilder SERIES = new StringBuilder();

    private ParityRun() {
    }

    /** A wall clock that advances 50 ms per counted server tick (see PathNavigateMixin). */
    public static long tickMillis() {
        return 1_000_000_000L + Math.max(tick, 0) * 50L;
    }

    /**
     * Worlds do not tick until the player has joined, so both runs simulate exactly the same
     * ticks: the join takes a variable number of server ticks (chunk loading, handshake).
     */
    public static boolean worldTicksAllowed() {
        return WORLD == null || joined;
    }

    private static long liteoptimizer$posHash(WorldServer w) {
        long h = 0;
        for (int i = 0; i < w.loadedEntityList.size(); i++) {
            net.minecraft.entity.Entity e = w.loadedEntityList.get(i);
            h = 31 * h + Double.doubleToLongBits(e.posX);
            h = 31 * h + Double.doubleToLongBits(e.posY);
            h = 31 * h + Double.doubleToLongBits(e.posZ);
            h = 31 * h + Float.floatToIntBits(e.rotationYaw);
        }
        return h;
    }

    /** Called at the head of every {@code MinecraftServer.tick()}. */
    public static void onServerTick(MinecraftServer server) {
        if (done || server.getPlayerList().getCurrentPlayerCount() == 0) {
            return;
        }
        joined = true;
        tick++;
        if (tick == 0) {
            if (POSE != null) {
                String name = server.getPlayerList().getPlayers().get(0).getName();
                server.getCommandManager().executeCommand(server, "tp " + name + " " + POSE.replace('_', ' '));
            }
            Config.LOG.info("[LiteOptimizer] parity run '{}' started: {} ticks", LABEL, TICKS);
        }
        if (System.getProperty("liteoptimizer.parity.verbose") != null) {
            WorldServer w = server.getWorld(0);
            int sent = 0;
            for (PlayerChunkMapEntry e : ((PlayerChunkMapAccessor) w.getPlayerChunkMap()).liteoptimizer$entries()) {
                if (e.isSentToPlayers()) {
                    sent++;
                }
            }
            SERIES.append("tick ").append(tick).append(" loaded ").append(((ChunkProviderServer) w.getChunkProvider()).getLoadedChunkCount())
                    .append(" sent ").append(sent).append(" players ").append(w.playerEntities.size())
                    .append(" rand ").append(w.rand instanceof TracingRandom ? ((TracingRandom) w.rand).calls : -1)
                    .append(" pos ").append(w.playerEntities.isEmpty() ? "-" : w.playerEntities.get(0).getPosition())
                    .append(" entities ").append(w.loadedEntityList.size()).append(" posHash ").append(liteoptimizer$posHash(w)).append('\n');
        }
        if (tick == TICKS) {
            done = true;
            try {
                WorldServer world = server.getWorld(0);
                ParityDump.run(world, LABEL);
            } catch (Exception e) {
                Config.LOG.error("[LiteOptimizer] parity dump failed", e);
            }
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    Minecraft.getMinecraft().shutdown();
                }
            });
        }
    }
}

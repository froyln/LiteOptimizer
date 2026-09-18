package me.jellysquid.mods.sodium.client;

import java.io.File;

import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** LiteOptimizer: Vintagium's Forge entry point reduced to the static accessors the renderer uses. */
public class SodiumClientMod {
    public static final String MODID = "liteoptimizer";
    public static final String MODNAME = "LiteOptimizer/Sodium";
    private static SodiumGameOptions CONFIG;
    public static Logger LOGGER = LogManager.getLogger(MODNAME);

    public static SodiumGameOptions options() {
        if (CONFIG == null) {
            CONFIG = SodiumGameOptions.load(new File(Minecraft.getMinecraft().gameDir, "liteconfig/common/liteoptimizer-sodium.json").toPath());
        }
        return CONFIG;
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static String getVersion() {
        return dev.froyln.liteoptimizer.LiteModLiteOptimizer.VERSION;
    }

    public static boolean isDirectMemoryAccessEnabled() {
        return options().advanced.allowDirectMemoryAccess;
    }
}

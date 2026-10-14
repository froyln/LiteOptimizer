package dev.froyln.liteoptimizer;

import java.io.File;

import com.mumfrey.liteloader.LiteMod;

import dev.froyln.liteoptimizer.config.Config;
import dev.froyln.liteoptimizer.config.Option;

/**
 * LiteLoader entry point. The optimizations are mixins under {@code dev.froyln.liteoptimizer.mixin},
 * gated by {@link dev.froyln.liteoptimizer.config.MixinPlugin}; this class only registers the mod
 * and reports what is active.
 */
public class LiteModLiteOptimizer implements LiteMod {
    public static final String VERSION = "@MOD_VERSION@";

    @Override
    public String getName() {
        return "LiteOptimizer";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void init(File configPath) {
        StringBuilder off = new StringBuilder();
        for (Option option : Option.values()) {
            if (!Config.isActive(option)) {
                off.append(off.length() == 0 ? "" : ", ").append(option.key);
            }
        }
        Config.LOG.info("[LiteOptimizer] {} loaded; disabled: {}", VERSION, off.length() == 0 ? "none" : off);
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }
}

package dev.froyln.liteoptimizer.config;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.launchwrapper.Launch;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import dev.froyln.liteoptimizer.dev.ParityOptions;

/**
 * Decides per mixin class whether it is applied, from the package name:
 * {@code dev.froyln.liteoptimizer.mixin.<pillar>.<key>.X} is applied iff the pillar and the key are
 * enabled in {@link Config}. {@code mixin.dev.<name>.X} (harness/bench tooling) is applied iff the
 * system property {@code liteoptimizer.<name>} is set — never from the config file.
 */
public final class MixinPlugin implements IMixinConfigPlugin {
    private static final String ROOT = "dev.froyln.liteoptimizer.mixin.";
    private final Set<String> reported = new HashSet<String>();

    @Override
    public void onLoad(String mixinPackage) {
        if (ParityOptions.apply(new File(Launch.minecraftHome, "parity/run.properties"))) {
            Config.LOG.info("[LiteOptimizer] parity settings loaded from game directory");
        }
        Config.load(new File(Launch.minecraftHome, "liteconfig/common/liteoptimizer.json"));
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(ROOT)) {
            return true;
        }
        String[] parts = mixinClassName.substring(ROOT.length()).split("\\.");
        if (parts.length < 3) {
            Config.LOG.warn("[LiteOptimizer] mixin {} is not under mixin.<pillar>.<key>; applying", mixinClassName);
            return true;
        }
        String pillar = parts[0];
        String key = parts[1];
        boolean apply;
        if (pillar.equals("dev")) {
            apply = System.getProperty("liteoptimizer." + key) != null;
        } else {
            Option option = Option.byKey(key);
            Option pillarOption = Option.byKey(pillar);
            if (option == null || pillarOption == null || option.pillar != pillarOption) {
                Config.LOG.warn("[LiteOptimizer] mixin {} has no matching Option ({}/{}); applying", mixinClassName, pillar, key);
                return true;
            }
            apply = Config.isActive(option);
        }
        if (reported.add(pillar + "/" + key)) {
            Config.LOG.info("[LiteOptimizer] {}/{}: {}", pillar, key, apply ? "enabled" : "disabled");
        }
        return apply;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}

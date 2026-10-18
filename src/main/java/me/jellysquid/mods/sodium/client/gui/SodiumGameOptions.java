package me.jellysquid.mods.sodium.client.gui;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.gui.options.FormattedTextProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SodiumGameOptions {
    public final QualitySettings quality = new QualitySettings();
    public final AdvancedSettings advanced = new AdvancedSettings();
    public final PerformanceSettings performance = new PerformanceSettings();
    public final NotificationSettings notifications = new NotificationSettings();

    private Path configPath;

    public static class AdvancedSettings {
        public boolean useVertexArrayObjects = true;
        // LiteOptimizer: shader backends are excluded until they preserve vanilla pixels.
        public boolean useChunkMultidraw = false;

        public boolean animateOnlyVisibleTextures = true;
        public boolean useEntityCulling = false;
        public boolean useParticleCulling = true;
        public boolean useFogOcclusion = false;
        // LiteOptimizer: off = vanilla's BLOCK vertex layout (needed by the fixed-function backend)
        public boolean useCompactVertexFormat = false;
        // LiteOptimizer: off = one buffer per pass in vanilla's block order (pixel parity: z-fights
        // between coplanar quads resolve as in vanilla); on = per-face slices, back faces skipped
        public boolean useBlockFaceCulling = false;
        public boolean allowDirectMemoryAccess = true;
        public boolean ignoreDriverBlacklist = false;
        // LiteOptimizer: vanilla sorts translucent quads back-to-front on every rebuild/resort
        public boolean translucencySorting = true;
        public boolean disableIncompatibleModWarnings = false;
    }

    public static class PerformanceSettings {
        public int chunkBuilderThreads = 0;
        public boolean alwaysDeferChunkUpdates = false;
    }

    public static class QualitySettings {
        public GraphicsQuality cloudQuality = GraphicsQuality.DEFAULT;
        public GraphicsQuality weatherQuality = GraphicsQuality.DEFAULT;
        public GraphicsQuality leavesQuality = GraphicsQuality.DEFAULT;

        public int biomeBlendRadius = 2;
        public float entityDistanceScaling = 1.0F;

        public boolean enableVignette = true;
        public boolean enableClouds = true;

        public LightingQuality smoothLighting = LightingQuality.HIGH;
    }

    public static class NotificationSettings {
        public boolean hideDonationButton = false;
    }

    public enum GraphicsQuality implements FormattedTextProvider {
        DEFAULT(new TextComponentTranslation("generator.default")),
        FANCY(new TextComponentTranslation("options.clouds.fancy")),
        FAST(new TextComponentTranslation("options.clouds.fast"));

        private final ITextComponent name;

        GraphicsQuality(ITextComponent name) {
            this.name = name;
        }

        @Override
        public ITextComponent getLocalizedName() {
            return this.name;
        }

        public boolean isFancy(boolean fancy) {
            return (this == FANCY) || (this == DEFAULT && fancy);
        }
    }

    public enum LightingQuality implements FormattedTextProvider {
        OFF(new TextComponentTranslation("options.ao.off")),
        LOW(new TextComponentTranslation("options.ao.min")),
        HIGH(new TextComponentTranslation("options.ao.max"));

        private final ITextComponent name;

        LightingQuality(ITextComponent name) {
            this.name = name;
        }

        @Override
        public ITextComponent getLocalizedName() {
            return this.name;
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    public static SodiumGameOptions load(Path path) {
        SodiumGameOptions config;
        boolean resaveConfig = true;

        if (Files.exists(path)) {
            try (FileReader reader = new FileReader(path.toFile())) {
                config = GSON.fromJson(reader, SodiumGameOptions.class);
            } catch (IOException e) {
                throw new RuntimeException("Could not parse config", e);
            } catch (JsonSyntaxException e) {
                SodiumClientMod.logger().error("Could not parse config, will fallback to default settings", e);
                config = new SodiumGameOptions();
                resaveConfig = false;
            }
        } else {
            config = new SodiumGameOptions();
        }

        if (config == null || config.advanced == null || config.quality == null
                || config.performance == null || config.notifications == null) {
            config = new SodiumGameOptions();
        }
        config.sanitize();
        config.configPath = path;

        try {
            if(resaveConfig)
                config.writeChanges();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't update config file", e);
        }

        return config;
    }

    private void sanitize() {
        if (this.quality.cloudQuality == null) this.quality.cloudQuality = GraphicsQuality.DEFAULT;
        if (this.quality.weatherQuality == null) this.quality.weatherQuality = GraphicsQuality.DEFAULT;
        if (this.quality.leavesQuality == null) this.quality.leavesQuality = GraphicsQuality.DEFAULT;
        if (this.quality.smoothLighting == null) this.quality.smoothLighting = LightingQuality.HIGH;
        if (this.performance.chunkBuilderThreads < 0) this.performance.chunkBuilderThreads = 0;
        // Saved upstream settings must not enable paths that change vanilla's rendered output.
        this.advanced.useEntityCulling = false;
        this.advanced.useChunkMultidraw = false;
        this.advanced.useCompactVertexFormat = false;
        this.advanced.useBlockFaceCulling = false;
        this.advanced.useFogOcclusion = false;
        this.advanced.translucencySorting = true;
    }

    public void writeChanges() throws IOException {
        this.sanitize();
        Path dir = this.configPath.getParent();

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        } else if (!Files.isDirectory(dir)) {
            throw new IOException("Not a directory: " + dir);
        }

        Files.write(this.configPath, GSON.toJson(this)
                .getBytes(StandardCharsets.UTF_8));
    }
}

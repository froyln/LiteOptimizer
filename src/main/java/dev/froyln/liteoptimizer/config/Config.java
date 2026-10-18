package dev.froyln.liteoptimizer.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Flat {@code liteoptimizer.json}: one boolean per {@link Option}. Missing key = default, malformed
 * file = all defaults. The file is rewritten with every known key (plus a {@code _help} block) so
 * users can see what exists. Loaded once, by {@link MixinPlugin} before any mixin is applied; must
 * not touch Minecraft classes (it runs inside the tweaker).
 */
public final class Config {
    public static final Logger LOG = LogManager.getLogger("LiteOptimizer");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Option, Boolean> VALUES = new EnumMap<Option, Boolean>(Option.class);

    private Config() {
    }

    public static boolean isEnabled(Option option) {
        Boolean v = VALUES.get(option);
        return v == null ? option.defaultValue : v;
    }

    /** True iff the option and its pillar (if any) are both enabled. */
    public static boolean isActive(Option option) {
        return isEnabled(option) && (option.pillar == null || isEnabled(option.pillar));
    }

    public static void load(File file) {
        VALUES.clear();
        boolean rewrite = true;
        if (file.isFile()) {
            try {
                Reader reader = new FileReader(file);
                try {
                    JsonElement root = new JsonParser().parse(reader);
                    if (!root.isJsonObject()) {
                        throw new IllegalStateException("root is not an object");
                    }
                    rewrite = read(root.getAsJsonObject());
                } finally {
                    reader.close();
                }
            } catch (Exception e) {
                LOG.warn("[LiteOptimizer] {} is not readable ({}); using defaults", file, e.toString());
                VALUES.clear();
            }
        } else {
            LOG.info("[LiteOptimizer] {} not found; writing defaults", file);
        }
        if (rewrite) {
            write(file);
        }
    }

    /** Returns true when the file lacks a known key (so it should be rewritten with all keys). */
    private static boolean read(JsonObject object) {
        boolean missing = false;
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("_")) {
                continue;
            }
            Option option = Option.byKey(key);
            if (option == null) {
                LOG.warn("[LiteOptimizer] unknown config key '{}' ignored", key);
            } else if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isBoolean()) {
                LOG.warn("[LiteOptimizer] config key '{}' is not a boolean; using default {}", key, option.defaultValue);
            } else {
                boolean enabled = entry.getValue().getAsBoolean();
                if (enabled && !option.defaultValue) {
                    LOG.warn("[LiteOptimizer] '{}' is not release-validated; forcing it off", key);
                    VALUES.put(option, false);
                    missing = true;
                } else {
                    VALUES.put(option, enabled);
                }
            }
        }
        for (Option option : Option.values()) {
            if (!object.has(option.key)) {
                missing = true;
            }
        }
        return missing;
    }

    private static void write(File file) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        Map<String, String> help = new LinkedHashMap<String, String>();
        for (Option option : Option.values()) {
            out.put(option.key, isEnabled(option));
            help.put(option.key, (option.pillar == null ? "[pillar] " : "[" + option.pillar.key + "] ") + option.description);
        }
        out.put("_help", help);
        try {
            file.getParentFile().mkdirs();
            Writer writer = new FileWriter(file);
            try {
                GSON.toJson(out, writer);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            LOG.warn("[LiteOptimizer] could not write {}: {}", file, e.toString());
        }
    }

    /** Self-check: {@code java -cp <classes>:<gson>:<log4j> dev.froyln.liteoptimizer.config.Config}. */
    public static void main(String[] args) throws IOException {
        File f = File.createTempFile("liteoptimizer", ".json");
        f.delete();
        load(f);                                            // missing → defaults, file written
        assert f.isFile() : "defaults not written";
        assert isEnabled(Option.WORLD) && isActive(Option.HOPPER_ENTITY_LOOKUP);
        assert !isActive(Option.ITEM_MERGING) : "unmeasured optimization must default off";
        Writer w = new FileWriter(f);
        w.write("{\"world\": false, \"item_merging\": true, \"bogus\": 1, \"render\": \"no\"}");
        w.close();
        load(f);
        assert !isEnabled(Option.WORLD) : "world should be off";
        assert !isEnabled(Option.ITEM_MERGING) && !isActive(Option.ITEM_MERGING)
                : "unvalidated option must remain disabled";
        String migrated = new String(java.nio.file.Files.readAllBytes(f.toPath()), "UTF-8");
        assert !migrated.contains("\"item_merging\": true") : "legacy true must be rewritten off";
        assert isEnabled(Option.RENDER) : "non-boolean must fall back to default";
        w = new FileWriter(f);
        w.write("not json at all");
        w.close();
        load(f);
        assert isEnabled(Option.WORLD) : "malformed must reset to defaults";
        assert !isActive(Option.ITEM_MERGING) : "malformed config must reset to release defaults";
        f.delete();
        System.out.println("Config self-check OK");
    }
}

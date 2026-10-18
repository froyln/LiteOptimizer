#!/usr/bin/env python3
"""Check real Sodium JSON loading/saving without starting Minecraft or OpenGL."""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parent.parent
java = Path('/usr/lib/jvm/java-8-openjdk/bin')
gson = next(p for p in (Path.home() / '.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson/2.8.0').glob('*/*.jar') if not p.name.endswith('-sources.jar'))
# Only Minecraft text/logging boundaries are stubbed; config code and Gson are real.
stubs = {
    'net/minecraft/util/text/ITextComponent.java': 'package net.minecraft.util.text; public interface ITextComponent {}',
    'net/minecraft/util/text/TextComponentTranslation.java': 'package net.minecraft.util.text; public class TextComponentTranslation implements ITextComponent { public TextComponentTranslation(String key) {} }',
    'me/jellysquid/mods/sodium/client/SodiumClientMod.java': 'package me.jellysquid.mods.sodium.client; public class SodiumClientMod { public static Log logger() { return new Log(); } public static class Log { public void error(String text, Throwable error) {} } }',
    'SodiumOptionsCheck.java': r'''
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
public class SodiumOptionsCheck {
    private static void safe(SodiumGameOptions c) {
        assert c.quality.cloudQuality != null;
        assert c.quality.weatherQuality != null;
        assert c.quality.leavesQuality != null;
        assert c.quality.smoothLighting != null;
        assert c.performance.chunkBuilderThreads >= 0;
        assert !c.advanced.useEntityCulling;
        assert !c.advanced.useChunkMultidraw;
        assert !c.advanced.useCompactVertexFormat;
        assert !c.advanced.useBlockFaceCulling;
        assert !c.advanced.useFogOcclusion;
        assert c.advanced.translucencySorting;
    }
    public static void main(String[] args) throws Exception {
        Path p = Paths.get(args[0]).resolve("options.json");
        safe(new SodiumGameOptions());
        safe(SodiumGameOptions.load(p));
        String old = "{\"advanced\":{\"use_chunk_multidraw\":true,\"use_compact_vertex_format\":true,\"use_block_face_culling\":true,\"use_fog_occlusion\":true,\"translucency_sorting\":false,\"use_entity_culling\":true},\"quality\":{\"enable_clouds\":false},\"performance\":{\"chunk_builder_threads\":3}}";
        Files.write(p, old.getBytes(StandardCharsets.UTF_8));
        SodiumGameOptions c = SodiumGameOptions.load(p);
        safe(c);
        assert !c.quality.enableClouds && !c.advanced.useEntityCulling;
        assert c.performance.chunkBuilderThreads == 3;
        safe(SodiumGameOptions.load(p));
        c.advanced.useEntityCulling = true;
        c.advanced.useChunkMultidraw = true;
        c.advanced.useCompactVertexFormat = true;
        c.advanced.useBlockFaceCulling = true;
        c.advanced.useFogOcclusion = true;
        c.advanced.translucencySorting = false;
        c.writeChanges();
        safe(c);
        SodiumGameOptions saved = SodiumGameOptions.load(p);
        safe(saved);
        assert !saved.quality.enableClouds && !saved.advanced.useEntityCulling;
        assert saved.performance.chunkBuilderThreads == 3;
        for (String invalid : new String[]{"null", "{\"advanced\":null}", "{\"quality\":null}", "{\"performance\":null}", "{\"notifications\":null}", "{",
                "{\"quality\":{\"cloud_quality\":\"INVALID\"}}", "{\"quality\":{\"weather_quality\":null}}",
                "{\"quality\":{\"leaves_quality\":\"INVALID\"}}", "{\"quality\":{\"smooth_lighting\":null}}",
                "{\"performance\":{\"chunk_builder_threads\":-1}}"}) {
            Files.write(p, invalid.getBytes(StandardCharsets.UTF_8));
            safe(SodiumGameOptions.load(p));
        }
        System.out.println("Sodium options check passed");
    }
}
''',
}
with tempfile.TemporaryDirectory(prefix='sodium-options-check-') as d:
    scratch = Path(d)
    sources = []
    for name, content in stubs.items():
        path = scratch / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content)
        sources.append(str(path))
    for name in ('gui/SodiumGameOptions.java', 'gui/options/FormattedTextProvider.java'):
        sources.append(str(root / 'src/main/java/me/jellysquid/mods/sodium/client' / name))
    subprocess.run([str(java / 'javac'), '-cp', str(gson), '-d', d, *sources], check=True)
    subprocess.run([str(java / 'java'), '-ea', '-cp', d + ':' + str(gson), 'SodiumOptionsCheck', d], check=True)

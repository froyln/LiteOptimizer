#!/usr/bin/env python3
"""Check optional Tweakeroo setting lookup with and without Tweakeroo present."""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parent.parent
java = Path('/usr/lib/jvm/java-8-openjdk/bin')
cache = Path.home() / '.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-api/2.8.1'
log4j = next(cache.glob('*/*.jar'))
log4j_core = next((Path.home() / '.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-core/2.8.1').glob('*/*.jar'))
helper = root / 'src/main/java/dev/froyln/liteoptimizer/render/TweakerooCompat.java'
sources = {
    'fi/dy/masa/tweakeroo/config/FeatureToggle.java': '''package fi.dy.masa.tweakeroo.config;
public enum FeatureToggle {
    TWEAK_CHUNK_RENDER_MAIN_THREAD;
    public boolean enabled;
    public boolean getBooleanValue() { return this.enabled; }
}''',
    'BridgeCheck.java': '''import dev.froyln.liteoptimizer.render.TweakerooCompat;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
public class BridgeCheck {
    public static void main(String[] args) {
        if (TweakerooCompat.isFreeCameraSpectator()) throw new AssertionError("spectator default");
        TweakerooCompat.setFreeCameraSpectator(true);
        if (!TweakerooCompat.isFreeCameraSpectator()) throw new AssertionError("spectator enable");
        TweakerooCompat.setFreeCameraSpectator(false);
        if (TweakerooCompat.isFreeCameraSpectator()) throw new AssertionError("spectator disable");
        if (TweakerooCompat.forceChunkBuildsOnMainThread()) throw new AssertionError("default");
        FeatureToggle.TWEAK_CHUNK_RENDER_MAIN_THREAD.enabled = true;
        if (!TweakerooCompat.forceChunkBuildsOnMainThread()) throw new AssertionError("enable");
        FeatureToggle.TWEAK_CHUNK_RENDER_MAIN_THREAD.enabled = false;
        if (TweakerooCompat.forceChunkBuildsOnMainThread()) throw new AssertionError("disable");
    }
}''',
    'BridgeAbsentCheck.java': '''import dev.froyln.liteoptimizer.render.TweakerooCompat;
public class BridgeAbsentCheck {
    public static void main(String[] args) {
        if (TweakerooCompat.isFreeCameraSpectator()) throw new AssertionError("spectator absent");
        if (TweakerooCompat.forceChunkBuildsOnMainThread()) throw new AssertionError("absent");
    }
}''',
}

with tempfile.TemporaryDirectory(prefix='tweakeroo-bridge-') as temp:
    scratch = Path(temp)
    source_paths = []
    for name, content in sources.items():
        path = scratch / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content)
        source_paths.append(str(path))
    cp = str(log4j) + ':' + str(log4j_core)
    subprocess.run([str(java / 'javac'), '-cp', cp, '-d', temp, str(helper), *source_paths], check=True)
    with_cp = temp + ':' + cp
    subprocess.run([str(java / 'java'), '-Dorg.apache.logging.log4j.simplelog.StatusLogger.level=OFF',
                    '-cp', with_cp, 'BridgeCheck'], check=True)
    absent_dir = scratch / 'absent'
    absent_dir.mkdir()
    subprocess.run([str(java / 'javac'), '-cp', cp, '-d', str(absent_dir), str(helper),
                    str(scratch / 'BridgeAbsentCheck.java')], check=True)
    without_cp = str(absent_dir) + ':' + cp
    subprocess.run([str(java / 'java'), '-Dorg.apache.logging.log4j.simplelog.StatusLogger.level=OFF',
                    '-cp', without_cp, 'BridgeAbsentCheck'], check=True)

print('Tweakeroo bridge present/absent checks passed')

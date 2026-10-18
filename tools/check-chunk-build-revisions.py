#!/usr/bin/env python3
"""Exercise the real Sodium upload filter against stale and current build results."""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parent.parent
java = Path('/usr/lib/jvm/java-8-openjdk/bin')
fastutil = next((Path.home() / '.gradle/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/7.1.0').glob('*/*.jar'))
source = (root / 'src/main/java/me/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder.java').read_text()


def method(signature):
    start = source.index(signature)
    brace = source.index('{', start)
    depth, end = 1, brace + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


filter_method = method('public Iterator<ChunkBuildResult<T>> filterChunkBuilds(')
harness = '''
import java.util.*;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;

class ChunkGraphicsState {}
class ChunkRenderContainer<T extends ChunkGraphicsState> {
    long revision;
    long getBuildRevision() { return revision; }
}
class ChunkBuildResult<T extends ChunkGraphicsState> {
    final ChunkRenderContainer<T> render;
    final long revision;
    final Object[] passesToUpload;
    ChunkBuildResult(ChunkRenderContainer<T> render, long revision, int passes) {
        this.render = render; this.revision = revision; this.passesToUpload = new Object[passes];
    }
}
class RevisionFilterCheck<T extends ChunkGraphicsState> {
FILTER_METHOD
    public static void main(String[] args) {
        RevisionFilterCheck<ChunkGraphicsState> check = new RevisionFilterCheck<>();
        ChunkRenderContainer<ChunkGraphicsState> render = new ChunkRenderContainer<>();
        render.revision = 2;
        ChunkBuildResult<ChunkGraphicsState> stale = new ChunkBuildResult<>(render, 1, 4);
        ChunkBuildResult<ChunkGraphicsState> current = new ChunkBuildResult<>(render, 2, 1);
        List<ChunkBuildResult<ChunkGraphicsState>> results = Arrays.asList(current, stale);
        Iterator<ChunkBuildResult<ChunkGraphicsState>> filtered = check.filterChunkBuilds(results.iterator());
        if (!filtered.hasNext() || filtered.next() != current || filtered.hasNext())
            throw new AssertionError("late stale upload replaced the current section build");
        render.revision = 3;
        if (check.filterChunkBuilds(Collections.singletonList(current).iterator()).hasNext())
            throw new AssertionError("deleted/rebuilt section accepted an old result");
        System.out.println("chunk build revision filter check passed");
    }
}
'''.replace('FILTER_METHOD', filter_method)

with tempfile.TemporaryDirectory(prefix='chunk-build-revisions-') as temp:
    java_file = Path(temp) / 'RevisionFilterCheck.java'
    java_file.write_text(harness)
    subprocess.run([str(java / 'javac'), '-cp', str(fastutil), '-d', temp, str(java_file)], check=True)
    subprocess.run([str(java / 'java'), '-cp', temp + ':' + str(fastutil), 'RevisionFilterCheck'], check=True)

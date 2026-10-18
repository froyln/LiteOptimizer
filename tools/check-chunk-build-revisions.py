#!/usr/bin/env python3
"""Check upload revisions and prevent transparency sorts from discarding block edits."""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parent.parent
java = Path('/usr/lib/jvm/java-8-openjdk/bin')
fastutil = next((Path.home() / '.gradle/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/7.1.0').glob('*/*.jar'))
source = (root / 'src/main/java/me/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuilder.java').read_text()


def method(signature, text=source):
    start = text.index(signature)
    brace = text.index('{', start)
    depth, end = 1, brace + 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


filter_method = method('public Iterator<ChunkBuildResult<T>> filterChunkBuilds(')
container = (root / 'src/main/java/me/jellysquid/mods/sodium/client/render/chunk/ChunkRenderContainer.java').read_text()
container_methods = '\n'.join(method(signature, container) for signature in (
    'public long cancelRebuildTask(', 'public long getBuildRevision(',
    'public void setData(', 'public boolean scheduleRebuild(', 'public boolean scheduleSort('))
if 'public boolean hasPendingBuild(' in container:
    container_methods += '\n' + method('public boolean hasPendingBuild(', container)
sort_methods = '\n'.join(method(signature) for signature in (
    'public CompletableFuture<ChunkBuildResult<T>> scheduleSortTaskAsync(',
    'private ChunkRenderBuildTask<T> createSortTask('))
harness = '''
import java.util.*;
import java.util.concurrent.CompletableFuture;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;

class ChunkGraphicsState {}
class ChunkRenderData {
    final String shulker;
    ChunkRenderData(String shulker) { this.shulker = shulker; }
    List<Object> getAnimatedSprites() { return Collections.emptyList(); }
}
class WorldRenderer {
    void onChunkRenderUpdated(int x, int y, int z, ChunkRenderData oldData, ChunkRenderData newData) {}
}
class ChunkRenderContainer<T extends ChunkGraphicsState> {
    long buildRevision, dataRevision;
    boolean needsRebuild, needsImportantRebuild, needsSort, tickable;
    int chunkX, chunkY, chunkZ;
    CompletableFuture<Void> rebuildTask;
    WorldRenderer worldRenderer = new WorldRenderer();
    ChunkRenderData data;
    boolean needsRebuild() { return needsRebuild; }
    Object getRenderOrigin() { return null; }
CONTAINER_METHODS
}
class ChunkRenderBuildTask<T extends ChunkGraphicsState> {}
class ChunkRenderTranslucencySortTask<T extends ChunkGraphicsState> extends ChunkRenderBuildTask<T> {
    ChunkRenderTranslucencySortTask(ChunkRenderContainer<T> render, long revision, Object origin, Object camera) {}
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
    Object cameraPosition;
    int scheduledSorts;
    CompletableFuture<ChunkBuildResult<T>> schedule(ChunkRenderBuildTask<T> task) {
        scheduledSorts++;
        return CompletableFuture.completedFuture(null);
    }
SORT_METHODS
    public static void main(String[] args) {
        RevisionFilterCheck<ChunkGraphicsState> check = new RevisionFilterCheck<>();
        ChunkRenderContainer<ChunkGraphicsState> render = new ChunkRenderContainer<>();
        render.buildRevision = 2;
        ChunkBuildResult<ChunkGraphicsState> stale = new ChunkBuildResult<>(render, 1, 4);
        ChunkBuildResult<ChunkGraphicsState> current = new ChunkBuildResult<>(render, 2, 1);
        List<ChunkBuildResult<ChunkGraphicsState>> results = Arrays.asList(current, stale);
        Iterator<ChunkBuildResult<ChunkGraphicsState>> filtered = check.filterChunkBuilds(results.iterator());
        if (!filtered.hasNext() || filtered.next() != current || filtered.hasNext())
            throw new AssertionError("late stale upload replaced the current section build");
        render.buildRevision = 3;
        if (check.filterChunkBuilds(Collections.singletonList(current).iterator()).hasNext())
            throw new AssertionError("deleted/rebuilt section accepted an old result");
        // Check both placement and removal: a camera sort must not discard the updated TE list.
        for (boolean placed : new boolean[] {true, false}) {
            render = new ChunkRenderContainer<>();
            render.setData(new ChunkRenderData(placed ? null : "old shulker"));
            render.scheduleRebuild(true);
            long rebuild = render.cancelRebuildTask(); // rebuild submitted; upload not applied yet
            current = new ChunkBuildResult<>(render, rebuild, 4);
            if (render.scheduleSort(false))
                throw new AssertionError("camera sort accepted while block-entity rebuild awaits upload");
            int scheduled = check.scheduledSorts;
            check.scheduleSortTaskAsync(render); // stale queue entry must also be harmless
            if (render.getBuildRevision() != rebuild || check.scheduledSorts != scheduled)
                throw new AssertionError("sort cancelled a pending block-entity rebuild");
            filtered = check.filterChunkBuilds(Collections.singletonList(current).iterator());
            if (!filtered.hasNext() || filtered.next() != current)
                throw new AssertionError("placed/removed shulker rebuild was discarded");
            render.setData(new ChunkRenderData(placed ? "new shulker" : null));
            if (placed != (render.data.shulker != null))
                throw new AssertionError("incorrect block-entity list after upload");
            if (!render.scheduleSort(false))
                throw new AssertionError("sort did not resume after rebuild upload");
            check.scheduleSortTaskAsync(render);
            if (check.scheduledSorts != scheduled + 1)
                throw new AssertionError("ready section did not sort");
            // A sort queued before a later block edit must leave that rebuild request intact.
            render.setData(render.data);
            render.scheduleSort(false);
            render.scheduleRebuild(true);
            rebuild = render.getBuildRevision();
            scheduled = check.scheduledSorts;
            check.scheduleSortTaskAsync(render);
            if (!render.needsRebuild() || render.getBuildRevision() != rebuild || check.scheduledSorts != scheduled)
                throw new AssertionError("stale sort queue cleared a newer block edit");
        }
        System.out.println("chunk build revision and shulker update checks passed");
    }
}
'''.replace('FILTER_METHOD', filter_method).replace('CONTAINER_METHODS', container_methods).replace('SORT_METHODS', sort_methods)

with tempfile.TemporaryDirectory(prefix='chunk-build-revisions-') as temp:
    java_file = Path(temp) / 'RevisionFilterCheck.java'
    java_file.write_text(harness)
    subprocess.run([str(java / 'javac'), '-cp', str(fastutil), '-d', temp, str(java_file)], check=True)
    subprocess.run([str(java / 'java'), '-cp', temp + ':' + str(fastutil), 'RevisionFilterCheck'], check=True)

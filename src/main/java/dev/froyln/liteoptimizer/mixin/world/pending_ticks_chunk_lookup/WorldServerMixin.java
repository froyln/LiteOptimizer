package dev.froyln.liteoptimizer.mixin.world.pending_ticks_chunk_lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Catalog row world/pending_ticks_chunk_lookup (audit C2).
 *
 * getPendingBlockUpdates walks the whole scheduled-tick TreeSet for every chunk that is saved
 * (all dirty chunks on every autosave) or unloaded. This keeps a per-chunk index of the same
 * entries — maintained at the three places vanilla adds to / removes from the tree — and answers
 * the query from the 3×3 chunk neighbourhood the bounding box can touch.
 *
 * Parity: the returned list is the same entries in the same order (tree order = the entries'
 * natural order, restored with a sort; then the this-tick list in list order, scanned as vanilla
 * does). remove == true removes exactly what vanilla removes. The tree/hash set are untouched.
 */
@Mixin(WorldServer.class)
public abstract class WorldServerMixin {
    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    @Shadow @Final private List<NextTickListEntry> pendingTickListEntriesThisTick;

    private final Long2ObjectOpenHashMap<ArrayList<NextTickListEntry>> liteoptimizer$byChunk = new Long2ObjectOpenHashMap<ArrayList<NextTickListEntry>>();

    private static long liteoptimizer$key(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }

    @Redirect(method = {"updateBlockTick", "scheduleBlockUpdate"}, at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;add(Ljava/lang/Object;)Z"))
    private boolean liteoptimizer$addIndexed(TreeSet<NextTickListEntry> tree, Object o) {
        NextTickListEntry entry = (NextTickListEntry) o;
        if (tree.add(entry)) {
            long key = liteoptimizer$key(entry.position);
            ArrayList<NextTickListEntry> bucket = this.liteoptimizer$byChunk.get(key);
            if (bucket == null) {
                this.liteoptimizer$byChunk.put(key, bucket = new ArrayList<NextTickListEntry>());
            }
            bucket.add(entry);
            return true;
        }
        return false;
    }

    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;remove(Ljava/lang/Object;)Z"))
    private boolean liteoptimizer$removeIndexed(TreeSet<NextTickListEntry> tree, Object o) {
        NextTickListEntry entry = (NextTickListEntry) o;
        if (tree.remove(entry)) {
            liteoptimizer$unindex(entry);
            return true;
        }
        return false;
    }

    private void liteoptimizer$unindex(NextTickListEntry entry) {
        long key = liteoptimizer$key(entry.position);
        ArrayList<NextTickListEntry> bucket = this.liteoptimizer$byChunk.get(key);
        if (bucket != null) {
            // entries at the front of the bucket are the oldest, which is what tickUpdates removes
            for (int i = 0, n = bucket.size(); i < n; i++) {
                if (bucket.get(i) == entry) {
                    bucket.remove(i);
                    break;
                }
            }
            if (bucket.isEmpty()) {
                this.liteoptimizer$byChunk.remove(key);
            }
        }
    }

    @Inject(method = "getPendingBlockUpdates(Lnet/minecraft/world/gen/structure/StructureBoundingBox;Z)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$lookupByChunk(StructureBoundingBox box, boolean remove, CallbackInfoReturnable<List<NextTickListEntry>> cir) {
        List<NextTickListEntry> list = null;
        for (int cx = box.minX >> 4; cx <= (box.maxX - 1) >> 4; cx++) {
            for (int cz = box.minZ >> 4; cz <= (box.maxZ - 1) >> 4; cz++) {
                ArrayList<NextTickListEntry> bucket = this.liteoptimizer$byChunk.get(ChunkPos.asLong(cx, cz));
                if (bucket == null) {
                    continue;
                }
                for (int i = 0, n = bucket.size(); i < n; i++) {
                    NextTickListEntry entry = bucket.get(i);
                    BlockPos pos = entry.position;
                    if (pos.getX() >= box.minX && pos.getX() < box.maxX && pos.getZ() >= box.minZ && pos.getZ() < box.maxZ) {
                        if (list == null) {
                            list = new ArrayList<NextTickListEntry>();
                        }
                        list.add(entry);
                    }
                }
            }
        }
        if (list != null) {
            Collections.sort(list);
            if (remove) {
                for (NextTickListEntry entry : list) {
                    this.pendingTickListEntriesHashSet.remove(entry);
                    this.pendingTickListEntriesTreeSet.remove(entry);
                    liteoptimizer$unindex(entry);
                }
            }
        }
        for (int i = 0; i < this.pendingTickListEntriesThisTick.size(); i++) {
            NextTickListEntry entry = this.pendingTickListEntriesThisTick.get(i);
            BlockPos pos = entry.position;
            if (pos.getX() >= box.minX && pos.getX() < box.maxX && pos.getZ() >= box.minZ && pos.getZ() < box.maxZ) {
                if (remove) {
                    this.pendingTickListEntriesThisTick.remove(i--);
                }
                if (list == null) {
                    list = new ArrayList<NextTickListEntry>();
                }
                list.add(entry);
            }
        }
        cir.setReturnValue(list);
    }
}

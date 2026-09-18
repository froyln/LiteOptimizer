package dev.froyln.liteoptimizer.mixin.world.structure_lookup_index;

import java.util.ArrayList;
import java.util.Iterator;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Catalog row world/structure_lookup_index (found by the B4 profile: 23 % of server time).
 *
 * Every monster spawn attempt in the overworld asks the scattered-feature generator
 * "is this position inside a swamp hut?" (ChunkGeneratorOverworld.getPossibleCreatures), and
 * ocean-monument / nether-fortress checks work the same way. MapGenStructure answers by
 * iterating its entire structureMap — every structure start of that type ever generated or
 * loaded in the world — and testing each bounding box. Witch farms make thousands of spawn
 * attempts per tick, so this dominated their tick time.
 *
 * The starts are additionally indexed by every chunk their bounding box covers, at the two
 * places vanilla puts them into structureMap; a query then tests only the starts registered
 * for the position's chunk. Bounding boxes are final after a start is stored (they are computed
 * in the Start constructors; generateStructure never widens them).
 *
 * Parity: getStructureAt and isPositionInStructure return a start / true iff some start passes
 * the same tests as vanilla; a start can only pass if its box contains the position, which
 * means it is registered for that chunk, so the candidate set is exactly the set of possible
 * matches. Both callers only use the result as a boolean or check its own type/components
 * (isSwampHut), and starts of one generator type cannot overlap, so which match is found
 * first cannot matter. getNearestStructurePos (/locate) is untouched.
 */
@Mixin(MapGenStructure.class)
public abstract class MapGenStructureMixin {
    @Shadow protected abstract void initializeStructureData(World worldIn);

    private final Long2ObjectOpenHashMap<ArrayList<StructureStart>> liteoptimizer$byChunk = new Long2ObjectOpenHashMap<ArrayList<StructureStart>>();

    @Redirect(method = {"recursiveGenerate", "initializeStructureData"}, at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;put(JLjava/lang/Object;)Ljava/lang/Object;", remap = false))
    private Object liteoptimizer$putIndexed(Long2ObjectMap<StructureStart> map, long key, Object value) {
        StructureStart start = (StructureStart) value;
        Object previous = map.put(key, start);
        if (previous != null) {
            liteoptimizer$unindex((StructureStart) previous);
        }
        StructureBoundingBox box = start.getBoundingBox();
        if (box != null) {
            for (int cx = box.minX >> 4; cx <= box.maxX >> 4; cx++) {
                for (int cz = box.minZ >> 4; cz <= box.maxZ >> 4; cz++) {
                    long k = ChunkPos.asLong(cx, cz);
                    ArrayList<StructureStart> bucket = this.liteoptimizer$byChunk.get(k);
                    if (bucket == null) {
                        this.liteoptimizer$byChunk.put(k, bucket = new ArrayList<StructureStart>(2));
                    }
                    bucket.add(start);
                }
            }
        }
        return previous;
    }

    private void liteoptimizer$unindex(StructureStart start) {
        for (Iterator<ArrayList<StructureStart>> it = this.liteoptimizer$byChunk.values().iterator(); it.hasNext();) {
            ArrayList<StructureStart> bucket = it.next();
            bucket.remove(start);
            if (bucket.isEmpty()) {
                it.remove();
            }
        }
    }

    @Inject(method = "getStructureAt", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$structureAt(BlockPos pos, CallbackInfoReturnable<StructureStart> cir) {
        ArrayList<StructureStart> bucket = this.liteoptimizer$byChunk.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if (bucket != null) {
            for (int i = 0, n = bucket.size(); i < n; i++) {
                StructureStart start = bucket.get(i);
                if (start.isSizeableStructure() && start.getBoundingBox().isVecInside(pos)) {
                    for (StructureComponent component : start.getComponents()) {
                        if (component.getBoundingBox().isVecInside(pos)) {
                            cir.setReturnValue(start);
                            return;
                        }
                    }
                }
            }
        }
        cir.setReturnValue(null);
    }

    @Inject(method = "isPositionInStructure", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$positionInStructure(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.initializeStructureData(world);
        ArrayList<StructureStart> bucket = this.liteoptimizer$byChunk.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if (bucket != null) {
            for (int i = 0, n = bucket.size(); i < n; i++) {
                StructureStart start = bucket.get(i);
                if (start.isSizeableStructure() && start.getBoundingBox().isVecInside(pos)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
        cir.setReturnValue(false);
    }
}

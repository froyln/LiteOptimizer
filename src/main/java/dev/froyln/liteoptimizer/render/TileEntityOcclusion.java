package dev.froyln.liteoptimizer.render;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Helper for render/tile_entity_occlusion. EntityCulling's corner-raycast test can miss a TE
 * seen through a gap none of its 8 corner rays cross (a false cull that changes pixels — see
 * the catalog row). Sodium's chunk-section visibility graph does not have that failure mode:
 * a section is visible only through a connected chain of open faces from the camera section
 * (RenderGlobal.setupTerrain's BFS, pixel-parity-checked at 0.3.0), so any camera ray that
 * could reach a point in an invisible section is already excluded by construction — no raycast
 * needed, just a lookup into that frame's graph. Same multi-section box test the ported
 * SodiumWorldRenderer.isEntityVisible already uses for entities, but keyed off the TE's block
 * box instead of a render bounding box: the 3x3x3 box (tile_entity_frustum's box — covers every
 * vanilla TESR's max draw extent) can touch up to 8 sections near a boundary, and the TE is
 * culled only when every section it touches is invisible, so a TE that straddles a culled and a
 * visible section is never culled.
 */
public final class TileEntityOcclusion {
    private static boolean insideRenderEntities;

    private TileEntityOcclusion() {}

    public static void enter() {
        insideRenderEntities = true;
    }

    public static void exit() {
        insideRenderEntities = false;
    }

    /** true = every section the TE's draw box touches is occluded; the caller skips it. */
    public static boolean isOccluded(TileEntity te) {
        if (!insideRenderEntities) {
            return false;
        }
        SodiumWorldRenderer renderer = SodiumWorldRenderer.getInstanceNullable();
        if (renderer == null) {
            return false;
        }
        BlockPos pos = te.getPos();
        int minX = (pos.getX() - 1) >> 4, maxX = (pos.getX() + 1) >> 4;
        int minY = (pos.getY() - 1) >> 4, maxY = (pos.getY() + 1) >> 4;
        int minZ = (pos.getZ() - 1) >> 4, maxZ = (pos.getZ() + 1) >> 4;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (renderer.isChunkVisible(x, y, z)) {
                        return false;
                    }
                }
            }
        }
        TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer(te);
        return tesr != null && !tesr.isGlobalRenderer(te);
    }
}

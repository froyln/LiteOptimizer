package dev.froyln.liteoptimizer.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Helper for render/tile_entity_frustum. Vanilla renders every block entity of every visible
 * render chunk (RenderGlobal.renderEntities "blockentities"); the only test per TE is the
 * distance check in TileEntityRendererDispatcher.render(TileEntity, float, int). A TE whose
 * draw lies entirely outside the frustum produces no pixels, so it is skipped. The box tested
 * is the 3×3×3 block cube around the TE — wider than any vanilla TESR draws relative to its
 * block (double chest +2 along one axis, banner 1.75 up, shulker lid, piston arm, enchantment
 * table book). Global renderers (beacon, end gateway, structure block — vanilla's own
 * isGlobalRenderer) are never culled. The camera is the same Frustum the terrain was culled
 * with, valid only while RenderGlobal.renderEntities runs; outside it nothing is culled.
 */
public final class TileEntityFrustum {
    private static Frustum camera;

    private TileEntityFrustum() {}

    public static void enter(ICamera cam) {
        camera = cam instanceof Frustum ? (Frustum) cam : null;
    }

    public static void exit() {
        camera = null;
    }

    /** true = the TE's draw cannot touch the frustum; the caller skips it. */
    public static boolean isOutside(TileEntity te) {
        Frustum cam = camera;
        if (cam == null) {
            return false;
        }
        BlockPos pos = te.getPos();
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if (cam.isBoxInFrustum(x - 1, y - 1, z - 1, x + 2, y + 2, z + 2)) {
            return false;
        }
        TileEntitySpecialRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(te);
        return renderer != null && !renderer.isGlobalRenderer(te);
    }
}

package dev.froyln.liteoptimizer.render;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public final class TileEntityFrustum {
    private static Frustum camera;

    private TileEntityFrustum() {}

    public static void enter(ICamera cam) {
        camera = cam instanceof Frustum ? (Frustum) cam : null;
    }

    public static void exit() {
        camera = null;
    }

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

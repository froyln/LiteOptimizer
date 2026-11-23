package dev.froyln.liteoptimizer.mixin.render.sodium.features.chunk_rendering;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockShulkerBox.class)
public abstract class MixinBlockShulkerBox {
    @Inject(method = "getBlockFaceShape", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$useClosedShapeForMissingTileEntity(IBlockAccess world, IBlockState state,
            BlockPos pos, EnumFacing face, CallbackInfoReturnable<BlockFaceShape> cir) {
        if (world instanceof WorldSlice && world.getTileEntity(pos) == null) {
            cir.setReturnValue(BlockFaceShape.SOLID);
        }
    }
}

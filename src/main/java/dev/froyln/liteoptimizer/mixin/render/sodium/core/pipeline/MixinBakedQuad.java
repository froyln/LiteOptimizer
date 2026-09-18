package dev.froyln.liteoptimizer.mixin.render.sodium.core.pipeline;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * LiteOptimizer: vanilla 1.12.2 BakedQuads always use DefaultVertexFormats.BLOCK — 7 ints per
 * vertex: position (0..2), colour (3), texture (4..5), lightmap (6). No Forge vertex-format
 * variants exist here, so the offsets are constants.
 */
@Mixin(BakedQuad.class)
public class MixinBakedQuad implements ModelQuadView {
    private static final int STRIDE = 7;

    @Shadow @Final protected TextureAtlasSprite sprite;
    @Shadow @Final protected int tintIndex;
    @Shadow @Final protected int[] vertexData;

    private int cachedFlags;

    @Inject(method = "<init>([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V", at = @At("RETURN"))
    private void init(int[] vertexData, int colorIndex, EnumFacing face, TextureAtlasSprite sprite, CallbackInfo ci) {
        this.cachedFlags = ModelQuadFlags.getQuadFlags((BakedQuad) (Object) this);
    }

    @Override
    public float getX(int idx) {
        return Float.intBitsToFloat(this.vertexData[idx * STRIDE]);
    }

    @Override
    public float getY(int idx) {
        return Float.intBitsToFloat(this.vertexData[idx * STRIDE + 1]);
    }

    @Override
    public float getZ(int idx) {
        return Float.intBitsToFloat(this.vertexData[idx * STRIDE + 2]);
    }

    @Override
    public int getColor(int idx) {
        return this.vertexData[idx * STRIDE + 3];
    }

    @Override
    public TextureAtlasSprite rubidium$getSprite() {
        return this.sprite;
    }

    @Override
    public float getTexU(int idx) {
        return Float.intBitsToFloat(this.vertexData[idx * STRIDE + 4]);
    }

    @Override
    public float getTexV(int idx) {
        return Float.intBitsToFloat(this.vertexData[idx * STRIDE + 5]);
    }

    @Override
    public int getFlags() {
        return this.cachedFlags;
    }

    @Override
    public int getNormal(int idx) {
        return 0;
    }

    @Override
    public int getColorIndex() {
        return this.tintIndex;
    }
}

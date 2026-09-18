package me.jellysquid.mods.sodium.client.render.pipeline;

import me.jellysquid.mods.sodium.client.model.light.LightPipelineProvider;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuad;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadViewMutable;
import me.jellysquid.mods.sodium.client.model.quad.blender.BiomeColorBlender;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.format.ModelVertexSink;
import me.jellysquid.mods.sodium.client.util.Norm3b;
import me.jellysquid.mods.sodium.client.util.color.ColorABGR;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

/**
 * LiteOptimizer: vanilla 1.12.2's {@code BlockFluidRenderer.renderFluid}, step for step, writing
 * into Sodium's chunk buffers instead of a BufferBuilder. Every height, UV, colour multiplier,
 * shading factor and lightmap value is computed exactly as vanilla computes it (same expressions,
 * same evaluation order), so the emitted vertices are the same numbers vanilla would emit; only the
 * container they land in differs. Sodium's own (1.16-derived) fluid renderer was replaced because
 * its corner-height and occlusion rules differ from 1.12's and would change pixels.
 */
public class FluidRenderer {
    private final ModelQuadViewMutable quad = new ModelQuad();
    private final int[] quadColors = new int[4];
    private final int[] quadLight = new int[4];

    private final BlockColors blockColors;
    private final TextureAtlasSprite[] atlasSpritesLava = new TextureAtlasSprite[2];
    private final TextureAtlasSprite[] atlasSpritesWater = new TextureAtlasSprite[2];
    private final TextureAtlasSprite atlasSpriteWaterOverlay;

    public FluidRenderer(Minecraft client, LightPipelineProvider lighters, BiomeColorBlender biomeColorBlender) {
        int normal = Norm3b.pack(0.0f, 1.0f, 0.0f);
        for (int i = 0; i < 4; i++) {
            this.quad.setNormal(i, normal);
        }
        this.blockColors = client.getBlockColors();
        TextureMap texturemap = client.getTextureMapBlocks();
        this.atlasSpritesLava[0] = texturemap.getAtlasSprite("minecraft:blocks/lava_still");
        this.atlasSpritesLava[1] = texturemap.getAtlasSprite("minecraft:blocks/lava_flow");
        this.atlasSpritesWater[0] = texturemap.getAtlasSprite("minecraft:blocks/water_still");
        this.atlasSpritesWater[1] = texturemap.getAtlasSprite("minecraft:blocks/water_flow");
        this.atlasSpriteWaterOverlay = texturemap.getAtlasSprite("minecraft:blocks/water_overlay");
    }

    // vanilla BufferBuilder.pos: (float)(((double)blockPos + d) + xOffset), xOffset = -(double)renderOrigin
    private double absX, absY, absZ;
    private int originX, originY, originZ;

    private float fx(double d) {
        return (float) ((this.absX + d) - this.originX);
    }

    private float fy(double d) {
        return (float) ((this.absY + d) - this.originY);
    }

    private float fz(double d) {
        return (float) ((this.absZ + d) - this.originZ);
    }

    /**
     * Vanilla BlockFluidRenderer.renderFluid, writing into Sodium's buffers. Positions are computed
     * exactly as vanilla does — absolute doubles rounded once to a float relative to the render
     * origin — so the chunk offset must be zero when this is called.
     */
    public boolean render(IBlockAccess blockAccess, IBlockState blockStateIn, BlockPos blockPosIn, ChunkModelBuffers buffers, BlockPos renderOrigin) {
        this.absX = blockPosIn.getX();
        this.absY = blockPosIn.getY();
        this.absZ = blockPosIn.getZ();
        this.originX = renderOrigin.getX();
        this.originY = renderOrigin.getY();
        this.originZ = renderOrigin.getZ();

        BlockLiquid blockliquid = (BlockLiquid) blockStateIn.getBlock();
        boolean flag = blockStateIn.getMaterial() == Material.LAVA;
        TextureAtlasSprite[] atextureatlassprite = flag ? this.atlasSpritesLava : this.atlasSpritesWater;
        int i = this.blockColors.colorMultiplier(blockStateIn, blockAccess, blockPosIn, 0);
        float f = (float) (i >> 16 & 255) / 255.0F;
        float f1 = (float) (i >> 8 & 255) / 255.0F;
        float f2 = (float) (i & 255) / 255.0F;
        boolean flag1 = blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.UP);
        boolean flag2 = blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.DOWN);
        boolean[] aboolean = new boolean[] {
                blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.NORTH),
                blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.SOUTH),
                blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.WEST),
                blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.EAST)};

        if (!flag1 && !flag2 && !aboolean[0] && !aboolean[1] && !aboolean[2] && !aboolean[3]) {
            return false;
        }

        boolean flag3 = false;
        Material material = blockStateIn.getMaterial();
        float f7 = this.getFluidHeight(blockAccess, blockPosIn, material);
        float f8 = this.getFluidHeight(blockAccess, blockPosIn.south(), material);
        float f9 = this.getFluidHeight(blockAccess, blockPosIn.east().south(), material);
        float f10 = this.getFluidHeight(blockAccess, blockPosIn.east(), material);
        final ModelQuadViewMutable quad = this.quad;
        quad.setFlags(0);

        if (flag1) {
            flag3 = true;
            float f12 = BlockLiquid.getSlopeAngle(blockAccess, blockPosIn, material, blockStateIn);
            TextureAtlasSprite textureatlassprite = f12 > -999.0F ? atextureatlassprite[1] : atextureatlassprite[0];
            f7 -= 0.001F;
            f8 -= 0.001F;
            f9 -= 0.001F;
            f10 -= 0.001F;
            float f13;
            float f14;
            float f15;
            float f16;
            float f17;
            float f18;
            float f19;
            float f20;

            if (f12 < -999.0F) {
                f13 = textureatlassprite.getInterpolatedU(0.0D);
                f17 = textureatlassprite.getInterpolatedV(0.0D);
                f14 = f13;
                f18 = textureatlassprite.getInterpolatedV(16.0D);
                f15 = textureatlassprite.getInterpolatedU(16.0D);
                f19 = f18;
                f16 = f15;
                f20 = f17;
            } else {
                float f21 = MathHelper.sin(f12) * 0.25F;
                float f22 = MathHelper.cos(f12) * 0.25F;
                f13 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f22 - f21) * 16.0F));
                f17 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f22 + f21) * 16.0F));
                f14 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f22 + f21) * 16.0F));
                f18 = textureatlassprite.getInterpolatedV((double) (8.0F + (f22 + f21) * 16.0F));
                f15 = textureatlassprite.getInterpolatedU((double) (8.0F + (f22 + f21) * 16.0F));
                f19 = textureatlassprite.getInterpolatedV((double) (8.0F + (f22 - f21) * 16.0F));
                f16 = textureatlassprite.getInterpolatedU((double) (8.0F + (f22 - f21) * 16.0F));
                f20 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f22 - f21) * 16.0F));
            }

            int k2 = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn);
            int color = ColorABGR.pack(1.0F * f, 1.0F * f1, 1.0F * f2, 1.0F);

            quad.setSprite(textureatlassprite);
            this.setVertex(quad, 0, this.fx(0.0D), this.fy(f7), this.fz(0.0D), f13, f17);
            this.setVertex(quad, 1, this.fx(0.0D), this.fy(f8), this.fz(1.0D), f14, f18);
            this.setVertex(quad, 2, this.fx(1.0D), this.fy(f9), this.fz(1.0D), f15, f19);
            this.setVertex(quad, 3, this.fx(1.0D), this.fy(f10), this.fz(0.0D), f16, f20);
            this.flushQuad(buffers, quad, ModelQuadFacing.UP, color, k2);

            if (blockliquid.shouldRenderSides(blockAccess, blockPosIn.up())) {
                this.setVertex(quad, 0, this.fx(0.0D), this.fy(f7), this.fz(0.0D), f13, f17);
                this.setVertex(quad, 1, this.fx(1.0D), this.fy(f10), this.fz(0.0D), f16, f20);
                this.setVertex(quad, 2, this.fx(1.0D), this.fy(f9), this.fz(1.0D), f15, f19);
                this.setVertex(quad, 3, this.fx(0.0D), this.fy(f8), this.fz(1.0D), f14, f18);
                this.flushQuad(buffers, quad, ModelQuadFacing.DOWN, color, k2);
            }
        }

        if (flag2) {
            float f35 = atextureatlassprite[0].getMinU();
            float f36 = atextureatlassprite[0].getMaxU();
            float f37 = atextureatlassprite[0].getMinV();
            float f38 = atextureatlassprite[0].getMaxV();
            int l1 = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down());
            int color = ColorABGR.pack(0.5F, 0.5F, 0.5F, 1.0F);

            quad.setSprite(atextureatlassprite[0]);
            this.setVertex(quad, 0, this.fx(0.0D), this.fy(0.0D), this.fz(1.0D), f35, f38);
            this.setVertex(quad, 1, this.fx(0.0D), this.fy(0.0D), this.fz(0.0D), f35, f37);
            this.setVertex(quad, 2, this.fx(1.0D), this.fy(0.0D), this.fz(0.0D), f36, f37);
            this.setVertex(quad, 3, this.fx(1.0D), this.fy(0.0D), this.fz(1.0D), f36, f38);
            this.flushQuad(buffers, quad, ModelQuadFacing.DOWN, color, l1);
            flag3 = true;
        }

        for (int i1 = 0; i1 < 4; ++i1) {
            int j1 = 0;
            int k1 = 0;

            if (i1 == 0) {
                --k1;
            }
            if (i1 == 1) {
                ++k1;
            }
            if (i1 == 2) {
                --j1;
            }
            if (i1 == 3) {
                ++j1;
            }

            BlockPos blockpos = blockPosIn.add(j1, 0, k1);
            TextureAtlasSprite textureatlassprite1 = atextureatlassprite[1];

            if (!flag) {
                Block block = blockAccess.getBlockState(blockpos).getBlock();
                if (block == Blocks.GLASS || block == Blocks.STAINED_GLASS) {
                    textureatlassprite1 = this.atlasSpriteWaterOverlay;
                }
            }

            if (aboolean[i1]) {
                float f39;
                float f40;
                double d3;
                double d4;
                double d5;
                double d6;

                if (i1 == 0) {
                    f39 = f7;
                    f40 = f10;
                    d3 = 0.0D;
                    d5 = 1.0D;
                    d4 = 0.0010000000474974513D;
                    d6 = 0.0010000000474974513D;
                } else if (i1 == 1) {
                    f39 = f9;
                    f40 = f8;
                    d3 = 1.0D;
                    d5 = 0.0D;
                    d4 = 1.0D - 0.0010000000474974513D;
                    d6 = 1.0D - 0.0010000000474974513D;
                } else if (i1 == 2) {
                    f39 = f8;
                    f40 = f7;
                    d3 = 0.0010000000474974513D;
                    d5 = 0.0010000000474974513D;
                    d4 = 1.0D;
                    d6 = 0.0D;
                } else {
                    f39 = f10;
                    f40 = f9;
                    d3 = 1.0D - 0.0010000000474974513D;
                    d5 = 1.0D - 0.0010000000474974513D;
                    d4 = 0.0D;
                    d6 = 1.0D;
                }

                flag3 = true;
                float f41 = textureatlassprite1.getInterpolatedU(0.0D);
                float f27 = textureatlassprite1.getInterpolatedU(8.0D);
                float f28 = textureatlassprite1.getInterpolatedV((double) ((1.0F - f39) * 16.0F * 0.5F));
                float f29 = textureatlassprite1.getInterpolatedV((double) ((1.0F - f40) * 16.0F * 0.5F));
                float f30 = textureatlassprite1.getInterpolatedV(8.0D);
                int j = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos);
                float f31 = i1 < 2 ? 0.8F : 0.6F;
                int color = ColorABGR.pack(1.0F * f31 * f, 1.0F * f31 * f1, 1.0F * f31 * f2, 1.0F);
                ModelQuadFacing facing = i1 == 0 ? ModelQuadFacing.NORTH : i1 == 1 ? ModelQuadFacing.SOUTH : i1 == 2 ? ModelQuadFacing.WEST : ModelQuadFacing.EAST;

                quad.setSprite(textureatlassprite1);
                this.setVertex(quad, 0, this.fx(d3), this.fy(f39), this.fz(d4), f41, f28);
                this.setVertex(quad, 1, this.fx(d5), this.fy(f40), this.fz(d6), f27, f29);
                this.setVertex(quad, 2, this.fx(d5), this.fy(0.0D), this.fz(d6), f27, f30);
                this.setVertex(quad, 3, this.fx(d3), this.fy(0.0D), this.fz(d4), f41, f30);
                this.flushQuad(buffers, quad, facing, color, j);

                if (textureatlassprite1 != this.atlasSpriteWaterOverlay) {
                    this.setVertex(quad, 0, this.fx(d3), this.fy(0.0D), this.fz(d4), f41, f30);
                    this.setVertex(quad, 1, this.fx(d5), this.fy(0.0D), this.fz(d6), f27, f30);
                    this.setVertex(quad, 2, this.fx(d5), this.fy(f40), this.fz(d6), f27, f29);
                    this.setVertex(quad, 3, this.fx(d3), this.fy(f39), this.fz(d4), f41, f28);
                    this.flushQuad(buffers, quad, facing.getOpposite(), color, j);
                }
            }
        }

        return flag3;
    }

    /** Vanilla BlockFluidRenderer.getFluidHeight, verbatim. */
    private float getFluidHeight(IBlockAccess blockAccess, BlockPos blockPosIn, Material blockMaterial) {
        int i = 0;
        float f = 0.0F;

        for (int j = 0; j < 4; ++j) {
            BlockPos blockpos = blockPosIn.add(-(j & 1), 0, -(j >> 1 & 1));

            if (blockAccess.getBlockState(blockpos.up()).getMaterial() == blockMaterial) {
                return 1.0F;
            }

            IBlockState iblockstate = blockAccess.getBlockState(blockpos);
            Material material = iblockstate.getMaterial();

            if (material != blockMaterial) {
                if (!material.isSolid()) {
                    ++f;
                    ++i;
                }
            } else {
                int k = ((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue();

                if (k >= 8 || k == 0) {
                    f += BlockLiquid.getLiquidHeightPercent(k) * 10.0F;
                    i += 10;
                }

                f += BlockLiquid.getLiquidHeightPercent(k);
                ++i;
            }
        }

        return 1.0F - f / (float) i;
    }

    private void flushQuad(ChunkModelBuffers buffers, ModelQuadView quad, ModelQuadFacing facing, int color, int light) {
        ModelVertexSink sink = buffers.getSink(facing);
        sink.ensureCapacity(4);

        for (int i = 0; i < 4; i++) {
            sink.writeQuad(quad.getX(i), quad.getY(i), quad.getZ(i), color, quad.getTexU(i), quad.getTexV(i), light);
        }

        TextureAtlasSprite sprite = quad.rubidium$getSprite();
        if (sprite != null) {
            buffers.getRenderData().addSprite(sprite);
        }

        sink.flush();
    }

    private void setVertex(ModelQuadViewMutable quad, int i, float x, float y, float z, float u, float v) {
        quad.setX(i, x);
        quad.setY(i, y);
        quad.setZ(i, z);
        quad.setTexU(i, u);
        quad.setTexV(i, v);
    }
}

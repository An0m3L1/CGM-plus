package com.mrcrayfish.guns.client.render.crosshair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Author: MrCrayfish
 */
public class TechCrosshair extends Crosshair
{
    private static final ResourceLocation TECH_CROSSHAIR = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/tech.png");
    private static final ResourceLocation DOT_CROSSHAIR = new ResourceLocation(Reference.MOD_ID, "textures/crosshair/dot.png");

    private float scale;
    private float prevScale;
    private float rotation;
    private float prevRotation;

    public TechCrosshair()
    {
        super(new ResourceLocation(Reference.MOD_ID, "tech"));
    }

    @Override
    public void tick()
    {
        this.prevRotation = this.rotation;
        this.prevScale = this.scale;
        this.rotation += 4;
        this.scale *= 0.7F;
    }

    @Override
    public void onGunFired()
    {
        this.prevScale = 1.0F;
        this.scale = 1.5F;
    }

    @Override
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks)
    {
        float alpha = 1.0F;// - (float) AimingHandler.get().getNormalisedAdsProgress();
        float size = 8.0F;

        RenderSystem.enableBlend();
        boolean doBlend = Config.CLIENT.blendCrosshair.get();
        if (doBlend)
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        stack.pushPose();
        {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, DOT_CROSSHAIR);
            Matrix4f matrix = stack.last().pose();
            stack.translate(Math.round((windowWidth) / 2F)-0.5, Math.round((windowHeight) / 2F)-0.5, 0);
            stack.translate(-size / 2F, -size / 2F, 0);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, size, 0).uv(0, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, size, size, 0).uv(1, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, size, 0, 0).uv(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        stack.pushPose();
        {
            Matrix4f matrix = stack.last().pose();
            stack.translate(Math.round((windowWidth) / 2F)-0.5, Math.round((windowHeight) / 2F)-0.5, 0);
            float scale = 1F + Mth.lerp(partialTicks, this.prevScale, this.scale);
            stack.scale(scale, scale, scale);
            stack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, this.prevRotation, this.rotation)));
            stack.translate(-size / 2F, -size / 2F, 0);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, TECH_CROSSHAIR);
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            buffer.vertex(matrix, 0, size, 0).uv(0, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, size, size, 0).uv(1, 1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, size, 0, 0).uv(1, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            buffer.vertex(matrix, 0, 0, 0).uv(0, 0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            BufferUploader.drawWithShader(buffer.end());
        }
        stack.popPose();

        if (doBlend)
        RenderSystem.defaultBlendFunc();
    }
}

package com.mrcrayfish.guns.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.entity.grenade.ThrowableGrenadeEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class ThrowableGrenadeRenderer extends EntityRenderer<ThrowableGrenadeEntity>
{
    public ThrowableGrenadeRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation(ThrowableGrenadeEntity entity)
    {
        return null;
    }

    @Override
    public void render(ThrowableGrenadeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light)
    {
        poseStack.pushPose();

        poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(entityYaw));

        /* Offsets to the center of the grenade before applying rotation */
        float rotation = entity.prevRotation + (entity.rotation - entity.prevRotation) * partialTicks;
        rotation *= 0.25F;
        poseStack.translate(0, entity.getDimensions(Pose.STANDING).height * 0.5, 0);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-rotation));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(-rotation));
        poseStack.translate(0,   -(entity.getDimensions(Pose.STANDING).height * 0.5),0);
        poseStack.translate(0, entity.getDimensions(Pose.STANDING).height * 0.5, 0);

        Minecraft.getInstance().getItemRenderer().renderStatic(entity.getItem(), ItemTransforms.TransformType.NONE, light, OverlayTexture.NO_OVERLAY, poseStack, renderTypeBuffer, 0);

        poseStack.popPose();
    }
}

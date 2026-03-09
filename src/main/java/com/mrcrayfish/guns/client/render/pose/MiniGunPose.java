package com.mrcrayfish.guns.client.render.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.common.GripType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
public class MiniGunPose extends WeaponPose
{
    @Override
    protected AimPose getUpPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(10F, 0F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-100F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-150F).setRotationAngleY(40F).setRotationAngleZ(-10F).setRotationPointY(1));
        return pose;
    }

    @Override
    protected AimPose getForwardPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setRightArm(new LimbPose().setRotationAngleX(-15F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-45F).setRotationAngleY(30F).setRotationAngleZ(0F).setRotationPointY(2));
        return pose;
    }

    @Override
    protected AimPose getDownPose()
    {
        AimPose pose = new AimPose();
        pose.getIdle().setRenderYawOffset(45F).setItemRotation(new Vector3f(-50F, 0F, 0F)).setItemTranslate(new Vector3f(0F, 0F, 1F)).setRightArm(new LimbPose().setRotationAngleX(0F).setRotationAngleY(-45F).setRotationAngleZ(0F).setRotationPointY(1)).setLeftArm(new LimbPose().setRotationAngleX(-25F).setRotationAngleY(30F).setRotationAngleZ(15F).setRotationPointY(4));
        return pose;
    }

    @Override
    protected boolean hasAimPose()
    {
        return false;
    }

    @Override
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress)
    {
        super.applyPlayerModelRotation(player, rightArm, leftArm, head, hand, aimProgress);
    }

    @Override
    public void applyPlayerPreRender(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        super.applyPlayerPreRender(player, hand, aimProgress, poseStack, buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        super.applyHeldItemTransforms(player, hand, aimProgress, poseStack, buffer);

    }

    @Override
    public boolean applyOffhandTransforms(Player player, PlayerModel model, ItemStack stack, PoseStack poseStack, float partialTicks)
    {
        return GripType.applyBackTransforms(player, poseStack);
    }

    @Override
    public boolean canApplySprintingAnimation()
    {
        return false;
    }
}

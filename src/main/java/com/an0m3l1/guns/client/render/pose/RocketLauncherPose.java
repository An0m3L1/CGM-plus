package com.an0m3l1.guns.client.render.pose;

import com.an0m3l1.guns.common.GripType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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
public class RocketLauncherPose extends WeaponPose
{
	@Override
	protected AimPose getUpPose()
	{
		AimPose pose = new AimPose();
		pose.getIdle().setRenderYawOffset(35F).setItemRotation(new Vector3f(10F, 0F, 0F)).setRightArm(new LimbPose().setRotationAngleX(-170F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointY(4).setRotationPointZ(-2)).setLeftArm(new LimbPose().setRotationAngleX(-130F).setRotationAngleY(65F).setRotationAngleZ(0F).setRotationPointX(3).setRotationPointY(2).setRotationPointZ(1));
		return pose;
	}
	
	@Override
	protected AimPose getForwardPose()
	{
		AimPose pose = new AimPose();
		pose.getIdle().setRenderYawOffset(35F).setRightArm(new LimbPose().setRotationAngleX(-90F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointY(2).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-91F).setRotationAngleY(35F).setRotationAngleZ(0F).setRotationPointX(4).setRotationPointY(2).setRotationPointZ(0));
		return pose;
	}
	
	@Override
	protected AimPose getDownPose()
	{
		AimPose pose = new AimPose();
		pose.getIdle().setRenderYawOffset(35F).setRightArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointY(2).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(15F).setRotationAngleZ(30F).setRotationPointX(4).setRotationPointY(2).setRotationPointZ(0));
		return pose;
	}
	
	@Override
	protected boolean hasAimPose()
	{
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
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

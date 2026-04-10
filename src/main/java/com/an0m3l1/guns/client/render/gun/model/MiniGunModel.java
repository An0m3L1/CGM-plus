package com.an0m3l1.guns.client.render.gun.model;

import com.an0m3l1.guns.client.GunModel;
import com.an0m3l1.guns.client.SpecialModels;
import com.an0m3l1.guns.client.render.gun.IOverrideModel;
import com.an0m3l1.guns.client.util.RenderUtil;
import com.an0m3l1.guns.init.ModSyncedDataKeys;
import com.an0m3l1.guns.util.GunCompositeStatHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
public class MiniGunModel implements IOverrideModel
{
	private final WeakHashMap<LivingEntity, Rotations> rotationMap = new WeakHashMap<>();
	
	float rotationSpeed = 0F;
	
	@Override
	public void tick(Player player)
	{
		this.rotationMap.putIfAbsent(player, new Rotations());
		Rotations rotations = this.rotationMap.get(player);
		rotations.prevRotation = rotations.rotation;
		
		ItemStack heldItem = player.getMainHandItem();
		boolean shooting = ModSyncedDataKeys.SHOOTING.getValue(player);
		boolean reloading = ModSyncedDataKeys.RELOADING.getValue(player);
		
		int fireRate = GunCompositeStatHelper.getCompositeRate(heldItem, player);
		float maxSpinRate = Mth.clamp(90F / (float) fireRate, 10F, 85F);
		
		if(shooting && !reloading)
		{
			rotationSpeed = maxSpinRate;
		}
		else
		{
			rotationSpeed *= 0.8F;
		}
		
		if(rotationSpeed < 1F)
		{
			rotationSpeed = 1F;
		}
		
		if(rotationSpeed > 0F)
		{
			rotations.rotation += (int) rotationSpeed;
		}
	}
	
	@Override
	public void render(float partialTicks, ItemTransforms.TransformType transformType, ItemStack stack, ItemStack parent,
	                   @Nullable
	                   LivingEntity entity, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, int overlay)
	{
		Rotations rotations = entity != null ? this.rotationMap.computeIfAbsent(entity, uuid -> new Rotations()) : Rotations.ZERO;
		Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, renderTypeBuffer, light, overlay, GunModel.wrap(SpecialModels.MINI_GUN_BASE.getModel()));
		poseStack.pushPose();
		boolean correctContext = (transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
		if(correctContext)
		{
			RenderUtil.rotateZ(poseStack, 0.0F, -0.375F, rotations.prevRotation + (rotations.rotation - rotations.prevRotation) * partialTicks);
		}
		Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, renderTypeBuffer, light, overlay, GunModel.wrap(SpecialModels.MINI_GUN_BARRELS.getModel()));
		poseStack.popPose();
	}
	
	private static class Rotations
	{
		private static final Rotations ZERO = new Rotations();
		
		private int rotation;
		private int prevRotation;
	}
	
	@SubscribeEvent
	public void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
	{
		this.rotationMap.clear();
	}
}

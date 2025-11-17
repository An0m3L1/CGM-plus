package com.mrcrayfish.guns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.GunModel;
import com.mrcrayfish.guns.client.SpecialModels;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.attachment.impl.IAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 * Modified by zaeonNineZero for Nine Zeros Gun Expansion
 * Attachment detection logic based off of code from Mo Guns by Bomb787 and AlanorMiga (MigaMi)
 */
public class SniperRifleModel implements IOverrideModel
{
	private boolean disableAnimations = false;

	@Override
	public void render(float partialTicks, ItemTransforms.TransformType transformType, ItemStack stack, ItemStack parent, @Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
	{
		BakedModel bakedModel = SpecialModels.SNIPER_RIFLE_BASE.getModel();
		Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, buffer, light, overlay, GunModel.wrap(bakedModel));

		ItemStack attachmentStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
		if(attachmentStack.isEmpty())
		{
			RenderUtil.renderModel(SpecialModels.SNIPER_RIFLE_SIGHTS.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		}

		boolean isPlayer = entity != null && entity.equals(Minecraft.getInstance().player);
		boolean isFirstPerson = (transformType.firstPerson());
		boolean correctContext = (transformType.firstPerson() || transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
		boolean useFallbackAnimation = false;

		Vec3 boltTranslations = Vec3.ZERO;
		Vec3 boltRotations = Vec3.ZERO;
		Vec3 boltRotOffset = new Vec3(0, -4.15, 0);

		Vec3 magTranslations = Vec3.ZERO;
		Vec3 magRotations = Vec3.ZERO;
		Vec3 magRotOffset = Vec3.ZERO;

		if(isPlayer && correctContext && !disableAnimations)
		{
			try {
				Player player = (Player) entity;

				boltTranslations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "bolt");
				boltRotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "bolt");

				magTranslations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "magazine");
				magRotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "magazine");
				magRotOffset = GunAnimationHelper.getSmartAnimationRotOffset(stack, player, partialTicks, "magazine");

				if(!GunAnimationHelper.hasAnimation("fire", stack) && GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks).equals("fire"))
					useFallbackAnimation = true;
			}
			catch(NoClassDefFoundError ignored) {
				disableAnimations = true;
			}
			catch(Exception e) {
				GunMod.LOGGER.error("Redundant Guns encountered an error trying to apply animations.");
				e.printStackTrace();
				disableAnimations = true;
			}
		}

		if(disableAnimations || useFallbackAnimation)
		{
			if(isPlayer && correctContext)
			{
				float cooldownDivider = 2.9F;
				float cooldownOffset1 = 0.8F;
				float intensity = 1.3F +1;
				float boltLeadTime = 0.4F;

				ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
				float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
				cooldown *= cooldownDivider;
				float cooldown_a = cooldown-cooldownOffset1;

				float cooldown_b = Math.min(Math.max(cooldown_a*intensity,0),1);
				float cooldown_c = Math.min(Math.max((-cooldown_a*intensity)+intensity,0),1);
				float cooldown_d = Math.min(cooldown_b,cooldown_c);

				float cooldown_e = Math.min(Math.max(cooldown_a*intensity+boltLeadTime,0),1);
				float cooldown_f = Math.min(Math.max((-cooldown_a*intensity+boltLeadTime)+intensity,0),1);
				float cooldown_g = Math.min(cooldown_e,cooldown_f);

				boltTranslations = new Vec3(0, 0, (cooldown_d * 2.5));
				boltRotations = new Vec3(0, 0, -(55F * Math.min(cooldown_g*2F,1)));
			}
		}

		poseStack.pushPose();
		if(isPlayer)
		{
			if(boltTranslations!=Vec3.ZERO)
				poseStack.translate(0, 0, boltTranslations.z*0.0625);
			if (!disableAnimations)
			{
				if(boltRotations!=Vec3.ZERO)
					GunAnimationHelper.rotateAroundOffset(poseStack, boltRotations, boltRotOffset);
			}
			else
			{
				poseStack.translate(0, boltRotOffset.y*0.0625, 0);
				poseStack.mulPose(Vector3f.ZN.rotationDegrees((float) boltRotations.z));
				poseStack.translate(0, -boltRotOffset.y*0.0625, 0);
			}
		}
		RenderUtil.renderModel(SpecialModels.SNIPER_RIFLE_BOLT.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		poseStack.popPose();

		poseStack.pushPose();
		if(isPlayer)
			poseStack.translate(0, 0, boltTranslations.z*0.0625);
		RenderUtil.renderModel(SpecialModels.SNIPER_RIFLE_CHAMBER.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		poseStack.popPose();

		// Magazine for Battle Rifle
		poseStack.pushPose();
		// Apply transformations to this part.
		if(isPlayer && isFirstPerson && !disableAnimations)
		{
			if(magTranslations!=Vec3.ZERO)
				poseStack.translate(magTranslations.x*0.0625, magTranslations.y*0.0625, magTranslations.z*0.0625);
			if(magRotations!=Vec3.ZERO)
				GunAnimationHelper.rotateAroundOffset(poseStack, magRotations, magRotOffset);
		}
		// Render the transformed model.
		SpecialModels magModel = SpecialModels.SNIPER_RIFLE_MAG;
		try {
			ItemStack magStack = Gun.getAttachment(IAttachment.Type.byTagKey("Magazine"), stack);
			if(!magStack.isEmpty())
			{
				if (magStack.getItem().builtInRegistryHolder().key().location().getPath().equals("light_magazine"))
					magModel = SpecialModels.SNIPER_RIFLE_LIGHT_MAG;
				if (magStack.getItem().builtInRegistryHolder().key().location().getPath().equals("extended_magazine"))
					magModel = SpecialModels.SNIPER_RIFLE_EXT_MAG;
			}
		}
		catch(Error | Exception ignored) {}

        RenderUtil.renderModel(magModel.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		poseStack.popPose();
	}
}
package com.mrcrayfish.guns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.GunModel;
import com.mrcrayfish.guns.client.SpecialModels;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.attachment.impl.IAttachment;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/** Author: An0m3L1 */
public class AutomaticPistolModel implements IOverrideModel
{
	final Minecraft mc = Minecraft.getInstance();
	private boolean disableAnimations = false;
	private static final Map<Player, Boolean> PREV_BOLT_LOCKED = new HashMap<>();
	private static final Map<Player, Integer> BOLT_LOCK_TIMER = new HashMap<>();
	
	@Override
	public void render(float partialTicks, ItemTransforms.TransformType transformType, ItemStack stack, ItemStack parent,
	                   @Nullable
	                   LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
	{
		// Render the base model
		BakedModel base = SpecialModels.AUTOMATIC_PISTOL_BASE.getModel();
		Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, buffer, light, overlay, GunModel.wrap(base));
		
		// Render the top rail model that appears when a scope is attached
		ItemStack scopeStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
		if(!scopeStack.isEmpty())
		{
			RenderUtil.renderModel(SpecialModels.AUTOMATIC_PISTOL_SCOPE_RAIL.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		}
		
		// Variables for animations
		boolean isPlayer = entity != null && entity.equals(Minecraft.getInstance().player);
		boolean isFirstPerson = transformType.firstPerson();
		boolean correctContext = (transformType.firstPerson() || transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
		
		GunItem gunStack = (GunItem) stack.getItem();
		Gun gun = gunStack.getModifiedGun(stack);
		CompoundTag tag = stack.getTag();
		
		boolean ammoIsEmpty = false;
		boolean ammoIsFull = false;
		if(tag != null)
		{
			int ammoCount = tag.getInt("AmmoCount");
			int ammoCapacity = GunCompositeStatHelper.getAmmoCapacity(stack);
			ammoIsEmpty = ammoCount <= 0;
			ammoIsFull = ammoCount >= ammoCapacity;
		}
		boolean reloading = isPlayer && ModSyncedDataKeys.RELOADING.getValue((Player) entity);
		boolean reloadFromEmpty = isPlayer && ReloadHandler.get().isReloadFromEmpty();

        /* Lock the bolt forward if:
            1. Gun has run out of ammo
            2. Gun is already loaded, but still playing the reload animation (to properly play animations) */
		boolean lockBoltForward = ammoIsEmpty || (reloadFromEmpty && reloading && ammoIsFull);
		
		Vec3 boltTranslations = Vec3.ZERO;
		Vec3 magTranslations = Vec3.ZERO;
		Vec3 magRotations = Vec3.ZERO;
		Vec3 magRotOffset = Vec3.ZERO;
		
		if(isPlayer && correctContext && !disableAnimations)
		{
			try
			{
				Player player = (Player) entity;
				boltTranslations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "bolt");
				magTranslations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "magazine");
				magRotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "magazine");
				magRotOffset = GunAnimationHelper.getSmartAnimationRotOffset(stack, player, partialTicks, "magazine");
			}
			catch(NoClassDefFoundError ignored)
			{
				disableAnimations = true;
			}
			catch(Exception e)
			{
				GunMod.LOGGER.error("NZGE encountered an error trying to apply animations.");
				e.printStackTrace();
				disableAnimations = true;
			}
		}
		
		// Fire animation for bolt is rendered both first and third person
		if(isPlayer && correctContext)
		{
			float cooldownDivider = Math.max((float) gun.getGeneral().getRate() / 3F, 1);
			float cooldownOffset1 = cooldownDivider - 1.0F;
			float intensity = 1.0F + 1;
			
			ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
			float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
			cooldown *= cooldownDivider;
			float cooldown_a = cooldown - cooldownOffset1;
			
			float cooldown_b = Math.min(Math.max(cooldown_a * intensity, 0), 1);
			float cooldown_c = Math.min(Math.max((-cooldown_a * intensity) + intensity, 0), 1);
			float cooldown_d = Math.min(cooldown_b, cooldown_c);
			
			boltTranslations = boltTranslations.add(0, 0, -(cooldown_d * 2));
		}
		
		// Render the bolt model that moves back and forth when firing and is locked forward when gun is emptied
		poseStack.pushPose();
		if(isPlayer)
		{
			double boltZ = boltTranslations.z;
			// Special check to render item in workbench as empty
			if(transformType.equals(ItemTransforms.TransformType.FIXED))
			{
				boltZ = boltZ - 2;
			}
			else
			{
				Player player = (Player) entity;
				boolean prevEmpty = PREV_BOLT_LOCKED.getOrDefault(player, false);
			
				/* Lock bolt movement for 1 second after lockBoltForward switches from true to false.
                This prevents bolt jitter when transitioning from reload animation to base state */
				if(prevEmpty && !lockBoltForward)
				{
					BOLT_LOCK_TIMER.put(player, 20);
				}
				PREV_BOLT_LOCKED.put(player, lockBoltForward);
				
				int transition = BOLT_LOCK_TIMER.getOrDefault(entity, 0);
				if(transition > 0)
				{
					// Lock the bolt
					boltZ = 0.0;
					BOLT_LOCK_TIMER.put(player, transition - 1);
				}
				// Move bolt 2 pixels forward
				else if(lockBoltForward)
				{
					boltZ = boltZ - 2;
				}
			}
			
			poseStack.translate(0, 0, boltZ * 0.0625);
		}
		RenderUtil.renderModel(SpecialModels.AUTOMATIC_PISTOL_BOLT.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		poseStack.popPose();
		
		// Render the magazine model
		poseStack.pushPose();
		if(isPlayer && isFirstPerson && !disableAnimations)
		{
			if(magTranslations != Vec3.ZERO)
			{
				poseStack.translate(magTranslations.x * 0.0625, magTranslations.y * 0.0625, magTranslations.z * 0.0625);
			}
			if(magRotations != Vec3.ZERO)
			{
				GunAnimationHelper.rotateAroundOffset(poseStack, magRotations, magRotOffset);
			}
		}
		SpecialModels magModel = SpecialModels.AUTOMATIC_PISTOL_MAG;
		try
		{
			// Use different magazine models for magazine attachments
			ItemStack magStack = Gun.getAttachment(IAttachment.Type.byTagKey("Magazine"), stack);
			if(!magStack.isEmpty())
			{
				if(magStack.getItem().builtInRegistryHolder().key().location().getPath().equals("light_magazine"))
				{
					magModel = SpecialModels.AUTOMATIC_PISTOL_LIGHT_MAG;
				}
				else if(magStack.getItem().builtInRegistryHolder().key().location().getPath().equals("extended_magazine"))
				{
					magModel = SpecialModels.AUTOMATIC_PISTOL_EXT_MAG;
				}
			}
		}
		catch(Error | Exception ignored)
		{
		}
		
		RenderUtil.renderModel(magModel.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
		poseStack.popPose();
	}
}
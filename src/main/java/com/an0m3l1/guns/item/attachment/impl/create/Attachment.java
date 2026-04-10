package com.an0m3l1.guns.item.attachment.impl.create;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.interfaces.IGunModifier;
import com.an0m3l1.guns.item.attachment.impl.IAttachment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * The base attachment object
 * <p>
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = GunMod.MOD_ID, value = Dist.CLIENT)
public abstract class Attachment
{
	protected final IGunModifier[] modifiers;
	private List<Component> perks = null;
	
	Attachment(IGunModifier... modifiers)
	{
		this.modifiers = modifiers;
	}
	
	public IGunModifier[] getModifiers()
	{
		return this.modifiers;
	}
	
	void setPerks(List<Component> perks)
	{
		if(this.perks == null)
		{
			this.perks = perks;
		}
	}
	
	List<Component> getPerks()
	{
		return this.perks;
	}
	
	/* Determines the perks of attachments and caches them */
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void addInformationEvent(ItemTooltipEvent event)
	{
		ItemStack stack = event.getItemStack();
		if(stack.getItem() instanceof IAttachment<?> attachment)
		{
			List<Component> perks = attachment.getProperties().getPerks();
			if(perks != null && !perks.isEmpty())
			{
				if(Screen.hasControlDown())
				{
					event.getToolTip().add(Component.translatable("info." + GunMod.MOD_ID + ".stats").withStyle(ChatFormatting.GOLD));
					event.getToolTip().addAll(perks);
				}
				else
				{
					event.getToolTip().add(Component.translatable("info." + GunMod.MOD_ID + ".stats_help").withStyle(ChatFormatting.GOLD));
				}
				return;
			}
			
			IGunModifier[] modifiers = attachment.getProperties().getModifiers();
			float thisOutput;
			float thisInput;
			List<Component> positivePerks = new ArrayList<>();
			List<Component> negativePerks = new ArrayList<>();
			List<Component> perkType;
			String perkDescription;
			
			/* Test for fire sound volume */
			float inputSound = 1.0F;
			float outputSound = inputSound;
			for(IGunModifier modifier : modifiers)
			{
				outputSound = modifier.modifyFireSoundVolume(outputSound);
			}
			thisOutput = outputSound;
			thisInput = inputSound;
			if(thisOutput != inputSound)
			{
				perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput < thisInput;
				float modifierValue = toPercent(thisOutput);
				perkDescription = "perk." + GunMod.MOD_ID + ".fire_volume";
				
				addPerk(perkType, isPositive, true, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for silenced */
			for(IGunModifier modifier : modifiers)
			{
				if(modifier.silencedFire())
				{
					addPerk(positivePerks, true, "perk." + GunMod.MOD_ID + ".silenced.positive");
					break;
				}
			}
			
			/* Test for flash size */
			double inputFlash = 10.0;
			double outputFlash = inputFlash;
			for(IGunModifier modifier : modifiers)
			{
				outputFlash = modifier.modifyMuzzleFlashScale(outputFlash);
			}
			thisOutput = (float) outputFlash;
			thisInput = (float) inputFlash;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput < thisInput;
				float modifierValue = toPercent(thisOutput);
				perkDescription = "perk." + GunMod.MOD_ID + ".flash";
				
				addPerk(perkType, isPositive, true, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for Light Magazine */
			for(IGunModifier modifier : modifiers)
			{
				if(modifier.lightMag())
				{
					addPerk(positivePerks, false, "perk." + GunMod.MOD_ID + ".mag");
					addPerk(positivePerks, true, "perk." + GunMod.MOD_ID + ".reload");
					break;
				}
			}
			
			/* Test for Extended Magazine */
			for(IGunModifier modifier : modifiers)
			{
				if(modifier.extMag())
				{
					addPerk(positivePerks, true, "perk." + GunMod.MOD_ID + ".mag");
					addPerk(positivePerks, false, "perk." + GunMod.MOD_ID + ".reload");
					break;
				}
			}
			
			/* Test for sound radius */
			double outputRadius = 10.0;
			for(IGunModifier modifier : modifiers)
			{
				outputRadius = modifier.modifyFireSoundRadius(outputRadius);
			}
			thisOutput = (float) outputRadius;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput < thisInput;
				float modifierValue = toPercent(thisOutput);
				perkDescription = "perk." + GunMod.MOD_ID + ".sound_radius";
				
				addPerk(perkType, isPositive, true, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for additional damage */
			float additionalDamage = 0.0F;
			for(IGunModifier modifier : modifiers)
			{
				additionalDamage += modifier.additionalDamage();
			}
			if(additionalDamage > 0.0F)
			{
				addPerk(positivePerks, true, "perk." + GunMod.MOD_ID + ".additional_damage.positive", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage / 2.0));
			}
			else if(additionalDamage < 0.0F)
			{
				addPerk(negativePerks, false, "perk." + GunMod.MOD_ID + ".additional_damage.negative", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage / 2.0));
			}
			
			/* Test for modified damage */
			float outputDamage = 10.0F;
			for(IGunModifier modifier : modifiers)
			{
				outputDamage = modifier.modifyProjectileDamage(outputDamage);
			}
			thisOutput = outputDamage;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput >= thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput > thisInput;
				float modifierValue = toPercent(thisOutput);
				//perkDescription = (isPositive ? "perk."+GunMod.MOD_ID+".modified_damage.positive" : "perk."+GunMod.MOD_ID+".modified_damage.negative");
				perkDescription = "perk." + GunMod.MOD_ID + ".modified_damage";
				
				addPerk(perkType, isPositive, false, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for modified projectile speed */
			double outputSpeed = 10.0;
			for(IGunModifier modifier : modifiers)
			{
				outputSpeed = modifier.modifyProjectileSpeed(outputSpeed);
			}
			thisOutput = (float) outputSpeed;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput >= thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput > thisInput;
				float modifierValue = toPercent(thisOutput);
				//perkDescription = (isPositive ? "perk."+GunMod.MOD_ID+".projectile_speed.positive" : "perk."+GunMod.MOD_ID+".projectile_speed.negative");
				perkDescription = "perk." + GunMod.MOD_ID + ".projectile_speed";
				
				addPerk(perkType, isPositive, false, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for modified projectile spread */
			float outputSpread = 10.0F;
			for(IGunModifier modifier : modifiers)
			{
				outputSpread = modifier.modifyProjectileSpread(outputSpread);
			}
			thisOutput = outputSpread;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput < thisInput;
				float modifierValue = toPercent(thisOutput);
				//perkDescription = (isPositive ? "perk."+GunMod.MOD_ID+".projectile_spread.positive" : "perk."+GunMod.MOD_ID+".projectile_spread.negative");
				perkDescription = "perk." + GunMod.MOD_ID + ".projectile_spread";
				
				addPerk(perkType, isPositive, true, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for modified projectile life */
			int inputLife = 100;
			int outputLife = inputLife;
			for(IGunModifier modifier : modifiers)
			{
				outputLife = modifier.modifyProjectileLife(outputLife);
			}
			thisOutput = (float) outputLife;
			thisInput = (float) inputLife;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput >= thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput > thisInput;
				float modifierValue = toPercent(thisOutput / 10F);
				//perkDescription = (isPositive ? "perk."+GunMod.MOD_ID+".projectile_life.positive" : "perk."+GunMod.MOD_ID+".projectile_life.negative");
				perkDescription = "perk." + GunMod.MOD_ID + ".projectile_life";
				
				addPerk(perkType, isPositive, false, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for modified recoil */
			float inputRecoil = 10.0F;
			float outputRecoil = inputRecoil;
			for(IGunModifier modifier : modifiers)
			{
				outputRecoil *= modifier.recoilModifier();
			}
			thisOutput = outputRecoil;
			thisInput = inputRecoil;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput < thisInput;
				float modifierValue = toPercent(thisOutput);
				//perkDescription = (isPositive ? "perk."+GunMod.MOD_ID+".recoil.positive" : "perk."+GunMod.MOD_ID+".recoil.negative");
				perkDescription = "perk." + GunMod.MOD_ID + ".recoil";
				
				addPerk(perkType, isPositive, true, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for ADS time */
			double outputAdsTime = 10.0;
			for(IGunModifier modifier : modifiers)
			{
				outputAdsTime = modifier.modifyAimDownSightSpeed(outputAdsTime);
			}
			thisOutput = (float) outputAdsTime;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput > thisInput;
				float modifierValue = toPercent(thisOutput);
				perkDescription = "perk." + GunMod.MOD_ID + ".ads_time";
				
				addPerk(perkType, isPositive, true, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			/* Test for fire rate */
			int outputRate = 10;
			for(IGunModifier modifier : modifiers)
			{
				outputRate = modifier.modifyFireRate(outputRate);
			}
			thisOutput = (float) outputRate;
			if(thisOutput != thisInput)
			{
				perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
				boolean isPositive = thisOutput < thisInput;
				float modifierValue = toPercent(thisOutput);
				//perkDescription = (isPositive ? "perk."+GunMod.MOD_ID+".rate.positive" : "perk."+GunMod.MOD_ID+".rate.negative");
				perkDescription = "perk." + GunMod.MOD_ID + ".rate";
				
				addPerk(perkType, isPositive, true, (float) Math.round(modifierValue * 100) / 100, perkDescription);
			}
			
			positivePerks.addAll(negativePerks);
			attachment.getProperties().setPerks(positivePerks);
			if(!positivePerks.isEmpty())
			{
				event.getToolTip().add(Component.translatable("info." + GunMod.MOD_ID + ".stats").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
				event.getToolTip().addAll(positivePerks);
			}
		}
	}
	
	private static float toPercent(float input)
	{
		return Math.round((input) * 10) - 100;
	}
	
	private static void addPerk(List<Component> components, boolean positive, String id, Object... params)
	{
		components.add(Component.translatable(positive ? "perk." + GunMod.MOD_ID + ".entry.positive" : "perk." + GunMod.MOD_ID + ".entry.negative", Component.translatable(id, params).withStyle(ChatFormatting.GRAY)).withStyle(positive ? ChatFormatting.GREEN : ChatFormatting.RED));
	}
	
	private static void addPerk(List<Component> components, boolean positive, float value, String id, Object... params)
	{
		components.add(Component.translatable(positive ? "perk." + GunMod.MOD_ID + ".entry.positive" : "perk." + GunMod.MOD_ID + ".entry.negative", Component.translatable(id, params).withStyle(ChatFormatting.GRAY).append(Component.literal(" (" + Math.abs(value) + "%)").withStyle(ChatFormatting.GRAY))).withStyle(positive ? ChatFormatting.DARK_AQUA : ChatFormatting.GOLD));
	}
	
	private static void addPerk(List<Component> components, boolean positive, boolean invert, float value, String id, Object... params)
	{
		boolean truePositive = (invert != positive);
		components.add(Component.literal((truePositive ? "+" : "-") + Math.abs(value) + "% ").withStyle(positive ? ChatFormatting.GREEN : ChatFormatting.RED).append(Component.translatable(id, params).withStyle(ChatFormatting.GRAY)));
	}
}

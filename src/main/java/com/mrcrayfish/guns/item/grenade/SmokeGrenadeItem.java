package com.mrcrayfish.guns.item.grenade;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.entity.grenade.ThrowableGrenadeEntity;
import com.mrcrayfish.guns.entity.grenade.ThrowableSmokeGrenadeEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SmokeGrenadeItem extends GrenadeItem
{
	public SmokeGrenadeItem(Properties properties, int maxCookTime, SoundEvent throwSound, SoundEvent pinSound)
	{
		super(properties, maxCookTime, throwSound, pinSound);
	}
	
	@Override
	public void appendHoverText(@NotNull ItemStack stack,
	                            @Nullable
	                            Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		double damage = Config.SERVER.smokeGrenadeDamage.get();
		double smokeDuration = Config.SERVER.smokeGrenadeCloudDuration.get();
		double smokeRadius = Config.SERVER.smokeGrenadeCloudRadius.get();
		float cookTime = (float) maxCookTime / 20;
		if(Screen.hasControlDown())
		{
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stats").withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".damage_tick", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".smoke_duration", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(smokeDuration)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".smoke_radius", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(smokeRadius)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".fuse", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(cookTime)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".extinguish").withStyle(ChatFormatting.GRAY));
		}
		else
		{
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stats_help").withStyle(ChatFormatting.GOLD));
		}
	}
	
	@Override
	public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft)
	{
		return new ThrowableSmokeGrenadeEntity(world, entity, timeLeft);
	}
}

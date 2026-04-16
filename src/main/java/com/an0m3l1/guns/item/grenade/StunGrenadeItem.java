package com.an0m3l1.guns.item.grenade;

import com.an0m3l1.guns.GunConfig;
import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.entity.grenade.ThrowableGrenadeEntity;
import com.an0m3l1.guns.entity.grenade.ThrowableStunGrenadeEntity;
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
public class StunGrenadeItem extends GrenadeItem
{
	public StunGrenadeItem(Properties properties, int maxCookTime, SoundEvent throwSound, SoundEvent pinSound)
	{
		super(properties, maxCookTime, throwSound, pinSound);
	}
	
	@Override
	public void appendHoverText(@NotNull ItemStack stack,
	                            @Nullable
	                            Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		double blindRadius = (GunConfig.SERVER.blindCriteria.radius.get());
		float maxBlindDuration = (float) (GunConfig.SERVER.blindCriteria.durationMax.get());
		double stunRadius = (GunConfig.SERVER.stunCriteria.radius.get());
		float maxStunDuration = (float) (GunConfig.SERVER.stunCriteria.durationMax.get());
		float cookTime = (float) maxCookTime / 20;
		if(Screen.hasControlDown())
		{
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stats").withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".blind_radius", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(blindRadius)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".blind", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxBlindDuration)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stun_radius", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(stunRadius)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stun", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxStunDuration)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".fuse", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(cookTime)).withStyle(ChatFormatting.GRAY));
		}
		else
		{
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stats_help").withStyle(ChatFormatting.GOLD));
		}
	}
	
	@Override
	public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft)
	{
		return new ThrowableStunGrenadeEntity(world, entity, timeLeft);
	}
}

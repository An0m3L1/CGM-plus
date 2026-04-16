package com.an0m3l1.guns.item.grenade;

import com.an0m3l1.guns.GunConfig;
import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.entity.grenade.ThrowableGrenadeEntity;
import com.an0m3l1.guns.entity.grenade.ThrowableImpactGrenadeEntity;
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
public class ImpactGrenadeItem extends GrenadeItem
{
	public ImpactGrenadeItem(Properties properties, int maxCookTime, SoundEvent throwSound, SoundEvent pinSound)
	{
		super(properties, maxCookTime, throwSound, pinSound);
	}
	
	@Override
	public boolean canCook()
	{
		return false;
	}
	
	@Override
	public void appendHoverText(@NotNull ItemStack stack,
	                            @Nullable
	                            Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		double damage = GunConfig.SERVER.impactGrenadeExplosionDamage.get();
		double explosionRadius = (GunConfig.SERVER.impactGrenadeExplosionRadius.get());
		if(Screen.hasControlDown())
		{
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stats").withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".explosion_radius", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(explosionRadius)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".explodes_on_impact").withStyle(ChatFormatting.GRAY));
		}
		else
		{
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stats_help").withStyle(ChatFormatting.GOLD));
		}
	}
	
	@Override
	public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft)
	{
		return new ThrowableImpactGrenadeEntity(world, entity, timeLeft);
	}
}

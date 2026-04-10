package com.an0m3l1.guns.item.grenade;

import com.an0m3l1.guns.Config;
import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.entity.grenade.ThrowableGrenadeEntity;
import com.an0m3l1.guns.init.ModSounds;
import com.an0m3l1.guns.item.IGrenade;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.S2CMessageSound;
import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class GrenadeItem extends Item implements IGrenade
{
	public final int maxCookTime;
	public final SoundEvent throwSound;
	public final SoundEvent pinSound;
	
	public GrenadeItem(Properties properties, int maxCookTime, SoundEvent throwSound, SoundEvent pinSound)
	{
		super(properties.stacksTo(8).tab(GunMod.GUNS));
		this.maxCookTime = maxCookTime;
		this.throwSound = throwSound;
		this.pinSound = pinSound;
	}
	
	@Override
	public void appendHoverText(@NotNull ItemStack stack,
	                            @Nullable
	                            Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
	{
		double damage = Config.SERVER.grenadeExplosionDamage.get();
		double explosionRadius = (Config.SERVER.grenadeExplosionRadius.get());
		float cookTime = (float) maxCookTime / 20;
		if(Screen.hasControlDown())
		{
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stats").withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".explosion_radius", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(explosionRadius)).withStyle(ChatFormatting.GRAY));
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".fuse", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(cookTime)).withStyle(ChatFormatting.GRAY));
		}
		else
		{
			tooltip.add(Component.translatable("info." + GunMod.MOD_ID + ".stats_help").withStyle(ChatFormatting.GOLD));
		}
	}
	
	@Override
	public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack)
	{
		return UseAnim.SPEAR;
	}
	
	@Override
	public int getUseDuration(@NotNull ItemStack stack)
	{
		return this.maxCookTime;
	}
	
	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count)
	{
		int duration = this.getUseDuration(stack) - count;
		
		if(duration == 9 && !player.level.isClientSide)
		{
			ResourceLocation soundId = this.pinSound.getLocation();
			float posX = (float) player.getX();
			float posY = (float) player.getY() + player.getEyeHeight();
			float posZ = (float) player.getZ();
			double radius = Config.SERVER.grenadePinSoundDistance.get();
			
			S2CMessageSound messageSound = new S2CMessageSound(soundId, SoundSource.PLAYERS, posX, posY, posZ, 1.0F, 1.0F, player.getId(), false, false);
			PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, posX, posY, posZ, radius), messageSound);
		}
	}
	
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn)
	{
		ItemStack stack = playerIn.getItemInHand(handIn);
		playerIn.startUsingItem(handIn);
		return InteractionResultHolder.consume(stack);
	}
	
	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving)
	{
		if(this.canCook() && !worldIn.isClientSide())
		{
			if(!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative())
			{
				stack.shrink(1);
			}
			ThrowableGrenadeEntity grenade = this.create(worldIn, entityLiving, 0);
			grenade.onDeath();
			if(entityLiving instanceof Player)
			{
				((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
			}
		}
		return stack;
	}
	
	@Override
	public void releaseUsing(@NotNull ItemStack stack, Level worldIn, @NotNull LivingEntity entityLiving, int timeLeft)
	{
		if(!worldIn.isClientSide())
		{
			int duration = this.getUseDuration(stack) - timeLeft;
			if(duration >= 10)
			{
				if(!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative())
				{
					stack.shrink(1);
				}
				float velocity = 1.15F;
				if(entityLiving.isCrouching())
				{
					velocity *= 0.5f;
				}
				ThrowableGrenadeEntity grenade = this.create(worldIn, entityLiving, this.maxCookTime - duration);
				grenade.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, velocity, 1.0F);
				worldIn.addFreshEntity(grenade);
				this.onThrown(worldIn, grenade);
				if(entityLiving instanceof Player)
				{
					worldIn.gameEvent(GameEvent.ITEM_INTERACT_FINISH, entityLiving.blockPosition(), GameEvent.Context.of(entityLiving));
					((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
				}
			}
		}
	}
	
	public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft)
	{
		return new ThrowableGrenadeEntity(world, entity, timeLeft);
	}
	
	public boolean canCook()
	{
		return true;
	}
	
	protected void onThrown(Level world, ThrowableGrenadeEntity entity)
	{
		if(!world.isClientSide)
		{
			int shooterId = entity.getOwner() != null ? entity.getOwner().getId() : -1;
			float posX = (float) entity.getX();
			float posY = (float) entity.getY();
			float posZ = (float) entity.getZ();
			double radius = Config.SERVER.grenadeThrowSoundDistance.get();
			
			// Generic throw sound
			ResourceLocation throwSoundId = ModSounds.THROW.getId();
			if(throwSoundId != null)
			{
				S2CMessageSound throwMessageSound = new S2CMessageSound(throwSoundId, SoundSource.PLAYERS, posX, posY, posZ, 0.5F, 1.0F, shooterId, false, false);
				PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(world, posX, posY, posZ, radius), throwMessageSound);
			}
			
			// Grenade specific throw sound
			ResourceLocation grenadeThrowSoundId = this.throwSound.getLocation();
			
			S2CMessageSound grenadeThrowMessageSound = new S2CMessageSound(grenadeThrowSoundId, SoundSource.PLAYERS, posX, posY, posZ, 1.0F, 1.0F, shooterId, false, false);
			PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(world, posX, posY, posZ, radius), grenadeThrowMessageSound);
		}
	}
}
package com.mrcrayfish.guns.common.network;

import com.mrcrayfish.guns.entity.ProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.mrcrayfish.guns.GunMod.MOD_ID)
public class ProjectileBlockDamageHandler
{
	
	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
	{
		if(!event.getLevel().isClientSide())
		{
			BlockPos pos = event.getPos();
			Level level = event.getLevel();
			ProjectileEntity.BlockDamageManager.removeDamage(level, pos);
		}
	}
	
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(!event.getLevel().isClientSide())
		{
			BlockPos pos = event.getPos();
			Level level = (Level) event.getLevel();
			ProjectileEntity.BlockDamageManager.removeDamage(level, pos);
		}
	}
	
	@SubscribeEvent
	public static void onExplosionDetonate(ExplosionEvent.Detonate event)
	{
		Level level = event.getLevel();
		if(level.isClientSide())
		{
			return;
		}
		for(BlockPos pos : event.getExplosion().getToBlow())
		{
			ProjectileEntity.BlockDamageManager.removeDamage(level, pos);
		}
	}
	
	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent event)
	{
		if(event.phase == TickEvent.Phase.END && !event.level.isClientSide())
		{
			ProjectileEntity.BlockDamageManager.tick(event.level);
		}
	}
}
package com.an0m3l1.guns.world;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.entity.ProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GunMod.MOD_ID)
public class ProjectileBlockDamageHandler
{
	
	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
	{
		if(!event.getLevel().isClientSide())
		{
			ProjectileEntity.BlockDamageManager.removeDamage(event.getLevel(), event.getPos());
		}
	}
	
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(!event.getLevel().isClientSide())
		{
			ProjectileEntity.BlockDamageManager.removeDamage((Level) event.getLevel(), event.getPos());
		}
	}
	
	@SubscribeEvent
	public static void onExplosionDetonate(ExplosionEvent.Detonate event)
	{
		if(!event.getLevel().isClientSide())
		{
			for(BlockPos pos : event.getExplosion().getToBlow())
			{
				ProjectileEntity.BlockDamageManager.removeDamage(event.getLevel(), pos);
			}
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
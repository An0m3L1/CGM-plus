package com.an0m3l1.guns.client;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.client.render.entity.*;
import com.an0m3l1.guns.init.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = GunMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GunEntityRenderers
{
	@SubscribeEvent
	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(ModEntities.PROJECTILE.get(), ProjectileRenderer::new);
		
		event.registerEntityRenderer(ModEntities.LIGHT_BULLET.get(), BulletRenderer::new);
		event.registerEntityRenderer(ModEntities.MEDIUM_BULLET.get(), BulletRenderer::new);
		event.registerEntityRenderer(ModEntities.HEAVY_BULLET.get(), BulletRenderer::new);
		event.registerEntityRenderer(ModEntities.BUCKSHOT_SHELL.get(), BulletRenderer::new);
		
		event.registerEntityRenderer(ModEntities.PIPE_GRENADE.get(), PipeGrenadeRenderer::new);
		event.registerEntityRenderer(ModEntities.ROCKET.get(), RocketRenderer::new);
		event.registerEntityRenderer(ModEntities.THROWABLE_GRENADE.get(), ThrowableGrenadeRenderer::new);
		event.registerEntityRenderer(ModEntities.THROWABLE_IMPACT_GRENADE.get(), ThrowableGrenadeRenderer::new);
		event.registerEntityRenderer(ModEntities.THROWABLE_SMOKE_GRENADE.get(), ThrowableGrenadeRenderer::new);
		event.registerEntityRenderer(ModEntities.THROWABLE_STUN_GRENADE.get(), ThrowableGrenadeRenderer::new);
		event.registerEntityRenderer(ModEntities.THROWABLE_INCENDIARY_GRENADE.get(), ThrowableGrenadeRenderer::new);
		event.registerEntityRenderer(ModEntities.THROWABLE_MOLOTOV.get(), ThrowableGrenadeRenderer::new);
		event.registerEntityRenderer(ModEntities.LIGHT_SOURCE.get(), LightSourceRenderer::new);
	}
}
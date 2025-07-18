package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.particle.*;
import com.mrcrayfish.guns.init.ModParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleFactoryRegistry
{
    @SubscribeEvent
    public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event)
    {
        ParticleEngine particleManager = Minecraft.getInstance().particleEngine;
        particleManager.register(ModParticleTypes.BULLET_HOLE.get(), (typeIn, worldIn, x, y, z, xSpeed, ySpeed, zSpeed) -> new BulletHoleParticle(worldIn, x, y, z, typeIn.getDirection(), typeIn.getPos()));
        particleManager.register(ModParticleTypes.BLOOD.get(), BloodParticle.Factory::new);
        particleManager.register(ModParticleTypes.HEADSHOT.get(), HeadshotParticle.Factory::new);
        particleManager.register(ModParticleTypes.EXPLOSION.get(), ExplosionParticle.Factory::new);
        particleManager.register(ModParticleTypes.SMOKE_CLOUD.get(), SmokeCloudParticle.Factory::new);
        particleManager.register(ModParticleTypes.SMOKE_EFFECT.get(), SmokeEffectParticle.Factory::new);
        particleManager.register(ModParticleTypes.TRAIL.get(), TrailParticle.Factory::new);
        particleManager.register(ModParticleTypes.CASING.get(), CasingParticle.Factory::new);
        particleManager.register(ModParticleTypes.BRASS_CASING.get(), CasingParticle.Factory::new);
        particleManager.register(ModParticleTypes.SHELL_CASING.get(), CasingParticle.Factory::new);
    }
}

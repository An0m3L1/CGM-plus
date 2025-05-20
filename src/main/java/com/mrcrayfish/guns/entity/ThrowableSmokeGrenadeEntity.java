package com.mrcrayfish.guns.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.audio.SmokeGrenadeExplosionSound;
import com.mrcrayfish.guns.init.*;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageSmokeGrenade;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class ThrowableSmokeGrenadeEntity extends ThrowableGrenadeEntity
{
    public ThrowableSmokeGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    public ThrowableSmokeGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world, LivingEntity player)
    {
        super(entityType, world, player);
        this.setItem(new ItemStack(ModItems.SMOKE_GRENADE_NO_PIN.get()));
    }

    public ThrowableSmokeGrenadeEntity(Level world, LivingEntity player, int timeLeft)
    {
        super(ModEntities.THROWABLE_SMOKE_GRENADE.get(), world, player);
        this.setItem(new ItemStack(ModItems.SMOKE_GRENADE_NO_PIN.get()));
        this.setMaxLife(timeLeft);
    }

    @Override
    public void tick()
    {
        super.tick();
        this.prevRotation = this.rotation;
        double speed = this.getDeltaMovement().length();
        if (speed > 0.1)
        {
            this.rotation += (speed * 50);
        }
        if (this.level.isClientSide)
        {
            this.level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), (Math.random()-0.5) * 0.1, 0.1, (Math.random()-0.5) * 0.1);
        }
    }

    @Override
    public void onDeath()
    {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        double radius = Config.COMMON.explosives.smokeGrenadeCloudDiameter.get() / 2;
        double duration = ((Config.COMMON.explosives.smokeGrenadeCloudDuration.get() - 4) * 20);
        @NotNull SimpleParticleType particle = ModParticleTypes.SMOKE_EFFECT.get();
        Minecraft.getInstance().getSoundManager().play(new SmokeGrenadeExplosionSound(ModSounds.ENTITY_SMOKE_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)y, (float)this.getZ(), 2, 1, this.level.getRandom()));
        if(!this.level.isClientSide)
        {
            //Low level cloud
            AreaEffectCloud cloudLow = new AreaEffectCloud(this.level, this.getX(), this.getY()-0.5, this.getZ());
            cloudLow.setParticle(particle);
            cloudLow.setRadius((float) radius);
            cloudLow.setDuration((int) duration);
            cloudLow.addEffect(new MobEffectInstance(ModEffects.SMOKED.get(), 60, 0, false, false, true));
            cloudLow.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, true));
            this.level.addFreshEntity(cloudLow);

            //Mid level cloud
            AreaEffectCloud cloudMid = new AreaEffectCloud(this.level, this.getX(), this.getY()+0.5, this.getZ());
            cloudMid.setParticle(particle);
            cloudMid.setRadius((float) radius);
            cloudMid.setDuration((int) duration);
            cloudMid.addEffect(new MobEffectInstance(ModEffects.SMOKED.get(), 60, 0, false, false, true));
            cloudMid.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, true));
            this.level.addFreshEntity(cloudMid);

            //High level cloud
            AreaEffectCloud cloudHigh = new AreaEffectCloud(this.level, this.getX(), this.getY()+1.5, this.getZ());
            cloudHigh.setParticle(particle);
            cloudHigh.setRadius((float) radius);
            cloudHigh.setDuration((int) duration);
            cloudHigh.addEffect(new MobEffectInstance(ModEffects.SMOKED.get(), 60, 0, false, false, true));
            cloudHigh.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, true));
            this.level.addFreshEntity(cloudHigh);
        }
        else
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageSmokeGrenade(this.getX(), y, this.getZ()));
    }
}

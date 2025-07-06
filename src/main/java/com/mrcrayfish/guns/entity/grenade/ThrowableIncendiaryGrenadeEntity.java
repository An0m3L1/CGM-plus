package com.mrcrayfish.guns.entity.grenade;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.audio.IncendiaryGrenadeExplosionSound;
import com.mrcrayfish.guns.entity.projectile.GrenadeEntity;
import com.mrcrayfish.guns.init.ModEntities;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageIncendiaryGrenade;
import com.mrcrayfish.guns.util.GrenadeFireHelper;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class ThrowableIncendiaryGrenadeEntity extends ThrowableGrenadeEntity
{
    protected float radius = Config.COMMON.incendiaryGrenadeExplosionRadius.get().floatValue();
    protected int fireDuration = Config.COMMON.incendiaryGrenadeFireDuration.get();

    public ThrowableIncendiaryGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
        if(GunMod.dynamicLightsLoaded && Config.COMMON.enableDynamicLights.get())
            DynamicLightHandlers.registerDynamicLightHandler(entityType, entity -> Config.COMMON.dynamicLightValue.get());
    }

    public ThrowableIncendiaryGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world, LivingEntity player)
    {
        super(entityType, world, player);
        this.setItem(new ItemStack(ModItems.INCENDIARY_GRENADE_NO_PIN.get()));
    }

    public ThrowableIncendiaryGrenadeEntity(Level world, LivingEntity player, int timeLeft)
    {
        super(ModEntities.THROWABLE_INCENDIARY_GRENADE.get(), world, player);
        this.setItem(new ItemStack(ModItems.INCENDIARY_GRENADE_NO_PIN.get()));
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
            this.level.addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY() + 0.25, this.getZ(), (Math.random()-0.5) * 0.1, 0.1, (Math.random()-0.5) * 0.1);
        }
    }

    @Override
    public void onDeath()
    {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        Vec3 center = new Vec3(this.getX(), y, this.getZ());

        Minecraft.getInstance().getSoundManager().play(new IncendiaryGrenadeExplosionSound(ModSounds.INCENDIARY_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)y, (float)this.getZ(), 1, pitch, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageIncendiaryGrenade(this.getX(), y, this.getZ()));
        GrenadeEntity.createFireExplosion(this, radius * 0.6F, false);
        GrenadeFireHelper.igniteEntities(level, center, radius * 1.1F, fireDuration);
    }
}

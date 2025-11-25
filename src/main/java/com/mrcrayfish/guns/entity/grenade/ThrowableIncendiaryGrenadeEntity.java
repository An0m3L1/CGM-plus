package com.mrcrayfish.guns.entity.grenade;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.entity.projectile.GrenadeEntity;
import com.mrcrayfish.guns.init.ModEntities;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageIncendiaryGrenade;
import com.mrcrayfish.guns.network.message.S2CMessageIncendiaryGrenadeUnderwater;
import com.mrcrayfish.guns.util.GrenadeFireHelper;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.core.particles.ParticleTypes;
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
        bounceSound = ModSounds.INCENDIARY_BOUNCE.get();
        useCustomBounceSound = true;
        if(GunMod.dynamicLightsLoaded && Config.COMMON.enableDynamicLights.get())
            DynamicLightHandlers.registerDynamicLightHandler(entityType, entity -> 7);
    }

    public ThrowableIncendiaryGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world, LivingEntity player)
    {
        super(entityType, world, player);
        this.setItem(new ItemStack(ModItems.INCENDIARY_GRENADE_NO_PIN.get()));
        bounceSound = ModSounds.INCENDIARY_BOUNCE.get();
        useCustomBounceSound = true;
    }

    public ThrowableIncendiaryGrenadeEntity(Level world, LivingEntity player, int timeLeft)
    {
        super(ModEntities.THROWABLE_INCENDIARY_GRENADE.get(), world, player);
        this.setItem(new ItemStack(ModItems.INCENDIARY_GRENADE_NO_PIN.get()));
        this.setMaxLife(timeLeft);
        bounceSound = ModSounds.INCENDIARY_BOUNCE.get();
        useCustomBounceSound = true;
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
        if (this.level.isClientSide && !this.isInWater())
        {
            this.level.addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY() + 0.25, this.getZ(), (Math.random()-0.5) * 0.1, 0.1, (Math.random()-0.5) * 0.1);
        }
    }

    @Override
    public void onDeath()
    {
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        Vec3 center = new Vec3(this.getX(), y, this.getZ());

        if(this.level.isClientSide)
        {
            return;
        }

        if(!this.isInWater())
        {
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                    LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageIncendiaryGrenade(this.getX(), y, this.getZ()));
            this.createLight(explosionLightValue, explosionLightLife);
            GrenadeEntity.createFireExplosion(this, radius * 0.6F, false);
            GrenadeFireHelper.igniteEntities(level, center, radius * 1.1F, fireDuration);
        }
        else
        {
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                    LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageIncendiaryGrenadeUnderwater(this.getX(), y, this.getZ()));
        }
    }
}

package com.mrcrayfish.guns.entity.grenade;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.entity.LightSourceEntity;
import com.mrcrayfish.guns.entity.ThrowableItemEntity;
import com.mrcrayfish.guns.entity.projectile.GrenadeEntity;
import com.mrcrayfish.guns.init.ModEntities;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageGrenade;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class ThrowableGrenadeEntity extends ThrowableItemEntity
{
    public float rotation;
    public float prevRotation;
    protected float radius = Config.COMMON.handGrenadeExplosionRadius.get().floatValue();
    protected boolean griefing = Config.COMMON.handGrenadeExplosionGriefing.get();

    public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
        bounceSound = ModSounds.GRENADE_BOUNCE.get();
        useCustomBounceSound = true;
    }

    public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity entity)
    {
        super(entityType, world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(ModItems.GRENADE_NO_PIN.get()));
        this.setMaxLife(20 * 3);
        bounceSound = ModSounds.GRENADE_BOUNCE.get();
        useCustomBounceSound = true;
    }

    public ThrowableGrenadeEntity(Level world, LivingEntity entity, int timeLeft)
    {
        super(ModEntities.THROWABLE_GRENADE.get(), world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(ModItems.GRENADE_NO_PIN.get()));
        this.setMaxLife(timeLeft);
        bounceSound = ModSounds.GRENADE_BOUNCE.get();
        useCustomBounceSound = true;
    }

    @Override
    protected void defineSynchedData()
    {
    }

    @Override
    public void tick()
    {
        super.tick();
        this.prevRotation = this.rotation;
        double speed = this.getDeltaMovement().length();
        if (speed > 0.1)
        {
            this.rotation += speed * 50;
        }
        if (this.level.isClientSide)
        {
            this.level.addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void onDeath()
    {
        GrenadeEntity.createCustomExplosion(this, radius, griefing);
        double y = this.getY() + this.getType().getDimensions().height * 0.5;
        if(this.level.isClientSide)
        {
            return;
        }
        LightSourceEntity light = new LightSourceEntity(level, this.getX(), this.getY(), this.getZ(), 12);
        level.addFreshEntity(light);
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageGrenade(this.getX(), y, this.getZ()));
    }
}

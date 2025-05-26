package com.mrcrayfish.guns.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.audio.IncendiaryGrenadeExplosionSound;
import com.mrcrayfish.guns.init.*;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageIncendiaryGrenade;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class ThrowableIncendiaryGrenadeEntity extends ThrowableGrenadeEntity
{
    protected float radius = Config.COMMON.explosives.incendiaryGrenadeExplosionRadius.get().floatValue() - 1F;

    public ThrowableIncendiaryGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
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
        Minecraft.getInstance().getSoundManager().play(new IncendiaryGrenadeExplosionSound(ModSounds.ENTITY_INCENDIARY_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)y, (float)this.getZ(), 2, 1, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageIncendiaryGrenade(this.getX(), y, this.getZ()));
        GrenadeEntity.createFireExplosion(this, radius, true);
    }
}

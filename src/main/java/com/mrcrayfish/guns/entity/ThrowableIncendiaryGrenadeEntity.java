package com.mrcrayfish.guns.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.audio.IncendiaryGrenadeExplosionSound;
import com.mrcrayfish.guns.init.ModEntities;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageIncendiaryGrenade;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class ThrowableIncendiaryGrenadeEntity extends ThrowableGrenadeEntity
{
    protected float radius = Config.COMMON.explosives.incendiaryGrenadeExplosionRadius.get().floatValue() - 1F;
    protected int fireDuration = Config.COMMON.explosives.incendiaryGrenadeFireDuration.get();

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
        Minecraft.getInstance().getSoundManager().play(new IncendiaryGrenadeExplosionSound(ModSounds.INCENDIARY_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)y, (float)this.getZ(), 1, pitch, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageIncendiaryGrenade(this.getX(), y, this.getZ()));
        GrenadeEntity.createFireExplosion(this, radius, true);

        // Calculate bounds of area where potentially effected entities may be
        double diameter = radius * 2;
        int minX = Mth.floor(this.getX() - diameter);
        int maxX = Mth.floor(this.getX() + diameter);
        int minY = Mth.floor(y - diameter);
        int maxY = Mth.floor(y + diameter);
        int minZ = Mth.floor(this.getZ() - diameter);
        int maxZ = Mth.floor(this.getZ() + diameter);

        Vec3 explosionPos = new Vec3(this.getX(), y, this.getZ());
        double radiusSq = this.radius * this.radius;

        // Affect all non-spectating players and entities in range of the blast
        for(LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class, new AABB(minX, minY, minZ, maxX, maxY, maxZ)))
        {
            if(entity.ignoreExplosion())
                continue;

            Vec3 entityPos = entity.getBoundingBox().getCenter();
            double distanceSq = explosionPos.distanceToSqr(entityPos);

            if(distanceSq > radiusSq) continue;

            ClipContext context = new ClipContext(explosionPos, entityPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);

            if(level.clip(context).getType() == HitResult.Type.MISS)
            {
                entity.setSecondsOnFire(fireDuration);
                entity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
            }
        }
    }
}

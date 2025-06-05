package com.mrcrayfish.guns.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.audio.RocketExplosionSound;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageRocket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class RocketEntity extends ProjectileEntity
{
    protected float radius = Config.COMMON.explosives.rocketExplosionRadius.get().floatValue();
    protected boolean griefing = !Config.COMMON.explosives.rocketExplosionGriefing.get();

    public RocketEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public RocketEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
    {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick()
    {
        if (this.level.isClientSide)
        {
            for (int i = 5; i > 0; i--)
            {
                this.level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
            }
            if (this.level.random.nextInt(2) == 0)
            {
                this.level.addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                this.level.addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot)
    {
        createCustomExplosion(this, radius, griefing);
        Minecraft.getInstance().getSoundManager().play(new RocketExplosionSound(ModSounds.ROCKET_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)this.getY(), (float)this.getZ(), 1, pitch, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256), new S2CMessageRocket(this.getX(), this.getY(), this.getZ()));
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
    {
        createCustomExplosion(this, radius, griefing);
        Minecraft.getInstance().getSoundManager().play(new RocketExplosionSound(ModSounds.ROCKET_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)this.getY(), (float)this.getZ(), 1, pitch, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256), new S2CMessageRocket(this.getX(), this.getY(), this.getZ()));

    }

    @Override
    public void onExpired()
    {
        createCustomExplosion(this, radius, griefing);
        Minecraft.getInstance().getSoundManager().play(new RocketExplosionSound(ModSounds.ROCKET_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)this.getY(), (float)this.getZ(), 1, pitch, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256), new S2CMessageRocket(this.getX(), this.getY(), this.getZ()));

    }
}

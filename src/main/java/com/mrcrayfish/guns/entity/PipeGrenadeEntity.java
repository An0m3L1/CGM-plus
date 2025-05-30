package com.mrcrayfish.guns.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.audio.PipeGrenadeExplosionSound;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessagePipeGrenade;
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
public class PipeGrenadeEntity extends ProjectileEntity
{
    public float rotation;
    public float prevRotation;
    protected float radius = Config.COMMON.explosives.pipeGrenadeExplosionRadius.get().floatValue();
    protected boolean griefing = !Config.COMMON.explosives.pipeGrenadeExplosionGriefing.get();
    protected float pitch = 0.9F + level.random.nextFloat() * 0.2F;

    public PipeGrenadeEntity(EntityType<? extends ProjectileEntity> entityType, Level world)
    {
        super(entityType, world);
    }

    public PipeGrenadeEntity(EntityType<? extends ProjectileEntity> entityType, Level world, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
    {
        super(entityType, world, shooter, weapon, item, modifiedGun);
    }

    @Override
    protected void onProjectileTick()
    {
        if (this.level.isClientSide)
        {
            this.level.addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    public void tick()
    {
        super.tick();
        this.prevRotation = this.rotation;
        double speed = this.getDeltaMovement().length();
        if (speed > 0.1)
        {
            this.rotation += speed * 50;
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot)
    {
        createCustomExplosion(this, radius, griefing);
        Minecraft.getInstance().getSoundManager().play(new PipeGrenadeExplosionSound(ModSounds.ENTITY_PIPE_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)this.getY(), (float)this.getZ(), 2, pitch, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256), new S2CMessagePipeGrenade(this.getX(), this.getY(), this.getZ()));    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
    {
        createCustomExplosion(this, radius, griefing);
        Minecraft.getInstance().getSoundManager().play(new PipeGrenadeExplosionSound(ModSounds.ENTITY_PIPE_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)this.getY(), (float)this.getZ(), 2, pitch, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256), new S2CMessagePipeGrenade(this.getX(), this.getY(), this.getZ()));    }

    @Override
    public void onExpired()
    {
        createCustomExplosion(this, radius, griefing);
        Minecraft.getInstance().getSoundManager().play(new PipeGrenadeExplosionSound(ModSounds.ENTITY_PIPE_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)this.getX(),(float)this.getY(), (float)this.getZ(), 2, pitch, this.level.getRandom()));
        if(this.level.isClientSide)
        {
            return;
        }
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256), new S2CMessagePipeGrenade(this.getX(), this.getY(), this.getZ()));    }
}

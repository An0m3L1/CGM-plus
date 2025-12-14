package com.mrcrayfish.guns.entity.grenade;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.audio.DistancedSound;
import com.mrcrayfish.guns.entity.projectile.GrenadeEntity;
import com.mrcrayfish.guns.init.ModEntities;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageMolotov;
import com.mrcrayfish.guns.network.message.S2CMessageMolotovUnderwater;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class ThrowableMolotovEntity extends ThrowableGrenadeEntity
{
    protected float radius = Config.COMMON.molotovExplosionRadius.get().floatValue();
    protected int fireDuration = Config.COMMON.molotovFireDuration.get();
    private int bounceCount = 1;
    private boolean wasInWater = false;

    public ThrowableMolotovEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
    {
        super(entityType, world);
        bounceSound = ModSounds.MOLOTOV_BOUNCE.get();
        useCustomBounceSound = true;
    }

    public ThrowableMolotovEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world, LivingEntity player)
    {
        super(entityType, world, player);
        this.setItem(new ItemStack(ModItems.MOLOTOV.get()));
        this.setShouldBounce(false);
        bounceSound = ModSounds.MOLOTOV_BOUNCE.get();
        useCustomBounceSound = true;
    }

    public ThrowableMolotovEntity(Level world, LivingEntity player, int timeLeft)
    {
        super(ModEntities.THROWABLE_MOLOTOV.get(), world, player);
        this.setItem(new ItemStack(ModItems.MOLOTOV.get()));
        this.setMaxLife(200);
        this.setShouldBounce(false);
        bounceSound = ModSounds.MOLOTOV_BOUNCE.get();
        useCustomBounceSound = true;
    }

    public void tick()
    {
        super.tick();
        this.prevRotation = this.rotation;
        double speed = this.getDeltaMovement().length();
        if (speed > 0.1)
        {
            this.rotation += (speed * 50);
        }
        if (this.isInWater())
        {
            this.wasInWater = true;
        }
        if (this.level.isClientSide && !this.isInWater() && !this.wasInWater)
        {
            this.level.addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY() + 0.75, this.getZ(), (Math.random() - 0.5) * 0.1, 0.1, (Math.random() - 0.5) * 0.1);
        }
    }

    @Override
    protected void onHit(HitResult result)
    {
        if(result.getType().equals(HitResult.Type.BLOCK))
        {
            BlockHitResult blockResult = (BlockHitResult) result;
            Direction direction = blockResult.getDirection();

            // Explode when hitting a floor
            if (direction == Direction.UP)
            {
                this.remove(Entity.RemovalReason.KILLED);
                this.onDeath();
            }
            else
            {
                // Bounce off the wall
                if (bounceCount > 0)
                {
                    bounceCount--;
                    this.bounce(direction);

                    BlockPos resultPos = blockResult.getBlockPos();
                    BlockState state = this.level.getBlockState(resultPos);
                    ResourceLocation sound = state.getBlock().getSoundType(state, this.level, resultPos, this).getStepSound().getLocation();
                    if (bounceSound != null && useCustomBounceSound)
                        sound = bounceSound.getLocation();

                    double speed = this.getDeltaMovement().length();
                    float x = (float)result.getLocation().x;
                    float y = (float)result.getLocation().y;
                    float z = (float)result.getLocation().z;

                    if(speed > 0.3 && this.level.isClientSide())
                    {
                        Minecraft.getInstance().getSoundManager().play(DistancedSound.grenadeBounce(sound, SoundSource.BLOCKS, x, y, z, 0.85F, 1, level.getRandom()));
                    }
                }
                // If already bounced, explode
                else
                {
                    this.remove(Entity.RemovalReason.KILLED);
                    this.onDeath();
                }
            }
        }
        // Bounce off entities
        else if(result.getType().equals(HitResult.Type.ENTITY))
        {
            EntityHitResult entityResult = (EntityHitResult) result;
            Entity entity = entityResult.getEntity();
            double speed = this.getDeltaMovement().length();
            if(speed > 0.1)
            {
                entity.hurt(DamageSource.thrown(this, this.getOwner()), 1.0F);
            }
            this.bounce(Direction.getNearest(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()).getOpposite());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.25, 1.0, 0.25));
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

        if(this.isInWater() || this.wasInWater)
        {
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                    LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageMolotovUnderwater(this.getX(), y, this.getZ()));
        }
        else
        {
            PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                    LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageMolotov(this.getX(), y, this.getZ()));
            this.createLight(explosionLightValue, explosionLightLife);
            GrenadeEntity.createFireExplosion(this, radius * 0.6F, false);
            ThrowableGrenadeEntity.igniteEntities(level, center, radius * 1.1F, fireDuration);
        }
    }
}
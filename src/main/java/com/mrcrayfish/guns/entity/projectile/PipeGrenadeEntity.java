package com.mrcrayfish.guns.entity.projectile;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.entity.LightSourceEntity;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.init.ModTags;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessagePipeGrenade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot)
    {
        explode();
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
    {
        /* If the projectile hit a fragile block, check if projectile griefing is enabled globally, for the projectile itself and explosion griefing is disabled.
        * We check for explosion griefing so the projectile doesn't just disappear */
        if (this.getProjectile() != null && state.is(ModTags.Blocks.HARDNESS_NONE))
        {
            if(!Config.COMMON.universalExplosionGriefing.get() && Config.COMMON.projectileGriefing.get() && this.getProjectile().isGriefing())
            {
                this.level.destroyBlock(pos, Config.COMMON.projectileGriefingBlockDrops.get());
            }
            else
            {
                explode();
            }
        }
        else
        {
            explode();
        }
    }

    @Override
    public void onExpired()
    {
        explode();
    }

    private void explode()
    {
        float radius = 4.0F;
        boolean griefing = true;

        if(this.getProjectile() != null)
        {
            radius = this.getProjectile().getExplosionRadius();
            griefing = this.getProjectile().isGriefing();
        }

        createExplosion(this, radius, griefing);
        if(this.level.isClientSide)
            return;

        LightSourceEntity light = new LightSourceEntity(level, this.getX(), this.getY(), this.getZ(), explosionLightValue, explosionLightLife);
        level.addFreshEntity(light);
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256), new S2CMessagePipeGrenade(this.getX(), this.getY(), this.getZ(), radius));
    }
}
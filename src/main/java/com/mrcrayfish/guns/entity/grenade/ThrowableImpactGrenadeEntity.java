package com.mrcrayfish.guns.entity.grenade;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.entity.ThrowableItemEntity;
import com.mrcrayfish.guns.entity.projectile.GrenadeEntity;
import com.mrcrayfish.guns.init.ModEntities;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageImpactGrenade;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class ThrowableImpactGrenadeEntity extends ThrowableGrenadeEntity
{
    protected float radius = Config.COMMON.impactGrenadeExplosionRadius.get().floatValue();
    protected boolean griefing = Config.COMMON.impactGrenadeExplosionGriefing.get();

    public ThrowableImpactGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public ThrowableImpactGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity entity)
    {
        super(entityType, world, entity);
        this.setItem(new ItemStack(ModItems.IMPACT_GRENADE_NO_PIN.get()));
        this.setShouldBounce(false);
    }

    public ThrowableImpactGrenadeEntity(Level world, LivingEntity entity, int timeLeft)
    {
        super(ModEntities.THROWABLE_IMPACT_GRENADE.get(), world, entity);
        this.setItem(new ItemStack(ModItems.IMPACT_GRENADE_NO_PIN.get()));
        this.setMaxLife(200);
        this.setShouldBounce(false);
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
        this.createLight(explosionLightValue, explosionLightLife);
        PacketHandler.getPlayChannel().sendToNearbyPlayers(() ->
                LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageImpactGrenade(this.getX(), y, this.getZ()));
    }
}

package com.mrcrayfish.guns.entity;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModParticleTypes;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class HeavyBulletEntity extends ProjectileEntity
{
    public float rotation;
    public float prevRotation;

    public HeavyBulletEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
    }

    public HeavyBulletEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
    {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.setItem(new ItemStack(ModItems.HEAVY_BULLET_MODEL.get()));
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
}

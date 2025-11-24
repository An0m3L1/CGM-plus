package com.mrcrayfish.guns.entity.projectile;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BulletEntity extends ProjectileEntity
{
    private final Item bulletModel;

    public BulletEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn)
    {
        super(entityType, worldIn);
        this.bulletModel = ModItems.LIGHT_BULLET_MODEL.get();
    }

    public BulletEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun, Item bulletModel)
    {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.bulletModel = bulletModel;
        this.setItem(new ItemStack(this.bulletModel));
    }

    public static BulletEntity createBuckshot(EntityType<? extends ProjectileEntity> type, Level world, LivingEntity shooter, ItemStack weapon, GunItem gunItem, Gun gun) {
        return new BulletEntity(type, world, shooter, weapon, gunItem, gun, ModItems.BUCKSHOT_MODEL.get());
    }
    public static BulletEntity createHeavy(EntityType<? extends ProjectileEntity> type, Level world, LivingEntity shooter, ItemStack weapon, GunItem gunItem, Gun gun) {
        return new BulletEntity(type, world, shooter, weapon, gunItem, gun, ModItems.HEAVY_BULLET_MODEL.get());
    }
    public static BulletEntity createLight(EntityType<? extends ProjectileEntity> type, Level world, LivingEntity shooter, ItemStack weapon, GunItem gunItem, Gun gun) {
        return new BulletEntity(type, world, shooter, weapon, gunItem, gun, ModItems.LIGHT_BULLET_MODEL.get());
    }
    public static BulletEntity createMedium(EntityType<? extends ProjectileEntity> type, Level world, LivingEntity shooter, ItemStack weapon, GunItem gunItem, Gun gun) {
        return new BulletEntity(type, world, shooter, weapon, gunItem, gun, ModItems.MEDIUM_BULLET_MODEL.get());
    }
}
package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class GunCompositeStatHelper
{
	// This helper delivers composite stats derived from GunModifierHelper
	
	public static int getCompositeRate(ItemStack weapon, Gun modifiedGun, Player player)
    {
        int a = modifiedGun.getGeneral().getRate();
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return GunModifierHelper.getRampUpRate(player, weapon, b);
    }
	public static int getCompositeRate(ItemStack weapon, Player player) {
		// Version of getCompositeRate that only requires an ItemStack and Player input
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		int a = modifiedGun.getGeneral().getRate();
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return GunModifierHelper.getRampUpRate(player, weapon, b);
	}
	
	public static int getCompositeBaseRate(ItemStack weapon, Gun modifiedGun)
    {
        int a = modifiedGun.getGeneral().getRate();
        return GunModifierHelper.getModifiedRate(weapon, a);
    }
	public static int getCompositeBaseRate(ItemStack weapon) {
		// Version of getCompositeBaseRate that only requires an ItemStack input
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		int a = modifiedGun.getGeneral().getRate();
		return GunModifierHelper.getModifiedRate(weapon, a);
	}
	
	public static float getCompositeSpread(ItemStack weapon, Gun modifiedGun)
    {
        return GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());
    }
	
	public static float getCompositeMinSpread(ItemStack weapon, Gun modifiedGun)
    {
        return GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getRestingSpread());
    }


	public static int getAmmoCapacity(ItemStack weapon) {
		Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		return getAmmoCapacity(weapon, modifiedGun);
	}
    public static int getAmmoCapacity(ItemStack weapon, Gun modifiedGun)
    {
        return Gun.getModifiedAmmoCapacity(weapon);
    }
    
	public static double getCompositeAimDownSightSpeed(ItemStack weapon)
    {
		double a = GunModifierHelper.getAimDownSightSpeed(weapon);
		return GunModifierHelper.getModifiedAimDownSightSpeed(weapon, a);
    }
	
	public static int getRealReloadSpeed(ItemStack weapon, boolean magReload, boolean reloadFromEmpty)
    {
        if (magReload)
        	return getMagReloadSpeed(weapon, reloadFromEmpty);

        return getReloadInterval(weapon, reloadFromEmpty);
    }

    public static int getReloadInterval(ItemStack weapon, boolean reloadFromEmpty)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        int baseInterval = modifiedGun.getGeneral().getReloadRate();
        int interval = modifiedGun.getGeneral().getReloadRate();
        return Math.max(interval, 1);
    }

    public static int getMagReloadSpeed(ItemStack weapon, boolean reloadFromEmpty)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        int baseSpeed = modifiedGun.getGeneral().getMagReloadTime();
        double reloadSpeedModifier = GunModifierHelper.getReloadSpeedModifier(weapon);
        
        int speed = (int) Math.round((baseSpeed) * reloadSpeedModifier);
        if (reloadFromEmpty)
        {
        	baseSpeed = modifiedGun.getGeneral().getMagReloadFromEmptyTime();
        	speed = (int) Math.round((baseSpeed) * reloadSpeedModifier);
    	}
        return Math.max(speed, 4);
    }

    public static int getGunshotLightValue(ItemStack weapon)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        int lightLevel = 7;

        if(GunModifierHelper.isSilencedFire(weapon))
            lightLevel = 4;

        return lightLevel;
    }

    public static float getHeadshotDamage(ItemStack weapon)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        float damage = modifiedGun.getProjectile().getDamage(); // Get base damage of the gun
        damage = GunModifierHelper.getModifiedProjectileDamage(weapon, damage); // Get modified damage of the gun

        if (modifiedGun.getProjectile().getHeadshotMultiplierOverride()!=0)
            damage *= modifiedGun.getProjectile().getHeadshotMultiplierOverride();
        else
        {
            double hm = Config.COMMON.headShotDamageMultiplier.get();
            float headshotMultiplier = (float) Math.max(hm,modifiedGun.getProjectile().getHeadshotMultiplierMin());
            damage *= headshotMultiplier+modifiedGun.getProjectile().getHeadshotMultiplierBonus();
        }

        if (modifiedGun.getProjectile().getHeadshotExtraDamage()>0)
            damage += modifiedGun.getProjectile().getHeadshotExtraDamage();

        return damage;
    }
}

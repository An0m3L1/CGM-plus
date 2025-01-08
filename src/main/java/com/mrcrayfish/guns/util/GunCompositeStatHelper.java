package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class GunCompositeStatHelper
{
	// This helper delivers composite stats derived from GunModifierHelper and GunEnchantmentHelper.
	
	public static int getCompositeRate(ItemStack weapon, Gun modifiedGun, Player player)
    {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return GunEnchantmentHelper.getRampUpRate(player, weapon, b);
    }
	public static int getCompositeRate(ItemStack weapon, Player player) {
		// Version of getCompositeRate that only requires an ItemStack and Player input
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        int b = GunModifierHelper.getModifiedRate(weapon, a);
        return GunEnchantmentHelper.getRampUpRate(player, weapon, b);
	}
	
	public static int getCompositeBaseRate(ItemStack weapon, Gun modifiedGun)
    {
        int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        return GunModifierHelper.getModifiedRate(weapon, a);
    }
	public static int getCompositeBaseRate(ItemStack weapon) {
		// Version of getCompositeBaseRate that only requires an ItemStack input
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		int a = GunEnchantmentHelper.getRate(weapon, modifiedGun);
		return GunModifierHelper.getModifiedRate(weapon, a);
	}
	
	public static float getCompositeSpread(ItemStack weapon, Gun modifiedGun)
    {
        //float a = GunEnchantmentHelper.getSpread(weapon, modifiedGun);
		//return GunModifierHelper.getModifiedSpread(weapon, a);
        return GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());
    }
	
	public static float getCompositeMinSpread(ItemStack weapon, Gun modifiedGun)
    {
        //float a = GunEnchantmentHelper.getMinSpread(weapon, modifiedGun);
		//return GunModifierHelper.getModifiedSpread(weapon, a);
        return GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getRestingSpread());
    }


	public static int getAmmoCapacity(ItemStack weapon) {
		Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
		return getAmmoCapacity(weapon, modifiedGun);
	}
    public static int getAmmoCapacity(ItemStack weapon, Gun modifiedGun)
    {
        int capacity = Gun.getModifiedAmmoCapacity(weapon);
        int extraCapacity = modifiedGun.getGeneral().getOverCapacityAmmo();
        if (extraCapacity <= 0)
        {
            extraCapacity = modifiedGun.getGeneral().getMaxAmmo() / 2;
        }
        return capacity;
    }
    
	public static double getCompositeAimDownSightSpeed(ItemStack weapon)
    {
		double a = GunEnchantmentHelper.getAimDownSightSpeed(weapon);
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
}

package com.mrcrayfish.guns.util;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.interfaces.IGunModifier;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.attachment.impl.IAttachment;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class GunModifierHelper
{
    private static final IGunModifier[] EMPTY = {};

    private static IGunModifier[] getModifiers(ItemStack weapon, IAttachment.Type type)
    {
        ItemStack stack = Gun.getAttachment(type, weapon);
        if(!stack.isEmpty() && stack.getItem() instanceof IAttachment<?> attachment)
        {
            return attachment.getProperties().getModifiers();
        }
        return EMPTY;
    }
    private static IAttachment.Type getType(IAttachment.Type type)
    {
        return type;
    }

    public static int getModifiedProjectileLife(ItemStack weapon, int life)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                life = modifier.modifyProjectileLife(life);
            }
        }
        return life;
    }

    public static double getModifiedProjectileGravity(ItemStack weapon, double gravity)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                gravity = modifier.modifyProjectileGravity(gravity);
            }
        }
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                gravity += modifier.additionalProjectileGravity();
            }
        }
        return gravity;
    }

    public static float getModifiedSpread(ItemStack weapon, float spread)
    {
    	Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
    	for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            IAttachment.Type attachType = getType(IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
            	if (!modifiedGun.getGeneral().usesShotgunSpread())
            	spread = modifier.modifyProjectileSpread(spread);
            	else
            	spread = Mth.lerp((attachType == IAttachment.Type.BARREL ? 0.8F : 0.2F),spread,modifier.modifyProjectileSpread(spread));
            }
        }
        return spread;
    }

    public static double getModifiedProjectileSpeed(ItemStack weapon, double speed)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyProjectileSpeed(speed);
            }
        }
        return speed;
    }

    public static float getFireSoundVolume(ItemStack weapon)
    {
        float volume = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                volume = modifier.modifyFireSoundVolume(volume);
            }
        }
        return Mth.clamp(volume, 0.0F, 16.0F);
    }

    @Deprecated(since = "1.3.0", forRemoval = true)
    public static double getMuzzleFlashSize(ItemStack weapon, double size)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                size = modifier.modifyMuzzleFlashSize(size);
            }
        }
        return size;
    }

    public static double getMuzzleFlashScale(ItemStack weapon, double scale)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                scale = modifier.modifyMuzzleFlashScale(scale);
            }
        }
        return scale;
    }

    public static float getKickReduction(ItemStack weapon)
    {
        float kickReduction = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                kickReduction *= Mth.clamp((modifier.kickModifier()*0.8F)+0.2F, 0.0F, 1.0F);
            }
        }
        return 1.0F - kickReduction;
    }

    public static float getRecoilModifier(ItemStack weapon)
    {
        float recoilReduction = 1.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                recoilReduction *= Math.max(modifier.recoilModifier(), 0.0F);
            }
        }
        return 1.0F - recoilReduction;
    }

    public static boolean isSilencedFire(ItemStack weapon)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                if(modifier.silencedFire())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static double getModifiedFireSoundRadius(ItemStack weapon, double radius)
    {
        double minRadius = radius;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                double newRadius = modifier.modifyFireSoundRadius(radius);
                if(newRadius < minRadius)
                {
                    minRadius = newRadius;
                }
            }
        }
        return Mth.clamp(minRadius, 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(ItemStack weapon)
    {
        float additionalDamage = 0.0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                additionalDamage += modifier.additionalDamage();
            }
        }
        return additionalDamage;
    }

    public static float getModifiedProjectileDamage(ItemStack weapon, float damage)
    {
        float finalDamage = damage;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                finalDamage = modifier.modifyProjectileDamage(finalDamage);
            }
        }
        return finalDamage;
    }

    public static float getModifiedDamage(ItemStack weapon, Gun modifiedGun, float damage)
    {
        float finalDamage = damage;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                finalDamage = modifier.modifyProjectileDamage(finalDamage);
            }
        }
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                finalDamage += modifier.additionalDamage();
            }
        }
        return finalDamage;
    }

    public static double getAimDownSightSpeed(ItemStack weapon)
    {
        if(!(weapon.getItem() instanceof GunItem))
            return 1;

        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        double speed = modifiedGun.getGeneral().getADSSpeed();
        return Math.max(speed,0.01);
    }

    public static double getModifiedAimDownSightSpeed(ItemStack weapon, double speed)
    {
        if (!(weapon.getItem() instanceof GunItem))
            return speed;

        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                speed = modifier.modifyAimDownSightSpeed(speed);
            }
        }
        return Mth.clamp(speed, 0.01, Double.MAX_VALUE);
    }

    public static int getModifiedRate(ItemStack weapon, int rate)
    {
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                rate = modifier.modifyFireRate(rate);
            }
        }
        return Mth.clamp(rate, 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(ItemStack weapon)
    {
        float chance = 0F;
        for(int i = 0; i < IAttachment.Type.values().length; i++)
        {
            IGunModifier[] modifiers = getModifiers(weapon, IAttachment.Type.values()[i]);
            for(IGunModifier modifier : modifiers)
            {
                chance += modifier.criticalChance();
            }
        }
        return Mth.clamp(chance, 0F, 1F);
    }

    public static double getReloadSpeedModifier(ItemStack weapon)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        ItemStack magStack = Gun.getAttachment(IAttachment.Type.byTagKey("Magazine"), weapon);
        double reloadSpeedModifier = 1;
        if(!magStack.isEmpty())
        {
            if (magStack.getItem().builtInRegistryHolder().key().location().getPath().equals("light_magazine"))
            {
                reloadSpeedModifier = modifiedGun.getGeneral().getLightMagReloadTimeModifier();
            }
            else
            if (magStack.getItem().builtInRegistryHolder().key().location().getPath().equals("extended_magazine"))
            {
                reloadSpeedModifier = modifiedGun.getGeneral().getExtendedMagReloadTimeModifier();
            }
        }
        return reloadSpeedModifier;
    }

    public static int getRampUpRate(Player player, ItemStack weapon, int baseRate)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        int maxRate = getRampUpMaxRate(weapon, baseRate);
        int minRate = getRampUpMinRate(maxRate);
        int newRate = baseRate;

        if(modifiedGun.getGeneral().hasDoRampUp())
        {
            int rampUpShot = ModSyncedDataKeys.RAMPUPSHOT.getValue(player);
            float rampFactor = (float) (Math.log((float) rampUpShot+1)/Math.log((float) getRampUpMaxShots(modifiedGun)));
            float rampedRate = (float) Math.ceil((float) Mth.lerp(rampFactor,minRate,maxRate));
            newRate = (int) Math.max(rampedRate, maxRate);
        }
        return newRate;
    }

    public static int getRampUpMaxShots(Gun gun)
    {
        return gun.getGeneral().getRampUpShotsNeeded();
    }

    public static int getRampUpMinRate(int rate)
    {
        return rate+3;
    }

    public static int getRampUpMaxRate(ItemStack weapon, int rate)
    {
        return rate;
    }

    public static int getRampUpMaxRate(ItemStack weapon, Gun modifiedGun)
    {
        return modifiedGun.getGeneral().getRate();
    }
}

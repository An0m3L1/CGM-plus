package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.GunMod;
import net.minecraft.world.item.Item;

/**
 * A basic item class that implements {@link IAmmo} to indicate this item is ammunition
 *
 * Author: MrCrayfish
 */
public class AmmoItem extends Item implements IAmmo
{
    public AmmoItem(Properties properties)
    {
        super(properties.tab(GunMod.GUNS));
    }
}

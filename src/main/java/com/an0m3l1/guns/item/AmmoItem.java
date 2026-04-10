package com.an0m3l1.guns.item;

import com.an0m3l1.guns.GunMod;
import net.minecraft.world.item.Item;

/**
 * A basic item class that implements {@link IAmmo} to indicate this item is ammunition
 * <p>
 * Author: MrCrayfish
 */
public class AmmoItem extends Item implements IAmmo
{
	public AmmoItem(Properties properties)
	{
		super(properties.tab(GunMod.GUNS));
	}
}

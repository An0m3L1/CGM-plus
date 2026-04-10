package com.an0m3l1.guns.item.attachment;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.item.IMeta;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class AttachmentItem extends Item implements IMeta
{
	public AttachmentItem(Properties properties)
	{
		super(properties.stacksTo(1).tab(GunMod.GUNS));
	}
	
	@Override
	public boolean isFoil(@NotNull ItemStack stack)
	{
		return false;
	}
	
	@Override
	public boolean isEnchantable(@NotNull ItemStack stack)
	{
		return false;
	}
}

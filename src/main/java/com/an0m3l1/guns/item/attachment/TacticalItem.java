package com.an0m3l1.guns.item.attachment;

import com.an0m3l1.guns.item.IColored;
import com.an0m3l1.guns.item.attachment.impl.ITactical;
import com.an0m3l1.guns.item.attachment.impl.create.Tactical;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A basic under barrel attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class TacticalItem extends AttachmentItem implements ITactical, IColored
{
	private final Tactical tactical;
	private final boolean colored;
	
	public TacticalItem(Tactical tactical, Properties properties)
	{
		super(properties);
		this.tactical = tactical;
		this.colored = true;
	}
	
	public TacticalItem(Tactical tactical, Properties properties, boolean colored)
	{
		super(properties);
		this.tactical = tactical;
		this.colored = colored;
	}
	
	@Override
	public Tactical getProperties()
	{
		return this.tactical;
	}
	
	@Override
	public boolean canColor(ItemStack stack)
	{
		return this.colored;
	}
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		return false;
	}
}

package com.an0m3l1.guns.item.attachment;

import com.an0m3l1.guns.item.IColored;
import com.an0m3l1.guns.item.attachment.impl.IStock;
import com.an0m3l1.guns.item.attachment.impl.create.Stock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A basic stock attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class StockItem extends AttachmentItem implements IStock, IColored
{
	private final Stock stock;
	private final boolean colored;
	
	public StockItem(Stock stock, Properties properties)
	{
		super(properties);
		this.stock = stock;
		this.colored = true;
	}
	
	public StockItem(Stock stock, Properties properties, boolean colored)
	{
		super(properties);
		this.stock = stock;
		this.colored = colored;
	}
	
	@Override
	public Stock getProperties()
	{
		return this.stock;
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

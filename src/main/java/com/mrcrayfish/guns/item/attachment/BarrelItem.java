package com.mrcrayfish.guns.item.attachment;

import com.mrcrayfish.guns.item.IColored;
import com.mrcrayfish.guns.item.attachment.impl.IBarrel;
import com.mrcrayfish.guns.item.attachment.impl.create.Barrel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A basic barrel attachment item implementation with color support
 *
 * Author: MrCrayfish
 */
public class BarrelItem extends AttachmentItem implements IBarrel, IColored
{
    private final Barrel barrel;
    private final boolean colored;

    public BarrelItem(Barrel barrel, Item.Properties properties)
    {
        super(properties);
        this.barrel = barrel;
        this.colored = true;
    }

    public BarrelItem(Barrel barrel, Item.Properties properties, boolean colored)
    {
        super(properties);
        this.barrel = barrel;
        this.colored = colored;
    }

    @Override
    public Barrel getProperties()
    {
        return this.barrel;
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

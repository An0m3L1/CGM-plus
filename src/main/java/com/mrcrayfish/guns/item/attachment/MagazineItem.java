package com.mrcrayfish.guns.item.attachment;

import com.mrcrayfish.guns.item.IColored;
import com.mrcrayfish.guns.util.attachment.IMagazine;
import com.mrcrayfish.guns.util.attachment.impl.Magazine;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A basic under barrel attachment item implementation with color support
 *
 * Author: MrCrayfish
 */
public class MagazineItem extends AttachmentItem implements IMagazine, IColored
{
    private final Magazine magazine;
    private final boolean colored;

    public MagazineItem(Magazine magazine, Properties properties)
    {
        super(properties);
        this.magazine = magazine;
        this.colored = false;
    }

    public MagazineItem(Magazine magazine, Properties properties, boolean colored)
    {
        super(properties);
        this.magazine = magazine;
        this.colored = false;
    }

    @Override
    public Magazine getProperties()
    {
        return this.magazine;
    }

    @Override
    public boolean canColor(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        return false;
    }
}

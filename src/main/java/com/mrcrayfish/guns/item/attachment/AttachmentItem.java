package com.mrcrayfish.guns.item.attachment;

import com.mrcrayfish.guns.item.IMeta;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class AttachmentItem extends Item implements IMeta
{
    public AttachmentItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }
}

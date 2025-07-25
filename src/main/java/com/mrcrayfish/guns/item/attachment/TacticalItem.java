package com.mrcrayfish.guns.item.attachment;

import com.mrcrayfish.guns.item.IColored;
import com.mrcrayfish.guns.item.attachment.impl.ITactical;
import com.mrcrayfish.guns.item.attachment.impl.create.Tactical;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A basic under barrel attachment item implementation with color support
 *
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

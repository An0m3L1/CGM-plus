package com.mrcrayfish.guns.item.curio;

import com.mrcrayfish.guns.GunMod;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class AccessoryItem extends Item
{
    public AccessoryItem(Item.Properties properties) {
        super(properties.stacksTo(1).tab(GunMod.GUNS));
    }

    public AccessoryItem() {
        this(new Item.Properties());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Objects.requireNonNull(ChatFormatting.YELLOW.getColor());
    }
}

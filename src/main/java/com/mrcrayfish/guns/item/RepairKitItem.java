package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.GunMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class RepairKitItem extends Item
{
    public RepairKitItem(Properties properties)
    {
        super(properties.stacksTo(16).tab(GunMod.GUNS));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
    {
        tooltip.add(Component.translatable("info.cgm.repair_kit").withStyle(ChatFormatting.GOLD));
    }
}

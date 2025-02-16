package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.entity.ThrowableGrenadeEntity;
import com.mrcrayfish.guns.entity.ThrowableStunGrenadeEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Scanner;

/**
 * Author: MrCrayfish
 */
public class StunGrenadeItem extends GrenadeItem
{
    public StunGrenadeItem(Item.Properties properties, int maxCookTime)
    {
        super(properties, maxCookTime);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
    {
        tooltip.add(Component.translatable("info.cgm.gun_details").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        float maxBlind = (float) (Config.COMMON.stunGrenades.blind.criteria.durationMax.get()) / 20;
        float maxDeafen = (float) (Config.COMMON.stunGrenades.deafen.criteria.durationMax.get()) / 20;
        float cookTime = (float) maxCookTime / 20;
        tooltip.add(Component.translatable("info.cgm.blind", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxBlind)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.cgm.deaf", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxDeafen)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("info.cgm.fuse", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(cookTime)).withStyle(ChatFormatting.GRAY));
     }

    @Override
    public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft)
    {
        return new ThrowableStunGrenadeEntity(world, entity, timeLeft);
    }
}

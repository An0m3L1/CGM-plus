package com.mrcrayfish.guns.item.grenade;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.entity.grenade.ThrowableGrenadeEntity;
import com.mrcrayfish.guns.entity.grenade.ThrowableStunGrenadeEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

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
        float maxBlind = (float) (Config.COMMON.blindCriteria.durationMax.get());
        float maxDeafen = (float) (Config.COMMON.stunCriteria.durationMax.get());
        float cookTime = (float) maxCookTime / 20;
        if(Screen.hasControlDown())
        {
            tooltip.add(Component.translatable("info.cgm.stats").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("info.cgm.blind", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxBlind)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("info.cgm.stun", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxDeafen)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("info.cgm.fuse", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(cookTime)).withStyle(ChatFormatting.GRAY));
        }
        else
        {
            tooltip.add(Component.translatable("info.cgm.stats_help").withStyle(ChatFormatting.GOLD));
        }
     }

    @Override
    public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft)
    {
        return new ThrowableStunGrenadeEntity(world, entity, timeLeft);
    }
}

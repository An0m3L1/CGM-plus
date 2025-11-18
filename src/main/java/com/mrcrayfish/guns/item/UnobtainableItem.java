package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class UnobtainableItem extends Item
{
    public UnobtainableItem(Item.Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
    {
        tooltip.add(Component.translatable("info.cgm.unobtainable").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack)
    {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack)
    {
        return 10;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn)
    {
        ItemStack stack = playerIn.getItemInHand(handIn);
        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving)
    {
        if(!worldIn.isClientSide())
        {
            if(!(entityLiving instanceof Player) || !((Player) entityLiving).isCreative())
                stack.shrink(1);
            if(entityLiving instanceof Player)
            {
                ((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
            }
        }
        worldIn.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), ModSounds.PARTY_HORN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return stack;
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> item) {}
}

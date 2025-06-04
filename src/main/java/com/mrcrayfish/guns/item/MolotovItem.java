package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.entity.ThrowableMolotovEntity;
import com.mrcrayfish.guns.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class MolotovItem extends GrenadeItem
{
    public MolotovItem(Properties properties, int maxCookTime)
    {
        super(properties, maxCookTime);
    }

    @Override
    public boolean canCook()
    {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
    {
        double fireDiameter = (Config.COMMON.explosives.molotovExplosionRadius.get() * 2F);
        int fireDuration = Config.COMMON.explosives.molotovFireDuration.get();
        if(Screen.hasControlDown())
        {
            tooltip.add(Component.translatable("info.cgm.stats").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("info.cgm.fire_diameter", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(fireDiameter)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("info.cgm.fire_duration", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(fireDuration)).withStyle(ChatFormatting.GRAY));
        }
        else
        {
            tooltip.add(Component.translatable("info.cgm.stats_help").withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count)
    {
        int duration = this.getUseDuration(stack) - count;
        if(duration == 9)
            player.level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_GRENADE_LIGHT.get(), SoundSource.PLAYERS, 2.5F, 1.0F, false);
    }

    @Override
    public ThrowableMolotovEntity create(Level world, LivingEntity entity, int timeLeft)
    {
        return new ThrowableMolotovEntity(world, entity, timeLeft);
    }
}

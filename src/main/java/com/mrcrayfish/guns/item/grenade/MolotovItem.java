package com.mrcrayfish.guns.item.grenade;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.entity.grenade.ThrowableMolotovEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
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
    public MolotovItem(Properties properties, int maxCookTime, SoundEvent throwSound, SoundEvent pinSound)
    {
        super(properties, maxCookTime, throwSound, pinSound);
    }

    @Override
    public boolean canCook()
    {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
    {
        double fireRadius = (Config.COMMON.molotovExplosionRadius.get());
        int fireDuration = Config.COMMON.molotovFireDuration.get();
        if(Screen.hasControlDown())
        {
            tooltip.add(Component.translatable("info.cgm.stats").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("info.cgm.fire_radius", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(fireRadius)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("info.cgm.fire_duration", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(fireDuration)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("info.cgm.explodes_on_impact").withStyle(ChatFormatting.GRAY));
        }
        else
        {
            tooltip.add(Component.translatable("info.cgm.stats_help").withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public ThrowableMolotovEntity create(Level world, LivingEntity entity, int timeLeft)
    {
        return new ThrowableMolotovEntity(world, entity, timeLeft);
    }
}

package com.mrcrayfish.guns.item.grenade;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.entity.grenade.ThrowableGrenadeEntity;
import com.mrcrayfish.guns.entity.grenade.ThrowableImpactGrenadeEntity;
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
public class ImpactGrenadeItem extends GrenadeItem
{
    public ImpactGrenadeItem(Properties properties, int maxCookTime)
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
        double damage = Config.COMMON.impactGrenadeExplosionDamage.get();
        if(Screen.hasControlDown())
        {
            tooltip.add(Component.translatable("info.cgm.stats").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("info.cgm.damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("info.cgm.explodes_on_impact").withStyle(ChatFormatting.GRAY));
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
        if(duration == 10)
            player.level.playLocalSound(player.getX(), player.getY(), player.getZ(), ModSounds.GRENADE_PIN.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    @Override
    public ThrowableGrenadeEntity create(Level world, LivingEntity entity, int timeLeft)
    {
        return new ThrowableImpactGrenadeEntity(world, entity, timeLeft);
    }
}

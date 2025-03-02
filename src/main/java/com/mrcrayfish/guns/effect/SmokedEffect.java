package com.mrcrayfish.guns.effect;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.init.ModEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SmokedEffect extends IncurableEffect
{
    public SmokedEffect(MobEffectCategory typeIn, int liquidColorIn)
    {
        super(typeIn, liquidColorIn);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier)
    {
        double damage = Config.COMMON.explosives.smokeGrenadeDamage.get();
        if (!entity.getCommandSenderWorld().isClientSide && entity.hasEffect(ModEffects.SMOKED.get()))
        {
            if(entity.getHealth() > 1.0F)
            {
                entity.hurt(DamageSource.MAGIC, (float) damage);
            }
            if(entity instanceof Mob mob)
            {
                mob.setTarget(null);
            }
        }
    }

    public boolean isDurationEffectTick(int duration, int amplifier)
    {
        int i;
        i = 25 >> amplifier;
        if (i > 0)
        {
            return duration % i == 0;
        }
        else
        {
            return true;
        }
    }
}

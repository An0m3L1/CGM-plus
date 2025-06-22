package com.mrcrayfish.guns.effect;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.init.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

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
        double damage = Config.COMMON.smokeGrenadeDamage.get();
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
        i = 20 >> amplifier;
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

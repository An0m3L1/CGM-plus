package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.effect.IncurableEffect;
import com.mrcrayfish.guns.effect.SmokedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModEffects
{
    public static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Reference.MOD_ID);

    public static final RegistryObject<IncurableEffect> BLINDED = REGISTER.register("blinded", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<IncurableEffect> STUNNED = REGISTER.register("stunned", () -> new IncurableEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<SmokedEffect> SMOKED = REGISTER.register("smoked", () -> new SmokedEffect(MobEffectCategory.HARMFUL, 0));
}

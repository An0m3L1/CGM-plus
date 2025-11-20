package com.mrcrayfish.guns.item.attachment.impl.create;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.interfaces.IGunModifier;
import com.mrcrayfish.guns.item.attachment.impl.IAttachment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * The base attachment object
 *
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT)
public abstract class Attachment
{
    protected IGunModifier[] modifiers;
    private List<Component> perks = null;

    Attachment(IGunModifier... modifiers)
    {
        this.modifiers = modifiers;
    }

    public IGunModifier[] getModifiers()
    {
        return this.modifiers;
    }

    void setPerks(List<Component> perks)
    {
        if(this.perks == null)
        {
            this.perks = perks;
        }
    }

    List<Component> getPerks()
    {
        return this.perks;
    }

    /* Determines the perks of attachments and caches them */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void addInformationEvent(ItemTooltipEvent event)
    {
        ItemStack stack = event.getItemStack();
        if(stack.getItem() instanceof IAttachment<?>)
        {
            IAttachment<?> attachment = (IAttachment<?>) stack.getItem();
            List<Component> perks = attachment.getProperties().getPerks();
            if(perks != null && perks.size() > 0)
            {
                if(Screen.hasControlDown())
                {
                    event.getToolTip().add(Component.translatable("info.cgm.stats").withStyle(ChatFormatting.GOLD));
                    event.getToolTip().addAll(perks);
                }
                else
                {
                    event.getToolTip().add(Component.translatable("info.cgm.stats_help").withStyle(ChatFormatting.GOLD));
                }
                return;
            }

            IGunModifier[] modifiers = attachment.getProperties().getModifiers();
            float thisOutput = 0F;
            float thisInput = 0F;
            List<Component> positivePerks = new ArrayList<>();
            List<Component> negativePerks = new ArrayList<>();
            List<Component> perkType;
            String perkDescription = "";

            /* Test for fire sound volume */
            float inputSound = 1.0F;
            float outputSound = inputSound;
            for(IGunModifier modifier : modifiers)
            {
                outputSound = modifier.modifyFireSoundVolume(outputSound);
            }
            thisOutput = (float) outputSound;
            thisInput = (float) inputSound;
            if (thisOutput != inputSound)
            {
	            perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput < thisInput;
                float modifierValue = toPercent(thisOutput);
	            //perkDescription = (isPositive ? "perk.cgm.fire_volume.positive" : "perk.cgm.fire_volume.negative");
                perkDescription = "perk.cgm.fire_volume";
	            
	            addPerk(perkType, isPositive, true, Math.round(modifierValue*100)/100, perkDescription);
            }

            /* Test for silenced */
            for(IGunModifier modifier : modifiers)
            {
                if(modifier.silencedFire())
                {
                    addPerk(positivePerks, true, "perk.cgm.silenced.positive");
                    break;
                }
            }

            /* Test for flash size */
            double inputFlash = 10.0;
            double outputFlash = inputFlash;
            for(IGunModifier modifier : modifiers)
            {
                outputFlash = modifier.modifyMuzzleFlashScale(outputFlash);
            }
            thisOutput = (float) outputFlash;
            thisInput = (float) inputFlash;
            if (thisOutput != thisInput)
            {
                perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput < thisInput;
                float modifierValue = toPercent(thisOutput);
                perkDescription = "perk.cgm.flash";

                addPerk(perkType, isPositive, true, Math.round(modifierValue*100)/100, perkDescription);
            }

            /* Test for Light Magazine */
            for(IGunModifier modifier : modifiers)
            {
                if(modifier.lightMag())
                {
                    addPerk(positivePerks, false, "perk.cgm.mag");
                    addPerk(positivePerks, true, "perk.cgm.reload");
                    break;
                }
            }

            /* Test for Extended Magazine */
            for(IGunModifier modifier : modifiers)
            {
                if(modifier.extMag())
                {
                    addPerk(positivePerks, true, "perk.cgm.mag");
                    addPerk(positivePerks, false, "perk.cgm.reload");
                    break;
                }
            }

            /* Test for sound radius */
            double inputRadius = 10.0;
            double outputRadius = inputRadius;
            for(IGunModifier modifier : modifiers)
            {
                outputRadius = modifier.modifyFireSoundRadius(outputRadius);
            }
            thisOutput = (float) outputRadius;
            thisInput = (float) inputRadius;
            if (thisOutput != thisInput)
            {
	            perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput < thisInput;
                float modifierValue = toPercent(thisOutput);
                perkDescription = "perk.cgm.sound_radius";
	            
	            addPerk(perkType, isPositive, true, Math.round(modifierValue*100)/100, perkDescription);
            }

            /* Test for additional damage */
            float additionalDamage = 0.0F;
            for(IGunModifier modifier : modifiers)
            {
                additionalDamage += modifier.additionalDamage();
            }
            if(additionalDamage > 0.0F)
            {
                addPerk(positivePerks, true, "perk.cgm.additional_damage.positive", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage / 2.0));
            }
            else if(additionalDamage < 0.0F)
            {
                addPerk(negativePerks, false, "perk.cgm.additional_damage.negative", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage / 2.0));
            }

            /* Test for modified damage */
            float inputDamage = 10.0F;
            float outputDamage = inputDamage;
            for(IGunModifier modifier : modifiers)
            {
                outputDamage = modifier.modifyProjectileDamage(outputDamage);
            }
            thisOutput = (float) outputDamage;
            thisInput = (float) inputDamage;
            if (thisOutput != thisInput)
            {
	            perkType = (thisOutput >= thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput > thisInput;
                float modifierValue = toPercent(thisOutput);
	            //perkDescription = (isPositive ? "perk.cgm.modified_damage.positive" : "perk.cgm.modified_damage.negative");
                perkDescription = "perk.cgm.modified_damage";
	            
	            addPerk(perkType, isPositive, false, Math.round(modifierValue*100)/100, perkDescription);
            }

            /* Test for modified projectile speed */
            double inputSpeed = 10.0;
            double outputSpeed = inputSpeed;
            for(IGunModifier modifier : modifiers)
            {
                outputSpeed = modifier.modifyProjectileSpeed(outputSpeed);
            }
            thisOutput = (float) outputSpeed;
            thisInput = (float) inputSpeed;
            if (thisOutput != thisInput)
            {
	            perkType = (thisOutput >= thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput > thisInput;
                float modifierValue = toPercent(thisOutput);
	            //perkDescription = (isPositive ? "perk.cgm.projectile_speed.positive" : "perk.cgm.projectile_speed.negative");
                perkDescription = "perk.cgm.projectile_speed";
	            
	            addPerk(perkType, isPositive, false, Math.round(modifierValue*100)/100, perkDescription);
            }

            /* Test for modified projectile spread */
            float inputSpread = 10.0F;
            float outputSpread = inputSpread;
            for(IGunModifier modifier : modifiers)
            {
                outputSpread = modifier.modifyProjectileSpread(outputSpread);
            }
            thisOutput = (float) outputSpread;
            thisInput = (float) inputSpread;
            if (thisOutput != thisInput)
            {
	            perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput < thisInput;
                float modifierValue = toPercent(thisOutput);
	            //perkDescription = (isPositive ? "perk.cgm.projectile_spread.positive" : "perk.cgm.projectile_spread.negative");
                perkDescription = "perk.cgm.projectile_spread";
	            
	            addPerk(perkType, isPositive, true, Math.round(modifierValue*100)/100, perkDescription);
            }

            /* Test for modified projectile life */
            int inputLife = 100;
            int outputLife = inputLife;
            for(IGunModifier modifier : modifiers)
            {
                outputLife = modifier.modifyProjectileLife(outputLife);
            }
            thisOutput = (float) outputLife;
            thisInput = (float) inputLife;
            if (thisOutput != thisInput)
            {
	            perkType = (thisOutput >= thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput > thisInput;
                float modifierValue = toPercent(thisOutput/10F);
	            //perkDescription = (isPositive ? "perk.cgm.projectile_life.positive" : "perk.cgm.projectile_life.negative");
                perkDescription = "perk.cgm.projectile_life";
	            
	            addPerk(perkType, isPositive, false, Math.round(modifierValue*100)/100, perkDescription);
            }

            /* Test for modified recoil */
            float inputRecoil = 10.0F;
            float outputRecoil = inputRecoil;
            for(IGunModifier modifier : modifiers)
            {
                outputRecoil *= modifier.recoilModifier();
            }
            thisOutput = (float) outputRecoil;
            thisInput = (float) inputRecoil;
            if (thisOutput != thisInput)
            {
	            perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput < thisInput;
                float modifierValue = toPercent(thisOutput);
	            //perkDescription = (isPositive ? "perk.cgm.recoil.positive" : "perk.cgm.recoil.negative");
                perkDescription = "perk.cgm.recoil";
	            
	            addPerk(perkType, isPositive, true, Math.round(modifierValue*100)/100, perkDescription);
            }

            /* Test for ADS time */
            double inputAdsTime = 10.0;
            double outputAdsTime = inputAdsTime;
            for(IGunModifier modifier : modifiers)
            {
                outputAdsTime = modifier.modifyAimDownSightSpeed(outputAdsTime);
            }
            thisOutput = (float) outputAdsTime;
            thisInput = (float) inputAdsTime;
            if (thisOutput != thisInput)
            {
            	perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput > thisInput;
                float modifierValue = toPercent(thisOutput);
            	perkDescription = (isPositive ? "perk.cgm.ads_time.positive" : "perk.cgm.ads_time.negative");
                perkDescription = "perk.cgm.ads_time";
	            
	            addPerk(perkType, isPositive, true, Math.round(modifierValue*100)/100, perkDescription);
        	}

            /* Test for fire rate */
            int inputRate = 10;
            int outputRate = inputRate;
            for(IGunModifier modifier : modifiers)
            {
                outputRate = modifier.modifyFireRate(outputRate);
            }
            thisOutput = (float) outputRate;
            thisInput = (float) inputRate;
            if (thisOutput != thisInput)
            {
                perkType = (thisOutput < thisInput ? positivePerks : negativePerks);
                boolean isPositive = thisOutput < thisInput;
                float modifierValue = toPercent(thisOutput);
                //perkDescription = (isPositive ? "perk.cgm.rate.positive" : "perk.cgm.rate.negative");
                perkDescription = "perk.cgm.rate";
	            
                addPerk(perkType, isPositive, true, Math.round(modifierValue*100)/100, perkDescription);
        	}

            positivePerks.addAll(negativePerks);
            attachment.getProperties().setPerks(positivePerks);
            if(positivePerks.size() > 0)
            {
                event.getToolTip().add(Component.translatable("info.cgm.stats").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
                event.getToolTip().addAll(positivePerks);
            }
        }
    }
    
    private static float toPercent(float input)
    {
    	return Math.round((input)*10)-100;
    }

    private static void addPerk(List<Component> components, boolean positive, String id, Object... params)
    {
        components.add(Component.translatable(positive ? "perk.cgm.entry.positive" : "perk.cgm.entry.negative", Component.translatable(id, params).withStyle(ChatFormatting.GRAY)).withStyle(positive ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private static void addPerk(List<Component> components, boolean positive, float value, String id, Object... params)
    {
        components.add(Component.translatable(positive ? "perk.cgm.entry.positive" : "perk.cgm.entry.negative", Component.translatable(id, params).withStyle(ChatFormatting.GRAY).append(Component.literal(" ("+Math.abs(value)+"%)").withStyle(ChatFormatting.GRAY))).withStyle(positive ? ChatFormatting.DARK_AQUA : ChatFormatting.GOLD));
    }

    private static void addPerk(List<Component> components, boolean positive, boolean invert, float value, String id, Object... params)
    {
    	boolean truePositive = (invert ? !positive : positive);
    	components.add(Component.literal((truePositive ? "+" : "-")+Math.abs(value)+"% ").withStyle(positive ? ChatFormatting.GREEN : ChatFormatting.RED).append(Component.translatable(id, params).withStyle(ChatFormatting.GRAY)));
    }
}

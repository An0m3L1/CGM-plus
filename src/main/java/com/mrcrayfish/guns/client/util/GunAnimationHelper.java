package com.mrcrayfish.guns.client.util;

import com.mrcrayfish.guns.cache.ObjectCache;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.interfaces.IGunModifier;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.attachment.IAttachment;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for more complex gun animations, including attachment and hand movements
 */
public final class GunAnimationHelper
{
    public static final String CACHE_KEY = "properties";

    public static void resetCache()
    {
        ObjectCache.getInstance(CACHE_KEY).reset();
    }
    
	public static float getAnimationValue(ItemStack weapon, float progress)
	{
		float cooldownDivider = PropertyHelper.getCooldownDivider(weapon);
        float cooldownOffset = PropertyHelper.getCooldownOffset(weapon);
        float intensity = PropertyHelper.getAnimIntensity(weapon) +1;
        
        float cooldown = progress*cooldownDivider;
        float cooldown_a = cooldown-cooldownOffset;

        float cooldown_b = Math.min(Math.max(cooldown_a*intensity,0),1);
        float cooldown_c = Math.min(Math.max((-cooldown_a*intensity)+intensity,0),1);
        float cooldown_d = Math.min(cooldown_b,cooldown_c);
        
        return cooldown_d;
	}
	
	public static float getBoltAnimationValue(ItemStack weapon, float progress)
	{
		float cooldownDivider = PropertyHelper.getCooldownDivider(weapon);
        float cooldownOffset = PropertyHelper.getCooldownOffset(weapon);
        float intensity = PropertyHelper.getAnimIntensity(weapon) +1;
        float boltLeadTime = PropertyHelper.getBoltLeadTime(weapon);
        
        float cooldown = progress*cooldownDivider;
        float cooldown_a = cooldown-cooldownOffset;

        float cooldown_b = Math.min(Math.max(cooldown_a*intensity+boltLeadTime,0),1);
        float cooldown_c = Math.min(Math.max((-cooldown_a*intensity+boltLeadTime)+intensity,0),1);
        float cooldown_d = Math.min(cooldown_b,cooldown_c);
        return cooldown_d;
	}
	
	public static float getHandBoltAnimationValue(ItemStack weapon, boolean isRearHand, float progress)
	{
		float cooldownDivider = PropertyHelper.getCooldownDivider(weapon);
        float cooldownOffset = PropertyHelper.getCooldownOffset(weapon);
        float intensity = PropertyHelper.getAnimIntensity(weapon) +1;
        float boltLeadTime = PropertyHelper.getHandBoltLeadTime(weapon, isRearHand);
        
        float cooldown = progress*cooldownDivider;
        float cooldown_a = cooldown-cooldownOffset;

        float cooldown_b = Math.min(Math.max(cooldown_a*intensity+boltLeadTime,0),1);
        float cooldown_c = Math.min(Math.max((-cooldown_a*intensity+boltLeadTime)+intensity,0),1);
        float cooldown_d = Math.min(cooldown_b,cooldown_c);
        return cooldown_d;
	}
	
	protected static Vec3 applyAnimation(ItemStack weapon, Vec3 input, float progress)
	{
		return input.scale(getAnimationValue(weapon, progress));
		//return new Vec3(input.x*applyAnimation(weapon,progress),input.y*applyAnimation(weapon,progress),input.z*applyAnimation(weapon,progress));
	}
	
	protected static Vec3 applyBoltAnimation(ItemStack weapon, Vec3 input, float progress)
	{
		return input.scale(getBoltAnimationValue(weapon, progress));
	}
	
	protected static Vec3 applyHandBoltAnimation(ItemStack weapon, Vec3 input, boolean isRearHand, float progress)
	{
		return input.scale(getHandBoltAnimationValue(weapon, isRearHand, progress));
	}
	
	public static Vec3 getAttachmentTranslation(ItemStack weapon, IAttachment.Type type, float progress)
	{
		if (PropertyHelper.hasAttachmentAnimation(weapon, type))
		{
			Vec3 animation = PropertyHelper.getAttachmentAnimTranslation(weapon, type);
			animation = applyAnimation(weapon, animation, progress);
			return animation;
		}
		return Vec3.ZERO;
	}
	
	public static Vec3 getHandTranslation(ItemStack weapon, boolean isRearHand, float progress)
	{
		if (PropertyHelper.hasHandAnimation(weapon, isRearHand))
		{
			Vec3 anim1 = PropertyHelper.getHandAnimationTranslation(weapon, isRearHand, false);
			Vec3 anim2 = PropertyHelper.getHandAnimationTranslation(weapon, isRearHand, true);
			
			anim1 = applyAnimation(weapon,anim1,progress);
			anim2 = applyHandBoltAnimation(weapon, anim2, isRearHand, progress);
			
			Vec3 animation = anim1.add(anim2);
			return animation.scale(PropertyHelper.getHandAnimScalar(weapon));
		}
		return Vec3.ZERO;
	}
}
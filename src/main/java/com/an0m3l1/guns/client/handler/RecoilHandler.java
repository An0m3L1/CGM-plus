package com.an0m3l1.guns.client.handler;

import com.an0m3l1.guns.client.util.Easings;
import com.an0m3l1.guns.common.Gun;
import com.an0m3l1.guns.event.GunFireEvent;
import com.an0m3l1.guns.item.GunItem;
import com.an0m3l1.guns.util.GunCompositeStatHelper;
import com.an0m3l1.guns.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * Author: MrCrayfish
 */
public class RecoilHandler
{
	private static RecoilHandler instance;
	
	public static RecoilHandler get()
	{
		if(instance == null)
		{
			instance = new RecoilHandler();
		}
		return instance;
	}
	
	private final Random random = new Random();
	private double gunRecoilNormal;
	private double gunRecoilAngle;
	
	private float gunRecoilRandom;
	private float gunVRecoilRandom;
	
	private float cameraRecoil;
	private float progressCameraRecoil;
	
	private float currentCameraVRecoil;
	private float prevCameraVRecoil;
	private float lastShotVRecoil;
	
	private float currentCameraHRecoil;
	private float prevCameraHRecoil;
	private float lastShotHRecoil;
	private float hRecoilPushForce;
	private float lastHRecoilPushForce;
	
	private int lastFireTick = -1;
	private double recoilBuildup = 0;
	
	private RecoilHandler()
	{
	}
	
	@SubscribeEvent
	public void onGunFire(GunFireEvent.Pre event)
	{
		if(event.isNotClient())
		{
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null)
		{
			return;
		}
		
		ItemStack heldItem = event.getStack();
		GunItem gunItem = (GunItem) heldItem.getItem();
		Gun modifiedGun = gunItem.getModifiedGun(heldItem);
		float recoilModifier = 1.0F - GunModifierHelper.getRecoilModifier(heldItem);
		recoilModifier *= (float) this.getAdsRecoilReduction(modifiedGun);
		this.cameraRecoil = modifiedGun.getGeneral().getRecoilAngle() * recoilModifier;
		this.gunRecoilRandom = random.nextFloat();
		this.gunVRecoilRandom = random.nextFloat();
		
		this.lastShotVRecoil = currentCameraVRecoil;
		this.lastShotHRecoil = currentCameraHRecoil;
		//this.lastVRecoilPushForce = vRecoilPushForce;
		this.lastHRecoilPushForce = hRecoilPushForce * 0.6F;
		
		float timeSinceLastShot = mc.player.tickCount - this.lastFireTick;
		double fireRate = GunCompositeStatHelper.getCompositeBaseRate(heldItem, modifiedGun);
		double scaledRecoilRate = Math.min(fireRate / 18, 1);
		double maxTimeBetweenShots = (10 - 1) + fireRate;
		if(timeSinceLastShot <= maxTimeBetweenShots)
		{
			if(recoilBuildup > 0 && timeSinceLastShot > 4)
			{
				recoilBuildup = Math.max(Mth.lerp(Easings.EASE_IN_OUT_SIN.apply((timeSinceLastShot) / maxTimeBetweenShots), recoilBuildup, 0), 0);
			}
			
			recoilBuildup = Math.min(recoilBuildup + scaledRecoilRate, 1);
		}
		else
		{
			recoilBuildup = 0;
			//this.lastVRecoilPushForce = 0;
			this.lastHRecoilPushForce = 0;
		}
		
		this.lastFireTick = mc.player.tickCount;
		
		if(this.progressCameraRecoil != 0F)
		{
			this.progressCameraRecoil = 0F;
		}
	}
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event)
	{
		if(event.phase != TickEvent.Phase.END || this.cameraRecoil <= 0)
		{
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null || mc.player.isDeadOrDying())
		{
			lastFireTick = -1;
			cameraRecoil = 0;
			return;
		}
		if(mc.isPaused())
		{
			return;
		}
		
		float cameraVAngleChange = 0;
		float cameraHAngleChange = 0;
		
		//New camera recoil method.
		if(lastFireTick != -1)
		{
			int currentRecoilTick = mc.player.tickCount - this.lastFireTick;
			
			float prevVRecoil = lastShotVRecoil - Math.min(lastShotVRecoil * 0.2F, 0.1F);
			float stackedVRecoil = cameraRecoil + lastShotVRecoil;
			float maxRecoilScaling = 20;
			float targetVRecoil = cameraRecoil != 0 ? Mth.lerp(Easings.EASE_OUT_QUAD.apply((stackedVRecoil / cameraRecoil) / maxRecoilScaling), 0, cameraRecoil * (maxRecoilScaling / 2.1F)) : cameraRecoil;
			
			float maxRecoilVariance = 3.0F;
			float recoilVarianceFraction = 1.0F;
			float recoilVariance = Mth.clamp(cameraRecoil * recoilVarianceFraction, -maxRecoilVariance, maxRecoilVariance);
			
			float vRecoilRandom = Mth.clamp(1F - (gunVRecoilRandom * 2.0F), -1, 1);
			float vRecoilPushForce = Mth.clamp(((Math.min(Easings.EASE_IN_QUAD.apply((float) recoilBuildup), 1) * vRecoilRandom) * 0.3F), -recoilVariance / 4F, recoilVariance / 4F);
			targetVRecoil += Mth.clamp(vRecoilPushForce, -recoilVariance, recoilVariance);
			
			float hRecoilRandom = Mth.clamp(1F - (gunRecoilRandom * 2.0F), -1, 1);
			hRecoilPushForce = Mth.clamp((lastHRecoilPushForce + (Math.min(Easings.EASE_IN_QUAD.apply((float) recoilBuildup), 1) * hRecoilRandom) / 1.5F), -recoilVariance / 3F, recoilVariance / 3F);
			float clampedLastShotHRecoil = Mth.clamp(lastShotHRecoil, -recoilVariance, recoilVariance);
			float targetHRecoil = clampedLastShotHRecoil + hRecoilPushForce;
			
			double recoilUpwardTime = (Math.abs(targetVRecoil - prevVRecoil) / 6) + 2;
			double recoilDownwardTime = ((targetVRecoil / 4) + 8);
			
			float recoilTime = (float) Math.min(((float) currentRecoilTick) + mc.getPartialTick(), recoilUpwardTime + recoilDownwardTime);
			
			if(recoilTime <= recoilUpwardTime)
			{
				currentCameraVRecoil = (float) Mth.lerp(Easings.EASE_OUT_QUAD.apply(recoilTime / recoilUpwardTime), prevVRecoil, targetVRecoil);
				currentCameraHRecoil = (float) Mth.lerp(Easings.EASE_OUT_QUAD.apply(recoilTime / recoilUpwardTime), lastShotHRecoil, targetHRecoil);
			}
			else
			{
				currentCameraVRecoil = (float) Mth.lerp(Easings.EASE_IN_OUT_SIN.apply((recoilTime - recoilUpwardTime) / recoilDownwardTime), targetVRecoil, 0);
				currentCameraHRecoil = (float) Mth.lerp(Easings.EASE_IN_OUT_QUAD.apply((recoilTime - recoilUpwardTime) / recoilDownwardTime), targetHRecoil, 0);
			}
			
			cameraVAngleChange = prevCameraVRecoil - currentCameraVRecoil;
			cameraHAngleChange = prevCameraHRecoil - currentCameraHRecoil;
			
			prevCameraVRecoil = currentCameraVRecoil;
			prevCameraHRecoil = currentCameraHRecoil;
			
			if(recoilTime > recoilUpwardTime + recoilDownwardTime)
			{
				this.lastFireTick = -1;
				this.cameraRecoil = 0;
			}
		}
		
		float pitch = mc.player.getXRot();
		mc.player.setXRot(pitch + cameraVAngleChange);
		float yaw = mc.player.getYRot();
		mc.player.setYRot(yaw + cameraHAngleChange);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderOverlay(RenderHandEvent event)
	{
		if(event.getHand() != InteractionHand.MAIN_HAND)
		{
			return;
		}
		
		ItemStack heldItem = event.getItemStack();
		if(!(heldItem.getItem() instanceof GunItem gunItem))
		{
			return;
		}
		
		Gun modifiedGun = gunItem.getModifiedGun(heldItem);
		assert Minecraft.getInstance().player != null;
		ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
		float cooldown = tracker.getCooldownPercent(gunItem, Minecraft.getInstance().getFrameTime());
		cooldown = cooldown >= modifiedGun.getGeneral().getRecoilDurationOffset() ? (cooldown - modifiedGun.getGeneral().getRecoilDurationOffset()) / (1.0F - modifiedGun.getGeneral().getRecoilDurationOffset()) : 0.0F;
		if(cooldown >= 0.85)
		{
			float amount = ((1.0F - (cooldown * 0.98F)) / 0.2F);
			this.gunRecoilNormal = 1 - (--amount) * amount * amount * amount;
		}
		else
		{
			float amount = (cooldown / 0.85F);
			this.gunRecoilNormal = amount < 0.5 ? 2 * amount * amount : -1 + (4 - 2 * amount) * amount;
		}
		
		this.gunRecoilAngle = modifiedGun.getGeneral().getRecoilAngle();
	}
	
	public double getAdsRecoilReduction(Gun gun)
	{
		return 1.0 - gun.getGeneral().getRecoilAdsReduction() * AimingHandler.get().getNormalisedAdsProgress();
	}
	
	public double getRecoilBuildup()
	{
		return this.recoilBuildup;
	}
	
	public float getCurrentVRecoil(float partialTicks)
	{
		return Mth.lerp(partialTicks, prevCameraVRecoil, currentCameraVRecoil);
	}
	
	public float getCurrentHRecoil(float partialTicks)
	{
		return Mth.lerp(partialTicks, prevCameraHRecoil, currentCameraHRecoil);
	}
	
	public double getGunRecoilNormal()
	{
		return this.gunRecoilNormal;
	}
	
	public double getGunRecoilAngle()
	{
		return this.gunRecoilAngle;
	}
	
	public float getGunRecoilRandom()
	{
		return this.gunRecoilRandom;
	}
}

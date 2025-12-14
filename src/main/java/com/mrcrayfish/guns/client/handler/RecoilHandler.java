package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.util.Easings;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;
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

    private Random random = new Random();
    private double gunRecoilNormal;
    private double gunRecoilAngle;

    private float gunRecoilRandom;
    private float gunVRecoilRandom;

    private float cameraRecoil;
    private float progressCameraRecoil;

    private float currentCameraVRecoil;
    private float prevCameraVRecoil;
    private float lastShotVRecoil;
    private float vRecoilPushForce;
    //private float lastVRecoilPushForce;

    private float currentCameraHRecoil;
    private float prevCameraHRecoil;
    private float lastShotHRecoil;
    private float hRecoilPushForce;
    private float lastHRecoilPushForce;

    private float maxRecoilScaling = 20;
    private int lastFireTick = -1;
    private double recoilBuildup = 0;
    private float recoilVarianceFraction = 1.0F;
    private float maxRecoilVariance = 3.0F;

    private RecoilHandler() {}

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Pre event)
    {
        if(!event.isClient())
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        ItemStack heldItem = event.getStack();
        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        float recoilModifier = 1.0F - GunModifierHelper.getRecoilModifier(heldItem);
        recoilModifier *= this.getAdsRecoilReduction(modifiedGun);
        this.cameraRecoil = modifiedGun.getGeneral().getRecoilAngle() * recoilModifier;
        this.gunRecoilRandom = random.nextFloat();
        this.gunVRecoilRandom = random.nextFloat();

        this.lastShotVRecoil = currentCameraVRecoil;
        this.lastShotHRecoil = currentCameraHRecoil;
        //this.lastVRecoilPushForce = vRecoilPushForce;
        this.lastHRecoilPushForce = hRecoilPushForce*0.6F;

        float timeSinceLastShot = mc.player.tickCount - this.lastFireTick;
        double fireRate = GunCompositeStatHelper.getCompositeBaseRate(heldItem, modifiedGun);
        double scaledRecoilRate = Math.min(fireRate/18,1);
        double maxTimeBetweenShots = (10-1)+fireRate;
        if (timeSinceLastShot<=maxTimeBetweenShots)
        {
            if (recoilBuildup>0 && timeSinceLastShot>4)
                recoilBuildup = Math.max(Mth.lerp(Easings.EASE_IN_OUT_SIN.apply((timeSinceLastShot)/maxTimeBetweenShots), recoilBuildup, 0), 0);

            recoilBuildup = Math.min(recoilBuildup+scaledRecoilRate,1);
        }
        else
        {
            recoilBuildup=0;
            //this.lastVRecoilPushForce = 0;
            this.lastHRecoilPushForce = 0;
        }

        this.lastFireTick = mc.player.tickCount;

        if (this.progressCameraRecoil != 0F)
            this.progressCameraRecoil = 0F;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END || this.cameraRecoil <= 0)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.player.isDeadOrDying())
        {
            lastFireTick = -1;
            cameraRecoil = 0;
            return;
        }
        if(mc.isPaused())
            return;

        float cameraVAngleChange = 0;
        float cameraHAngleChange = 0;

        if(!Config.CLIENT.useOldCameraRecoil.get())
        {
            //New camera recoil method.
            if (lastFireTick != -1)
            {
                int currentRecoilTick = mc.player.tickCount - this.lastFireTick;

                float prevVRecoil = lastShotVRecoil-Math.min(lastShotVRecoil*0.2F,0.1F);
                float stackedVRecoil = cameraRecoil + lastShotVRecoil;
                float targetVRecoil = cameraRecoil != 0 ? Mth.lerp(Easings.EASE_OUT_QUAD.apply((stackedVRecoil/cameraRecoil)/maxRecoilScaling),0,cameraRecoil*(maxRecoilScaling/2.1F)) : cameraRecoil;

                float recoilVariance = Mth.clamp(cameraRecoil*recoilVarianceFraction,-maxRecoilVariance,maxRecoilVariance);

                float vRecoilRandom = Mth.clamp(1F-(gunVRecoilRandom*2.0F),-1,1);
                vRecoilPushForce = Mth.clamp(((Math.min(Easings.EASE_IN_QUAD.apply((float) recoilBuildup),1)*vRecoilRandom)*0.3F),-recoilVariance/4F,recoilVariance/4F);
                targetVRecoil += Mth.clamp(vRecoilPushForce, -recoilVariance,recoilVariance);

                float hRecoilRandom = Mth.clamp(1F-(gunRecoilRandom*2.0F),-1,1);
                hRecoilPushForce = Mth.clamp((lastHRecoilPushForce+(Math.min(Easings.EASE_IN_QUAD.apply((float) recoilBuildup),1)*hRecoilRandom)/1.5F),-recoilVariance/3F,recoilVariance/3F);
                float clampedLastShotHRecoil = Mth.clamp(lastShotHRecoil, -recoilVariance/1.0F,recoilVariance/1.0F);
                float targetHRecoil = clampedLastShotHRecoil + hRecoilPushForce;

                //float hRecoilPushForce = Math.min(cameraRecoil * (Easings.EASE_IN_SIN.apply(excessRecoilRatio-1)*0.5F),cameraRecoil/2F);
                //float targetHRecoil = Mth.clamp(lastShotHRecoil + (hRecoilPushForce * Mth.clamp(2F-(gunRecoilRandom*4.0F),-1,1)) , -targetVRecoil*0.8F,targetVRecoil*0.8F);

                double recoilUpwardTime = (Math.abs(targetVRecoil-prevVRecoil)/6)+2;
                double recoilDownwardTime = ((targetVRecoil/4)+8);

                float recoilTime = (float) Math.min(((float) currentRecoilTick) + mc.getPartialTick(), recoilUpwardTime+recoilDownwardTime);

                if (recoilTime <= recoilUpwardTime)
                {
                    currentCameraVRecoil = (float) Mth.lerp(Easings.EASE_OUT_QUAD.apply(recoilTime/recoilUpwardTime), prevVRecoil, targetVRecoil);
                    currentCameraHRecoil = (float) Mth.lerp(Easings.EASE_OUT_QUAD.apply(recoilTime/recoilUpwardTime), lastShotHRecoil, targetHRecoil);

                }
                else
                {
                    currentCameraVRecoil = (float) Mth.lerp(Easings.EASE_IN_OUT_SIN.apply((recoilTime-recoilUpwardTime)/recoilDownwardTime), targetVRecoil, 0);
                    currentCameraHRecoil = (float) Mth.lerp(Easings.EASE_IN_OUT_QUAD.apply((recoilTime-recoilUpwardTime)/recoilDownwardTime), targetHRecoil, 0);
                }

                cameraVAngleChange = prevCameraVRecoil-currentCameraVRecoil;
                cameraHAngleChange = prevCameraHRecoil-currentCameraHRecoil;

                prevCameraVRecoil = currentCameraVRecoil;
                prevCameraHRecoil = currentCameraHRecoil;

                if (recoilTime > recoilUpwardTime+recoilDownwardTime)
                {
                    this.lastFireTick = -1;
                    this.cameraRecoil = 0;
                }
            }
        }
        else
        {
            // Old camera recoil method.
            float recoilAmount = this.cameraRecoil * mc.getDeltaFrameTime() * 0.15F;
            float startProgress = this.progressCameraRecoil / this.cameraRecoil;
            float endProgress = (this.progressCameraRecoil + recoilAmount) / this.cameraRecoil;


            if(startProgress < 0.2F)
            {
                cameraVAngleChange = ((endProgress - startProgress) / 0.2F) * -this.cameraRecoil;
            }
            else
            {
                cameraVAngleChange = ((endProgress - startProgress) / 0.8F) * this.cameraRecoil;
            }
	        /*float pitch = mc.player.getXRot();
	        if(startProgress < 0.2F)
	        {
	            mc.player.setXRot(pitch - ((endProgress - startProgress) / 0.2F) * this.cameraRecoil);
	        }
	        else
	        {
	            mc.player.setXRot(pitch + ((endProgress - startProgress) / 0.8F) * this.cameraRecoil);
	        }*/

            this.progressCameraRecoil += recoilAmount;

            if(this.progressCameraRecoil >= this.cameraRecoil)
            {
                this.cameraRecoil = 0;
                this.progressCameraRecoil = 0;
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
            return;

        ItemStack heldItem = event.getItemStack();
        if(!(heldItem.getItem() instanceof GunItem))
            return;

        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(gunItem, Minecraft.getInstance().getFrameTime());
        cooldown = cooldown >= modifiedGun.getGeneral().getRecoilDurationOffset() ? (cooldown - modifiedGun.getGeneral().getRecoilDurationOffset()) / (1.0F - modifiedGun.getGeneral().getRecoilDurationOffset()) : 0.0F;
        if(cooldown >= 0.85)
        {
            float amount = 1.0F * ((1.0F - (cooldown*0.98F)) / 0.2F);
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

package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.util.Easings;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
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
    private float cameraRecoil;
    private float progressCameraRecoil;
    private float actualRecoil;
    private int lastFireTick = -1;
    private int shotsFired = 0;
    private float recoilUpwardTime = 2;
    private float recoilDownwardTime = 8;

    private RecoilHandler() {}

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event)
    {
        if(!event.isClient())
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        if(!Config.SERVER.enableCameraRecoil.get())
            return;

        ItemStack heldItem = event.getStack();
        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        float recoilModifier = 1.0F - GunModifierHelper.getRecoilModifier(heldItem);
        recoilModifier *= this.getAdsRecoilReduction(modifiedGun);
        this.cameraRecoil = modifiedGun.getGeneral().getRecoilAngle() * recoilModifier;
        this.progressCameraRecoil = 0F;
        this.gunRecoilRandom = random.nextFloat();
        if (mc.player.tickCount - 5 <= this.lastFireTick || this.shotsFired == 0)
            this.shotsFired++;
        else
            this.shotsFired = 1;

        this.lastFireTick = mc.player.tickCount;
        this.actualRecoil = 0F;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END || this.cameraRecoil <= 0)
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;
        if(mc.isPaused())
            return;

        if(!Config.SERVER.enableCameraRecoil.get())
            return;

        float cameraAngleChange = 0;

        //New camera recoil
        if(!Config.CLIENT.display.useOldCameraRecoil.get())
        {
            int currentRecoilTick = mc.player.tickCount - this.lastFireTick;
            float recoilTime = ((float) currentRecoilTick) + mc.getFrameTime();
            float targetRecoil = 0;

            if (recoilTime <= recoilUpwardTime)
                targetRecoil = (Easings.EASE_OUT_QUAD.apply(recoilTime/recoilUpwardTime) * -this.cameraRecoil);
            else
                targetRecoil = (Easings.EASE_IN_OUT_QUAD.apply((-recoilTime+(recoilUpwardTime+recoilDownwardTime))/recoilDownwardTime) * -this.cameraRecoil);

            if (lastFireTick != -1)
                cameraAngleChange = (targetRecoil-actualRecoil) * (recoilTime <= recoilUpwardTime ? 1 : Math.min(this.shotsFired * 0.5F + 0.5F,2F));
            actualRecoil = targetRecoil;

            if (recoilTime >= recoilUpwardTime+recoilDownwardTime)
            {
                this.cameraRecoil = 0;
                this.shotsFired = 0;
            }
        }
        else
        //Old camera recoil
        {
            float recoilAmount = this.cameraRecoil * mc.getDeltaFrameTime() * 0.15F;
            float startProgress = this.progressCameraRecoil / this.cameraRecoil;
            float endProgress = (this.progressCameraRecoil + recoilAmount) / this.cameraRecoil;


            if(startProgress < 0.2F)
            {
                cameraAngleChange = ((endProgress - startProgress) / 0.2F) * -this.cameraRecoil;
            }
            else
            {
                cameraAngleChange = ((endProgress - startProgress) / 0.8F) * this.cameraRecoil;
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
        mc.player.setXRot(pitch + cameraAngleChange);
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

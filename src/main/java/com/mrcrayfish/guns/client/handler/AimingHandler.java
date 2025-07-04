package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.KeyBinds;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.PropertyHelper;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.compat.PlayerReviveHelper;
import com.mrcrayfish.guns.compat.ShoulderSurfingHelper;
import com.mrcrayfish.guns.debug.Debug;
import com.mrcrayfish.guns.init.ModBlocks;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageAim;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
public class AimingHandler
{
    private static AimingHandler instance;

    public static AimingHandler get()
    {
        if(instance == null)
        {
            instance = new AimingHandler();
        }
        return instance;
    }

    private static final double MAX_AIM_PROGRESS = 5;
    private final AimTracker localTracker = new AimTracker();
    private final Map<Player, AimTracker> aimingMap = new WeakHashMap<>();
    private double normalisedAdsProgress;
    private float lastZoomModifier = 1F;
    private boolean aiming = false;
    private boolean setNewZoomModifier = true;
    private boolean doTempFirstPerson = false;
    private boolean skipThirdPersonSwitch = false;
    private boolean speedReductionApplied = false;

    private AimingHandler() {}

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        Player player = event.player;
        AimTracker tracker = getAimTracker(player);
        if(tracker != null)
        {
            tracker.handleAiming(player, player.getItemInHand(InteractionHand.MAIN_HAND));
            if(!tracker.isAiming())
            {
                this.aimingMap.remove(player);
            }
        }
    }

    @Nullable
    private AimTracker getAimTracker(Player player)
    {
        if(ModSyncedDataKeys.AIMING.getValue(player) && !this.aimingMap.containsKey(player))
        {
            this.aimingMap.put(player, new AimTracker());
        }
        return this.aimingMap.get(player);
    }

    public float getAimProgress(Player player, float partialTicks)
    {
        if(player.isLocalPlayer())
        {
            return (float) this.localTracker.getNormalProgress(partialTicks);
        }

        AimTracker tracker = this.getAimTracker(player);
        if(tracker != null)
        {
            return (float) tracker.getNormalProgress(partialTicks);
        }
        return 0F;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return;
        
        ItemStack heldItem = mc.player.getMainHandItem();
    	GunItem gunItem;
    	Gun modifiedGun = null;
    	if (mc.player.getMainHandItem().getItem() instanceof GunItem)
    	{
    		gunItem = (GunItem) mc.player.getMainHandItem().getItem();
    		modifiedGun = gunItem.getModifiedGun(mc.player.getMainHandItem());
    	}
        boolean resetPOV = false;

        if(this.isAiming())
        {
        	if (!mc.options.keySprint.isDown())
            {
                player.setSprinting(false);
            }
            if(!this.aiming)
            {
                ModSyncedDataKeys.AIMING.setValue(player, true);
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(true));
                this.aiming = true;
            }
            if (Config.CLIENT.forceFirstPersonOnZoomedAim.get() && getNormalisedAdsProgress()>=0.2 && getNormalisedAdsProgress()<=0.95)
            {
            	if (!this.doTempFirstPerson && modifiedGun!=null)
                {
                	if(modifiedGun.getModules().getZoom() != null && Gun.getFovModifier(heldItem, modifiedGun) <= Config.CLIENT.firstPersonAimZoomThreshold.get())
                    {
                    	if (ShoulderSurfingHelper.isShoulderSurfing())
	                    	{
	                    		this.doTempFirstPerson = true;
	                    		ShoulderSurfingHelper.changePerspective("FIRST_PERSON");
	                        	this.skipThirdPersonSwitch = false;
	                		}
                    }
                    else
                    if (this.doTempFirstPerson)
                    {
                        resetPOV = true;
                    }
                }
            }
            if(!this.speedReductionApplied)
            {
                player.setSprinting(false);
                this.speedReductionApplied = true;
            }
        }
        else
        {
        	if (this.doTempFirstPerson && getNormalisedAdsProgress()<=0.3)
            {
                resetPOV = true;
            }
        	if(this.aiming)
	        {
	            ModSyncedDataKeys.AIMING.setValue(player, false);
	            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAim(false));
	            this.aiming = false;
	        }
            if(this.speedReductionApplied)
            {
                this.speedReductionApplied = false;
            }
        }
        
        if (this.doTempFirstPerson)
        {
        	if (mc.options.getCameraType()!=CameraType.FIRST_PERSON)
            {
                this.skipThirdPersonSwitch = true;
            }
        	if(modifiedGun==null || modifiedGun.getModules().getZoom() == null
            || Gun.getFovModifier(heldItem, modifiedGun) > Config.CLIENT.firstPersonAimZoomThreshold.get())
            {
                resetPOV = true;
            }
        }

        if (resetPOV && Config.CLIENT.forceFirstPersonOnZoomedAim.get())
        {
        	this.doTempFirstPerson = false;
        	if (mc.options.getCameraType()==CameraType.FIRST_PERSON && !skipThirdPersonSwitch)
            {
                ShoulderSurfingHelper.changePerspective("SHOULDER_SURFING");
            }
        }

        this.localTracker.handleAiming(player, player.getItemInHand(InteractionHand.MAIN_HAND));
    }

    @SubscribeEvent
    public void onFovUpdate(ViewportEvent.ComputeFov event)
    {
        if(!event.usedConfiguredFov())
            return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.player.getMainHandItem().isEmpty() || (mc.options.getCameraType() != CameraType.FIRST_PERSON && !ShoulderSurfingHelper.isShoulderSurfing()))
            return;

        ItemStack heldItem = mc.player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem gunItem))
            return;

        if(AimingHandler.get().getNormalisedAdsProgress() == 0)
        {
            this.setNewZoomModifier = true;
            return;
        }

        if(ModSyncedDataKeys.RELOADING.getValue(mc.player))
            return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(modifiedGun.getModules().getZoom() == null)
            return;

        double time = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getFovCurve().apply(this.normalisedAdsProgress);
        boolean isFirstPerson = (mc.options.getCameraType() == CameraType.FIRST_PERSON);
        float modifier = this.lastZoomModifier;
        float newModifier = Gun.getFovModifier(heldItem, modifiedGun);
        if(this.isZooming() && this.setNewZoomModifier)
        {
            this.lastZoomModifier = newModifier;
            this.setNewZoomModifier = false;
        }
        modifier = Math.max((modifier * (isFirstPerson ? 1 : 0.3F)) + (isFirstPerson ? 0 : 0.4F),modifier);
        modifier = (1.0F - modifier) * (float) time;
        event.setFOV(event.getFOV() - event.getFOV() * modifier);
    }

    @SubscribeEvent
    public void onClientTick(ClientPlayerNetworkEvent.LoggingOut event)
    {
        this.aimingMap.clear();
    }

    /**
     * Prevents the crosshair from rendering when aiming down sight
     */
    @SubscribeEvent(receiveCanceled = true)
    public void onRenderOverlay(RenderGuiOverlayEvent event)
    {
        this.normalisedAdsProgress = this.localTracker.getNormalProgress(event.getPartialTick());
    }

    public boolean isZooming()
    {
        return this.aiming;
    }

    public boolean isAiming()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return false;

        if(mc.player.isSpectator())
            return false;
        
        if(mc.options.keySprint.isDown() && mc.player.isSprinting())
            return false;

        if(Debug.isForceAim())
            return true;

        if(mc.screen != null)
            return false;

        if(PlayerReviveHelper.isBleeding(mc.player))
            return false;

        ItemStack heldItem = mc.player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem))
            return false;

        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        if(!gun.canAimDownSight())
            return false;

        if(mc.player.getOffhandItem().getItem() == Items.SHIELD && (gun.getGeneral().getGripType() == GripType.ONE_HANDED_PISTOL || gun.getGeneral().getGripType() == GripType.TWO_HANDED_PISTOL))
            return false;

        if(!this.localTracker.isAiming() && this.isLookingAtInteractableBlock())
            return false;

        if(ModSyncedDataKeys.RELOADING.getValue(mc.player) || ReloadHandler.get().getReloadTimer()!=0)
            return false;

        if(mc.player.tickCount<ShootingHandler.get().getWeaponSwitchTick()+3 || (GunAnimationHelper.getSmartAnimationType(heldItem, mc.player, mc.getPartialTick()) == "draw" && ModSyncedDataKeys.SWITCHTIME.getValue(mc.player) > 0))
            return false;

        boolean zooming = KeyBinds.getAimMapping().isDown();
        if(GunMod.controllableLoaded)
        {
            zooming |= ControllerHandler.isAiming();
        }

        return zooming;
    }

    public boolean isLookingAtInteractableBlock()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.hitResult != null && mc.level != null)
        {
            if(mc.hitResult instanceof BlockHitResult result)
            {
                BlockState state = mc.level.getBlockState(result.getBlockPos());
                Block block = state.getBlock();
                // Forge should add a tag for intractable blocks so modders can know which blocks can be interacted with :)
                return block instanceof EntityBlock || block == Blocks.CRAFTING_TABLE || block == ModBlocks.GUN_WORKBENCH.get() || state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS) || state.is(Tags.Blocks.CHESTS) || state.is(Tags.Blocks.FENCE_GATES);
            }
            else if(mc.hitResult instanceof EntityHitResult result)
            {
                return result.getEntity() instanceof ItemFrame;
            }
        }
        return false;
    }

    public double getNormalisedAdsProgress()
    {
        return this.normalisedAdsProgress;
    }

    public class AimTracker
    {
        private double currentAim;
        private double previousAim;

        private void handleAiming(Player player, ItemStack heldItem)
        {
            if(player.tickCount<ShootingHandler.get().getWeaponSwitchTick()+4)
            {
                this.previousAim = this.currentAim = 0;
                return;
            }
            this.previousAim = this.currentAim;

            if(ModSyncedDataKeys.AIMING.getValue(player) || (player.isLocalPlayer() && AimingHandler.this.isAiming()))
            {
                if(this.currentAim < MAX_AIM_PROGRESS)
                {
                    double speed = GunModifierHelper.getAimDownSightSpeed(heldItem);
                    speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, speed);
                    this.currentAim += speed;
                    if(this.currentAim > MAX_AIM_PROGRESS)
                    {
                        this.currentAim = (int) MAX_AIM_PROGRESS;
                    }
                }
            }
            else
            {
                if(this.currentAim > 0)
                {
                    double speed = GunModifierHelper.getAimDownSightSpeed(heldItem);
                    speed = GunModifierHelper.getModifiedAimDownSightSpeed(heldItem, speed);
                    this.currentAim -= speed;
                    if(this.currentAim < 0)
                    {
                        this.currentAim = 0;
                    }
                }
            }
        }

        public boolean isAiming()
        {
            return this.currentAim != 0 || this.previousAim != 0;
        }

        public double getNormalProgress(float partialTicks)
        {
            return Mth.clamp((this.previousAim + (this.currentAim - this.previousAim) * partialTicks) / MAX_AIM_PROGRESS, 0.0, 1.0);
        }
    }
}

package com.mrcrayfish.guns.client.handler;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.client.GunModel;
import com.mrcrayfish.guns.client.GunRenderType;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.render.gun.ModelOverrides;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.GunLegacyAnimationHelper;
import com.mrcrayfish.guns.client.util.PropertyHelper;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.properties.SightAnimation;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.attachment.impl.IAttachment;
import com.mrcrayfish.guns.item.attachment.impl.IBarrel;
import com.mrcrayfish.guns.item.attachment.impl.create.Scope;
import com.mrcrayfish.guns.item.grenade.GrenadeItem;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class GunRenderingHandler
{
    private static GunRenderingHandler instance;

    public static GunRenderingHandler get()
    {
        if(instance == null)
        {
            instance = new GunRenderingHandler();
        }
        return instance;
    }

    public static final ResourceLocation MUZZLE_FLASH_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/effect/muzzle_flash.png");
    public static final ResourceLocation MUZZLE_FLASH_TEXTURE_1 = new ResourceLocation(Reference.MOD_ID, "textures/effect/muzzle_flash_1.png");

    private final Random random = new Random();
    private final Set<Integer> entityIdForMuzzleFlash = new HashSet<>();
    private final Set<Integer> entityIdForDrawnMuzzleFlash = new HashSet<>();
    private final Map<Integer, Float> entityIdToRandomValue = new HashMap<>();

    private double lastViewportFOV;
    private boolean setNewViewportFOV = true;

    private int sprintTransition;
    private int prevSprintTransition;
    private int sprintCooldown;
    private float sprintIntensity;

    private boolean playingHitMarker = false;
    private int hitMarkerTime;
    private int prevHitMarkerTime;
    private int hitMarkerMaxTime = 2;
    private boolean hitMarkerCrit = false;
    
    private int lastStartReloadTick = 0;
    private float lastReloadCycle = 0;
    private float lastReloadDeltaTime = 0;
    
    private int reserveAmmo = 0;
    private int ammoAutoUpdateTimer = 0;
    private int ammoAutoUpdateRate = 20;
    private boolean doUpdateAmmo = false;

    private float offhandTranslate;
    private float prevOffhandTranslate;

    private Field equippedProgressMainHandField;
    private Field prevEquippedProgressMainHandField;

    private float immersiveRoll;
    private float prevImmersiveRoll;
    private float fallSway;
    private float prevFallSway;

    @Nullable
    private ItemStack renderingWeapon;

    private GunRenderingHandler() {}

    @Nullable
    public ItemStack getRenderingWeapon()
    {
        return this.renderingWeapon;
    }

    @Nullable
    public float getSprintTransition(float partialTicks)
    {
    	return (this.prevSprintTransition + (this.sprintTransition - this.prevSprintTransition) * partialTicks) / 5F;
    }

    @Nullable
    public float getHitMarkerProgress(float partialTicks)
    {
    	return ((this.prevHitMarkerTime + (this.hitMarkerTime - this.prevHitMarkerTime) * partialTicks) / (float) hitMarkerMaxTime);
    }

    @Nullable
    public boolean isRenderingHitMarker()
    {
    	return playingHitMarker;
    }

    @Nullable
    public boolean getHitMarkerCrit()
    {
    	return hitMarkerCrit;
    }

    @Nullable
    public int getSprintCooldown()
    {
    	return sprintCooldown;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        this.updateSprinting();
        this.updateHitMarker();
        this.updateMuzzleFlash();
        this.updateOffhandTranslate();
        this.updateImmersiveCamera();
        
        ammoAutoUpdateTimer++;
        if (ammoAutoUpdateTimer>=ammoAutoUpdateRate)
        {
        	doUpdateAmmo = true;
        	ammoAutoUpdateTimer=0;
        }
    }

    private void updateSprinting()
    {
    	this.prevSprintTransition = this.sprintTransition;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player != null && mc.player.isSprinting() && ModSyncedDataKeys.SWITCHTIME.getValue(mc.player)<=0 && !ModSyncedDataKeys.SHOOTING.getValue(mc.player) && (!ModSyncedDataKeys.RELOADING.getValue(mc.player) && ReloadHandler.get().getReloadTimer()==0) && !AimingHandler.get().isAiming() && this.sprintCooldown == 0)
        {
            if(this.sprintTransition < 5)
            {
                this.sprintTransition++;
            }
        }
        else if(this.sprintTransition > 0)
        {
            this.sprintTransition--;
        }

        if(this.sprintCooldown > 0)
        {
            this.sprintCooldown--;
        }
    }

    private void updateHitMarker()
    {
    	this.prevHitMarkerTime = this.hitMarkerTime;

        if(playingHitMarker)
        {
            this.hitMarkerTime++;
            if(this.hitMarkerTime > hitMarkerMaxTime)
            {
            	this.playingHitMarker=false;
            	this.hitMarkerTime=0;
            }
        }
        else
        {
        	this.hitMarkerTime=0;
        }
    }
    private void fetchReserveAmmo(Player player, Gun gun)
    {
    	this.reserveAmmo = Gun.getReserveAmmoCount(player, gun.getProjectile().getItem());
    	this.doUpdateAmmo = false;
    	this.ammoAutoUpdateTimer=0;
    }
    
    public void updateReserveAmmo(Player player, Gun gun)
    {
    	fetchReserveAmmo(player, gun);
    }
    
    public void stageReserveAmmoUpdate()
    {
    	this.ammoAutoUpdateTimer=this.ammoAutoUpdateRate;
    }
    
    public void stageReserveAmmoUpdate(int i)
    {
    	int newUpdateTimer = this.ammoAutoUpdateRate-i;
    	this.ammoAutoUpdateTimer = Mth.clamp(newUpdateTimer, 0, ammoAutoUpdateRate);
    }
    
    public void forceSetReserveAmmo(int ammoCount)
    {
    	this.reserveAmmo = ammoCount;
    	this.doUpdateAmmo = false;
    	this.ammoAutoUpdateTimer=0;
    }
    
    public void updateReserveAmmo(Player player)
    {

        ItemStack heldItem = player.getMainHandItem();
        if(heldItem.getItem() instanceof GunItem)
        {
        	Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        	fetchReserveAmmo(player, modifiedGun);
        }
    }

    private void updateMuzzleFlash()
    {
        this.entityIdForDrawnMuzzleFlash.clear();
        this.entityIdForDrawnMuzzleFlash.addAll(this.entityIdForMuzzleFlash);
        this.entityIdToRandomValue.keySet().removeAll(this.entityIdForDrawnMuzzleFlash);
        this.entityIdForMuzzleFlash.removeAll(this.entityIdForDrawnMuzzleFlash);
    }

    private void updateOffhandTranslate()
    {
        this.prevOffhandTranslate = this.offhandTranslate;
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        boolean down = false;
        ItemStack heldItem = mc.player.getMainHandItem();
        if(heldItem.getItem() instanceof GunItem)
        {
            Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            down = (!modifiedGun.getGeneral().getGripType().getHeldAnimation().canRenderOffhandItem() || ModSyncedDataKeys.RELOADING.getValue(mc.player));
        }

        float direction = down ? -1.0F : 1.0F;
        this.offhandTranslate = Mth.clamp(this.offhandTranslate + direction, -2.0F, 1.0F);
    }

    @SubscribeEvent
    public void onGunFire(GunFireEvent.Post event)
    {
        if(!event.isClient())
            return;

        this.sprintTransition = 0;
        this.sprintCooldown = 10;

        ItemStack heldItem = event.getStack();
        GunItem gunItem = (GunItem) heldItem.getItem();
        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(modifiedGun.getDisplay().getFlash() != null)
        {
            this.showMuzzleFlashForPlayer(Minecraft.getInstance().player.getId());
        }
    }

    public void showMuzzleFlashForPlayer(int entityId)
    {
        this.entityIdForMuzzleFlash.add(entityId);
        this.entityIdToRandomValue.put(entityId, this.random.nextFloat());
    }

	public void playHitMarker(boolean crit) {
		this.playingHitMarker=true;
		this.hitMarkerCrit=crit;
		this.hitMarkerTime=1;
		this.prevHitMarkerTime=0;
	}

    /**
     * Handles calculating the FOV of the first person viewport when aiming with a scope. Changing
     * the FOV allows the user to look through the model of the scope. At a high FOV, the model is
     * very hard to see through, so by lowering the FOV it makes it possible to look through it. This
     * avoids having to render the game twice, which saves a lot of performance.
     */
    @SubscribeEvent
    public void onComputeFov(ViewportEvent.ComputeFov event)
    {
        // We only want to modify the FOV of the viewport for rendering hand/items in first person
        if(event.usedConfiguredFov())
            return;

        // Test if the gun has a scope
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        ItemStack heldItem = player.getMainHandItem();
        if(!(heldItem.getItem() instanceof GunItem gunItem))
            return;

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(!modifiedGun.canAimDownSight())
            return;

        // Change the FOV of the first person viewport based on the scope and aim progress
        if(AimingHandler.get().getNormalisedAdsProgress() <= 0)
        {
            this.setNewViewportFOV = true;
            return;
        }

        // Calculate the time curve
        double time = AimingHandler.get().getNormalisedAdsProgress();
        SightAnimation sightAnimation = PropertyHelper.getSightAnimations(heldItem, modifiedGun);
        time = sightAnimation.getViewportCurve().apply(time);

        // Apply the new FOV
        double newViewportFov = PropertyHelper.getViewportFov(heldItem, modifiedGun);
        if (this.lastViewportFOV == 0)
            lastViewportFOV = newViewportFov;

        double viewportFov = lastViewportFOV;
        if(AimingHandler.get().isZooming() && this.setNewViewportFOV)
        {
            this.lastViewportFOV = newViewportFov;
            this.setNewViewportFOV = false;
        }
        double newFov = viewportFov > 0 ? viewportFov : event.getFOV(); // Backwards compatibility
        event.setFOV(Mth.lerp(time, event.getFOV(), newFov));
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderHandEvent event)
    {
        PoseStack poseStack = event.getPoseStack();

        boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? event.getHand() == InteractionHand.MAIN_HAND : event.getHand() == InteractionHand.OFF_HAND;
        HumanoidArm hand = right ? HumanoidArm.RIGHT : HumanoidArm.LEFT;

        ItemStack heldItem = event.getItemStack();
        if(event.getHand() == InteractionHand.OFF_HAND)
        {
            if(heldItem.getItem() instanceof GunItem)
            {
                event.setCanceled(true);
                return;
            }

            float offhand = 1.0F - Mth.lerp(event.getPartialTick(), this.prevOffhandTranslate, this.offhandTranslate);
            poseStack.translate(0, offhand * -0.6F, 0);

            Player player = Minecraft.getInstance().player;
            if(player != null && player.getMainHandItem().getItem() instanceof GunItem)
            {
                Gun modifiedGun = ((GunItem) player.getMainHandItem().getItem()).getModifiedGun(player.getMainHandItem());
                if(!modifiedGun.getGeneral().getGripType().getHeldAnimation().canRenderOffhandItem())
                {
                    return;
                }
            }

            /* Makes the off hand item move out of view */
            poseStack.translate(0, -1 * AimingHandler.get().getNormalisedAdsProgress(), 0);
        }

        if(!(heldItem.getItem() instanceof GunItem gunItem))
        {
            return;
        }

        /* Cancel it because we are doing our own custom render */
        event.setCanceled(true);

        ItemStack overrideModel = ItemStack.EMPTY;
        if(heldItem.getTag() != null)
        {
            if(heldItem.getTag().contains("Model", Tag.TAG_COMPOUND))
            {
                overrideModel = ItemStack.of(heldItem.getTag().getCompound("Model"));
            }
        }

        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(overrideModel.isEmpty() ? heldItem : overrideModel, player.level, player, 0);
        float scaleX = model.getTransforms().firstPersonRightHand.scale.x();
        float scaleY = model.getTransforms().firstPersonRightHand.scale.y();
        float scaleZ = model.getTransforms().firstPersonRightHand.scale.z();
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        float translateY = model.getTransforms().firstPersonRightHand.translation.y();
        float translateZ = model.getTransforms().firstPersonRightHand.translation.z();

        poseStack.pushPose();

        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
        if(AimingHandler.get().getNormalisedAdsProgress() > 0 && modifiedGun.canAimDownSight())
        {
            if(event.getHand() == InteractionHand.MAIN_HAND)
            {
                double xOffset = translateX;
                double yOffset = translateY;
                double zOffset = translateZ;

                /* Offset since rendering translates to the center of the model */
                xOffset -= 0.5 * scaleX;
                yOffset -= 0.5 * scaleY;
                zOffset -= 0.5 * scaleZ;

                /* Translate to the origin of the weapon */
                Vec3 gunOrigin = PropertyHelper.getModelOrigin(heldItem, PropertyHelper.GUN_DEFAULT_ORIGIN);
                xOffset += gunOrigin.x * 0.0625 * scaleX;
                yOffset += gunOrigin.y * 0.0625 * scaleY;
                zOffset += gunOrigin.z * 0.0625 * scaleZ;

                /* Creates the required offsets to position the scope into the middle of the screen. */
                Scope scope = Gun.getScope(heldItem);
                if(modifiedGun.canAttachType(IAttachment.Type.SCOPE) && scope != null)
                {
                    /* Translate to the mounting position of scopes */
                    Vec3 scopePosition = PropertyHelper.getAttachmentPosition(heldItem, modifiedGun, IAttachment.Type.SCOPE).subtract(gunOrigin);
                    xOffset += scopePosition.x * 0.0625 * scaleX;
                    yOffset += scopePosition.y * 0.0625 * scaleY;
                    zOffset += scopePosition.z * 0.0625 * scaleZ;

                    /* Translate to the reticle of the scope */
                    ItemStack scopeStack = Gun.getScopeStack(heldItem);
                    Vec3 scopeOrigin = PropertyHelper.getModelOrigin(scopeStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
                    Vec3 scopeCamera = PropertyHelper.getScopeCamera(scopeStack).subtract(scopeOrigin);
                    Vec3 scopeScale = PropertyHelper.getAttachmentScale(heldItem, modifiedGun, IAttachment.Type.SCOPE);
                    xOffset += scopeCamera.x * 0.0625 * scaleX * scopeScale.x;
                    yOffset += scopeCamera.y * 0.0625 * scaleY * scopeScale.y;
                    zOffset += scopeCamera.z * 0.0625 * scaleZ * scopeScale.z;
                }
                else
                {
                    /* Translate to iron sight */
                    Vec3 ironSightCamera = PropertyHelper.getIronSightCamera(heldItem, modifiedGun, gunOrigin).subtract(gunOrigin);
                    xOffset += ironSightCamera.x * 0.0625 * scaleX;
                    yOffset += ironSightCamera.y * 0.0625 * scaleY;
                    zOffset += ironSightCamera.z * 0.0625 * scaleZ;

                    /* Need to add this to ensure old method still works */
                    if(PropertyHelper.isLegacyIronSight(heldItem))
                    {
                        zOffset += 0.72;
                    }
                }

                /* Controls the direction of the following translations, changes depending on the main hand. */
                float side = right ? 1.0F : -1.0F;
                double time = AimingHandler.get().getNormalisedAdsProgress();
                double transition = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getSightCurve().apply(time);

                /* Reverses the original first person translations */
                poseStack.translate(-0.56 * side * transition, 0.52 * transition, 0.72 * transition);

                /* Reverses the first person translations of the item in order to position it in the center of the screen */
                poseStack.translate(-xOffset * side * transition, -yOffset * transition, -zOffset * transition);
            }
        }

        /* Applies custom bobbing animations */
        this.applyBobbingTransforms(poseStack, event.getPartialTick());

        /* Applies equip progress animation */
        float equipProgress = this.getEquipProgress(event.getPartialTick());
        if (GunAnimationHelper.getSmartAnimationType(heldItem, player, event.getPartialTick()) != "draw")
            poseStack.mulPose(Vector3f.XP.rotationDegrees(equipProgress * -50F));

        /* Update the current reload progress, when applicable */
        this.updateReloadProgress(heldItem);
        
        /* Renders the reload arm. Will only render if actually reloading. This is applied before
         * any recoil or reload rotations as the animations would be borked if applied after. */
        this.renderReloadArm(poseStack, event.getMultiBufferSource(), event.getPackedLight(), modifiedGun, heldItem, hand, translateX);

        // Values are based on vanilla translations for first person
        int offset = right ? 1 : -1;
        poseStack.translate(0.56 * offset, -0.52, -0.72);

        /* Apply various transforms, such as for aiming, sprinting, reloading, and custom animations */
        this.applyIdleTransforms(poseStack, heldItem, modifiedGun, offset);
        this.applyAimingTransforms(poseStack, heldItem, modifiedGun, translateX, translateY, translateZ, offset);
        this.applySwayTransforms(poseStack, modifiedGun, player, translateX, translateY, translateZ, event.getPartialTick());
        this.applySprintingTransforms(modifiedGun, hand, poseStack, event.getPartialTick());
        this.applyAnimationTransforms(poseStack, player, heldItem, modifiedGun, event.getPartialTick());
        this.applyRecoilTransforms(poseStack, heldItem, modifiedGun);
        this.applyReloadTransforms(poseStack, heldItem, modifiedGun, event.getPartialTick());
        this.applyShieldTransforms(poseStack, player, modifiedGun, event.getPartialTick());

        /* Determines the lighting for the weapon. Weapon will appear bright from muzzle flash or light sources */
        int blockLight;
        if (player.isOnFire())
            blockLight = 15;
        else
            blockLight = player.level.getBrightness(LightLayer.BLOCK, new BlockPos(player.getEyePosition(event.getPartialTick())));
        if (this.entityIdForMuzzleFlash.contains(player.getId()))
            blockLight += Config.COMMON.dynamicLightValue.get();

        blockLight = Math.min(blockLight, 15);
        int packedLight = LightTexture.pack(blockLight, player.level.getBrightness(LightLayer.SKY, new BlockPos(player.getEyePosition(event.getPartialTick()))));

        /* Renders the first persons arms from the grip type of the weapon */
        poseStack.pushPose();
        modifiedGun.getGeneral().getGripType().getHeldAnimation().renderFirstPersonArms(Minecraft.getInstance().player, hand, heldItem, poseStack, event.getMultiBufferSource(), packedLight, event.getPartialTick());
        poseStack.popPose();

        /* Renders the weapon */
        ItemTransforms.TransformType transformType = right ? ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
        this.renderWeapon(Minecraft.getInstance().player, heldItem, transformType, event.getPoseStack(), event.getMultiBufferSource(), packedLight, event.getPartialTick());

        poseStack.popPose();
    }

    private void applyBobbingTransforms(PoseStack poseStack, float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.options.bobView().get() && mc.getCameraEntity() instanceof Player player)
        {
            float deltaDistanceWalked = player.walkDist - player.walkDistO;
            float distanceWalked = -(player.walkDist + deltaDistanceWalked * partialTicks);
            float bobbing = Mth.lerp(partialTicks, player.oBob, player.bob);

            /* Reverses the original bobbing rotations and translations so it can be controlled */
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-(Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F)));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F)));
            poseStack.translate(-(Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 0.5F), -(-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * bobbing)), 0.0D);

            /* Slows down the bob by half */
            bobbing *= player.isSprinting() ? 8.0 : 4.0;
            bobbing *= Config.CLIENT.bobbingIntensity.get();

            /* The new controlled bobbing */
            double invertZoomProgress = 1.0 - AimingHandler.get().getNormalisedAdsProgress() * this.sprintIntensity;
            //poseStack.translate((double) (Mth.sin(distanceWalked * (float) Math.PI) * cameraYaw * 0.5F) * invertZoomProgress, (double) (-Math.abs(Mth.cos(distanceWalked * (float) Math.PI) * cameraYaw)) * invertZoomProgress, 0.0D);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees((Mth.sin(distanceWalked * (float) Math.PI) * bobbing * 3.0F) * (float) invertZoomProgress));
            poseStack.mulPose(Vector3f.XP.rotationDegrees((Math.abs(Mth.cos(distanceWalked * (float) Math.PI - 0.2F) * bobbing) * 5.0F) * (float) invertZoomProgress));
        }
    }

    private void applyIdleTransforms(PoseStack poseStack, ItemStack heldItem, Gun modifiedGun, int offset)
    {
        float aiming = (float) Math.sin(Math.toRadians(AimingHandler.get().getNormalisedAdsProgress() * 180F));
        aiming = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getAimTransformCurve().apply(aiming);
        Vec3 idleTranslations = PropertyHelper.getViewmodelPosition(heldItem, modifiedGun);
        poseStack.translate(idleTranslations.x * offset,idleTranslations.y,idleTranslations.z);
    }

    private void applyAimingTransforms(PoseStack poseStack, ItemStack heldItem, Gun modifiedGun, float x, float y, float z, int offset)
    {
        if(!Config.CLIENT.oldAnimations.get())
        {
            poseStack.translate(x * offset, y, z);
            poseStack.translate(0, -0.25, 0.25);
            float aiming = (float) Math.sin(Math.toRadians(AimingHandler.get().getNormalisedAdsProgress() * 180F));
            aiming = PropertyHelper.getSightAnimations(heldItem, modifiedGun).getAimTransformCurve().apply(aiming);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(aiming * 10F * offset));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(aiming * 5F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(aiming * 5F * offset));
            poseStack.translate(0, 0.25, -0.25);
            poseStack.translate(-x * offset, -y, -z);
        }
    }

    private void applySwayTransforms(PoseStack poseStack, Gun modifiedGun, LocalPlayer player, float x, float y, float z, float partialTicks)
    {
        if(Config.CLIENT.weaponSway.get() && player != null)
        {
            poseStack.translate(x, y, z);

            double zOffset = modifiedGun.getGeneral().getGripType().getHeldAnimation().getFallSwayZOffset();
            poseStack.translate(0, -0.25, zOffset);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(partialTicks, this.prevFallSway, this.fallSway)));
            poseStack.translate(0, 0.25, -zOffset);

            float bobPitch = Mth.rotLerp(partialTicks, player.xBobO, player.xBob);
            float headPitch = Mth.rotLerp(partialTicks, player.xRotO, player.getXRot());
            float swayPitch = headPitch - bobPitch;
            swayPitch *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
            poseStack.mulPose(Config.CLIENT.swayType.get().getPitchRotation().rotationDegrees(swayPitch * Config.CLIENT.swaySensitivity.get().floatValue()));

            float bobYaw = Mth.rotLerp(partialTicks, player.yBobO, player.yBob);
            float headYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
            float swayYaw = headYaw - bobYaw;
            swayYaw *= 1.0 - 0.5 * AimingHandler.get().getNormalisedAdsProgress();
            poseStack.mulPose(Config.CLIENT.swayType.get().getYawRotation().rotationDegrees(swayYaw * Config.CLIENT.swaySensitivity.get().floatValue()));

            poseStack.translate(-x, -y, -z);
        }
    }

    private void applySprintingTransforms(Gun modifiedGun, HumanoidArm hand, PoseStack poseStack, float partialTicks)
    {
        if(Config.CLIENT.sprintAnimation.get() && modifiedGun.getGeneral().getGripType().getHeldAnimation().canApplySprintingAnimation())
        {
        	GripType pose = modifiedGun.getGeneral().getGripType();
        	if(pose == GripType.ONE_HANDED_PISTOL || pose == GripType.TWO_HANDED_PISTOL)
        	{
            	float transition = (this.prevSprintTransition + (this.sprintTransition - this.prevSprintTransition) * partialTicks) / 5F;
            	transition = (float) Math.sin((transition * Math.PI) / 2);
            	transition = (float) (transition*(1-AimingHandler.get().getNormalisedAdsProgress()));
            	poseStack.translate(0, 0.35 * transition, -0.1 * transition);
            	poseStack.mulPose(Vector3f.XP.rotationDegrees(45F * transition));
        	}
        	else
        	{
        		float leftHanded = hand == HumanoidArm.LEFT ? -1 : 1;
            	float transition = (this.prevSprintTransition + (this.sprintTransition - this.prevSprintTransition) * partialTicks) / 5F;
            	transition = (float) Math.sin((transition * Math.PI) / 2);
            	transition = (float) (transition*(1-AimingHandler.get().getNormalisedAdsProgress()));
            	poseStack.translate(-0.25 * leftHanded * transition, -0.1 * transition, 0);
            	poseStack.mulPose(Vector3f.YP.rotationDegrees(45F * leftHanded * transition));
            	poseStack.mulPose(Vector3f.XP.rotationDegrees(-25F * transition));
        	}
        }
    }

    private void applyAnimationTransforms(PoseStack poseStack, LocalPlayer player, ItemStack item, Gun modifiedGun, float partialTicks)
    {
    	Minecraft mc = Minecraft.getInstance();
    	double zoomFactor = (1-Gun.getFovModifier(item, modifiedGun)) * AimingHandler.get().getNormalisedAdsProgress();
    	Vec3 translations = GunAnimationHelper.getSmartAnimationTrans(item, player, partialTicks, "viewModel").scale(1-zoomFactor);
        Vec3 rotations = GunAnimationHelper.getSmartAnimationRot(item, player, partialTicks, "viewModel").scale(1-zoomFactor);
        Vec3 offsets = GunAnimationHelper.getSmartAnimationRotOffset(item, player, partialTicks, "viewModel").add(5.25, 4.0, 4.0);
    	if(!GunAnimationHelper.hasAnimation("fire", item) && GunAnimationHelper.getSmartAnimationType(item, player, partialTicks)=="fire")
    	{
    		ItemCooldowns tracker = mc.player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(item.getItem(), Minecraft.getInstance().getFrameTime());
            translations = translations.add(GunLegacyAnimationHelper.getViewModelTranslation(item, cooldown));
    		rotations = rotations.add(GunLegacyAnimationHelper.getViewModelRotation(item, cooldown));
    	}
        
        poseStack.translate(translations.x * 0.0625, translations.y * 0.0625, translations.z * 0.0625);
        GunAnimationHelper.rotateAroundOffset(poseStack, rotations, offsets);
    }

    private void applyReloadTransforms(PoseStack poseStack, ItemStack item, Gun modifiedGun, float partialTicks)
    {
    	float reloadProgress = ReloadHandler.get().getReloadProgress(partialTicks);
    	if(!GunAnimationHelper.hasAnimation("reload", item))
    	{
    		double reloadOffset = Math.max(!modifiedGun.getGeneral().usesMagReload() ? Math.min(getReloadDeltaTime(item)*2.1 + 0.8, 1) : 1, 0);
    		poseStack.translate(0, 0.35 * (reloadOffset * reloadProgress), 0);
    		poseStack.translate(0, 0, -0.1 * (reloadOffset * reloadProgress));
    		poseStack.mulPose(Vector3f.XP.rotationDegrees(45F * ((float) reloadOffset) * reloadProgress));
    	}
    }

    private void applyRecoilTransforms(PoseStack poseStack, ItemStack item, Gun gun)
    {
        double recoilNormal = RecoilHandler.get().getGunRecoilNormal();
        if(Gun.hasAttachmentEquipped(item, gun, IAttachment.Type.SCOPE))
        {
            recoilNormal -= recoilNormal * (0.5 * AimingHandler.get().getNormalisedAdsProgress());
        }
        float kickReduction = 1.0F - GunModifierHelper.getKickReduction(item);
        float recoilReduction = 1.0F - GunModifierHelper.getRecoilModifier(item);
        double kick = gun.getGeneral().getRecoilKick() * 0.0625 * (recoilNormal) * RecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilLift = (float) (gun.getGeneral().getRecoilAngle() * recoilNormal) * (float) RecoilHandler.get().getAdsRecoilReduction(gun);
        float recoilSwayAmount = (float) (2F + 1F * (1.0 - AimingHandler.get().getNormalisedAdsProgress()));
        float recoilSway = (float) ((RecoilHandler.get().getGunRecoilRandom() * recoilSwayAmount - recoilSwayAmount / 2F) * recoilNormal);
        poseStack.translate(0, 0, kick * kickReduction);
        poseStack.translate(0, 0, 0.15);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(recoilSway * recoilReduction));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(recoilSway * recoilReduction));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(recoilLift * recoilReduction));
        poseStack.translate(0, 0, -0.15);
    }

    private void applyShieldTransforms(PoseStack poseStack, LocalPlayer player, Gun modifiedGun, float partialTick)
    {
        if(player.isUsingItem() && player.getOffhandItem().getItem() == Items.SHIELD && (modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED_PISTOL || modifiedGun.getGeneral().getGripType() == GripType.TWO_HANDED_PISTOL))
        {
        	this.sprintCooldown = 1;
        	double time = Mth.clamp((player.getTicksUsingItem() + partialTick), 0.0, 4.0) / 4.0;
            poseStack.translate(0, 0.35 * time, 0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(45F * (float) time));
        }
    }

    @SuppressWarnings("resource")
	@SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase.equals(TickEvent.Phase.START))
            return;

        Minecraft mc = Minecraft.getInstance();
        if(!mc.isWindowActive())
            return;

        Player player = mc.player;
        if(player == null)
            return;

        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.isEmpty())
            return;
        
        updateReloadProgress(heldItem);

        if(player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND && heldItem.getItem() instanceof GrenadeItem)
        {
            if(!((GrenadeItem) heldItem.getItem()).canCook())
                return;

            int duration = player.getTicksUsingItem();
            if(duration >= 10)
            {
                float cookTime = 1.0F - ((float) (duration - 10) / (float) (player.getUseItem().getUseDuration() - 10));
                if(cookTime > 0.0F)
                {
                    float scale = 3;
                    Window window = mc.getWindow();
                    int i = (int) ((window.getGuiScaledHeight() / 2 - 7 - 60) / scale);
                    int j = (int) Math.ceil((window.getGuiScaledWidth() / 2 - 8 * scale) / scale);

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

                    PoseStack stack = new PoseStack();
                    stack.scale(scale, scale, scale);
                    int progress = (int) Math.ceil((cookTime) * 17.0F) - 1;
                    Screen.blit(stack, j, i, 36, 94, 16, 4, 256, 256);
                    Screen.blit(stack, j, i, 52, 94, progress, 4, 256, 256);

                    RenderSystem.disableBlend();
                }
            }
            return;
        }

        if(heldItem.getItem() instanceof GunItem)
        {
        	renderGunInfoHUD(event, heldItem);
        	
            if(Config.CLIENT.cooldownIndicator.get())
            {
            	//Gun gun = ((GunItem) heldItem.getItem()).getGun();
            	if(!Gun.isAuto(heldItem) && !Gun.hasBurstFire(heldItem))
            	{
                	float coolDown = player.getCooldowns().getCooldownPercent(heldItem.getItem(), event.renderTickTime);
                	if(coolDown > 0.0F)
                	{
                    	float scale = 3;
                    	Window window = mc.getWindow();
                    	int i = (int) ((window.getGuiScaledHeight() / 2 - 7 - 60) / scale);
                    	int j = (int) Math.ceil((window.getGuiScaledWidth() / 2 - 8 * scale) / scale);

                    	RenderSystem.enableBlend();
                    	RenderSystem.defaultBlendFunc();
                    	RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    	RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    	RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

                    	PoseStack stack = new PoseStack();
                    	stack.scale(scale, scale, scale);
                    	int progress = (int) Math.ceil((coolDown + 0.05) * 17.0F) - 1;
                    	Screen.blit(stack, j, i, 36, 94, 16, 4, 256, 256);
                    	Screen.blit(stack, j, i, 52, 94, progress, 4, 256, 256);

                    	RenderSystem.disableBlend();
                	}
            	}
        	}
        }
    }

    @SuppressWarnings("resource")
	public void renderGunInfoHUD(TickEvent.RenderTickEvent event, ItemStack heldItem)
    {
    	if(!Config.CLIENT.displayAmmoCount.get())
    		return;

        Player player = Minecraft.getInstance().player;
    	Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        CompoundTag tagCompound = heldItem.getTag();
        if (Minecraft.getInstance().screen != null)
        	return;
        if(tagCompound != null)
        {
            Window window = Minecraft.getInstance().getWindow();

            int ammoPosX = (int) (window.getGuiScaledWidth()*0.87);
            int ammoPosY = (int) (window.getGuiScaledHeight()*0.8);

            // PoseStack for text components
            PoseStack poseStack = new PoseStack();

            // Special hud when gun is broken
            if(heldItem.getDamageValue() == (heldItem.getMaxDamage() - 1))
                GuiComponent.drawString(poseStack, Minecraft.getInstance().font, Component.translatable("info.cgm.broken"), ammoPosX, ammoPosY-10, 0xAA0000);

            // Ammo Item Icon
            Item ammoItem = ForgeRegistries.ITEMS.getValue(gun.getProjectile().getItem());
            if(ammoItem != null && (!Gun.hasInfiniteAmmo(heldItem) || gun.getProjectile().getProjectileOverride()==null))
            {
                ItemStack ammoStack = new ItemStack(ammoItem, 1);
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
				itemRenderer.renderGuiItem(ammoStack, ammoPosX-17, ammoPosY);
            }
            
            // Fire mode display
            if (gun.getFireModes().usesFireModes())
            {
	            int currentFireMode = Gun.getFireMode(heldItem);
	            String fireModeString = (currentFireMode==0 ? "Semi" : (currentFireMode==1 ? "Auto" : (currentFireMode==2 ? "Burst" : "")));
	            //GuiComponent.drawString(poseStack, Minecraft.getInstance().font, fireModeDisplay, ammoPosX, ammoPosY+20, 0xFFFFFF);
	            
	            MutableComponent fireSwitch = (Component.literal(""));
	            int fireSwitches = 0;
	            if (Gun.canDoSemiFire(heldItem))
	            {
	            	fireSwitch.append(Component.literal("'").withStyle(currentFireMode==0 ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
	            	fireSwitches++;
            	}
	            if (Gun.canDoAutoFire(heldItem))
	            {
	            	fireSwitch.append(Component.literal("'").withStyle(currentFireMode==1 ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
	            	fireSwitches++;
            	}
	            if (Gun.canDoBurstFire(heldItem))
	            {
	            	fireSwitch.append(Component.literal("'").withStyle(currentFireMode==2 ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
	            	fireSwitches++;
	            }
	            
	            MutableComponent fireModeDisplay = (Component.literal(""));
	            if (fireSwitches>1)
	            	fireModeDisplay.append(fireSwitch.append(" "));
	            fireModeDisplay.append(Component.literal(fireModeString).withStyle(ChatFormatting.BOLD));
	            
		        GuiComponent.drawString(poseStack, Minecraft.getInstance().font, fireModeDisplay, ammoPosX, ammoPosY+20, 0xFFFFFF);
		        //GuiComponent.drawCenteredString(poseStack, Minecraft.getInstance().font, fireSwitch, ammoPosX-6, ammoPosY+20, 0x555555);
	            
        	}
            
            // Ammo counter
            int currentAmmo = tagCompound.getInt("AmmoCount");
            MutableComponent ammoCountValue = (Component.literal(currentAmmo + " / " + GunCompositeStatHelper.getAmmoCapacity(heldItem, gun)).withStyle(ChatFormatting.BOLD));
            if (Gun.hasInfiniteAmmo(heldItem))
            	ammoCountValue = (Component.literal("∞ / ∞").withStyle(ChatFormatting.BOLD));

            if(ModSyncedDataKeys.RELOADING.getValue(player))
                GuiComponent.drawString(poseStack, Minecraft.getInstance().font, Component.translatable("info.cgm.reloading"), ammoPosX, ammoPosY, 0xFFFF55);
            else
                GuiComponent.drawString(poseStack, Minecraft.getInstance().font, ammoCountValue, ammoPosX, ammoPosY, (currentAmmo>0 || Gun.hasInfiniteAmmo(heldItem) ? 0xFFFFFF : 0xFF5555));

            // Reserve ammo counter
            if (!Gun.hasInfiniteAmmo(heldItem))
            {
            	if (this.doUpdateAmmo)
            	{
            		this.fetchReserveAmmo(player, gun);
            		this.doUpdateAmmo = false;
            	}
            	String displayReserveAmmo = (!Gun.hasUnlimitedReloads(heldItem) ? "" + reserveAmmo: "∞");
	            MutableComponent reserveAmmoValue = (Component.literal(displayReserveAmmo));
	            GuiComponent.drawString(poseStack, Minecraft.getInstance().font, reserveAmmoValue, ammoPosX+5, ammoPosY+10, (reserveAmmo<=0 && !Gun.hasUnlimitedReloads(heldItem) ? 0x555555 : 0xAAAAAA));
            }

            RenderSystem.disableBlend();
        }
    }

    public void applyWeaponScale(ItemStack heldItem, PoseStack stack)
    {
        if(heldItem.getTag() != null)
        {
            CompoundTag compound = heldItem.getTag();
            if(compound.contains("Scale", Tag.TAG_FLOAT))
            {
                float scale = compound.getFloat("Scale");
                stack.scale(scale, scale, scale);
            }
        }
    }

    public boolean renderWeapon(@Nullable LivingEntity entity, ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks)
    {
        if(stack.getItem() instanceof GunItem)
        {
            poseStack.pushPose();

            ItemStack model = ItemStack.EMPTY;
            if(stack.getTag() != null)
            {
                if(stack.getTag().contains("Model", Tag.TAG_COMPOUND))
                {
                    model = ItemStack.of(stack.getTag().getCompound("Model"));
                }
            }

            RenderUtil.applyTransformType(stack, poseStack, transformType, entity);

            this.renderingWeapon = stack;
            this.renderGun(entity, transformType, model.isEmpty() ? stack : model, poseStack, renderTypeBuffer, light, partialTicks);
            this.renderAttachments(entity, transformType, stack, poseStack, renderTypeBuffer, light, partialTicks);
            this.renderMuzzleFlash(entity, poseStack, renderTypeBuffer, stack, transformType, partialTicks);
            this.renderingWeapon = null;

            poseStack.popPose();
            return true;
        }
        return false;
    }

    private void renderGun(@Nullable LivingEntity entity, ItemTransforms.TransformType transformType, ItemStack stack, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks)
    {
        if(ModelOverrides.hasModel(stack))
        {
            IOverrideModel model = ModelOverrides.getModel(stack);
            if(model != null)
            {
                model.render(partialTicks, transformType, stack, ItemStack.EMPTY, entity, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
            }
        }
        else
        {
            Level level = entity != null ? entity.level : null;
            BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, level, entity, 0);
            Minecraft.getInstance().getItemRenderer(). render(stack, ItemTransforms.TransformType.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, bakedModel);
        }
    }

    private void renderAttachments(@Nullable LivingEntity entity, ItemTransforms.TransformType transformType, ItemStack stack, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, float partialTicks)
    {
        if(stack.getItem() instanceof GunItem)
        {
            Gun modifiedGun = ((GunItem) stack.getItem()).getModifiedGun(stack);
            CompoundTag gunTag = stack.getOrCreateTag();
            CompoundTag attachments = gunTag.getCompound("Attachments");
            for(String tagKey : attachments.getAllKeys())
            {
                IAttachment.Type type = IAttachment.Type.byTagKey(tagKey);
                if(type != null && type != IAttachment.Type.MAGAZINE && modifiedGun.canAttachType(type))
                {
                    ItemStack attachmentStack = Gun.getAttachment(type, stack);
                    if(!attachmentStack.isEmpty())
                    {
                        poseStack.pushPose();

                        /* Gather some animation-related values before we continue */
                        Vec3 animTrans = Vec3.ZERO;
                        Vec3 animRot = Vec3.ZERO;
                        String animType = "";
                        boolean animateableContext = (transformType.firstPerson() || transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
                        if (entity != null && entity.equals(Minecraft.getInstance().player))
                        {
                        	Player player = Minecraft.getInstance().player;
                        	animTrans = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, type.getSerializeKey());
                        	animRot = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, type.getSerializeKey());
                	        animType = GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks);
                	        if(!GunAnimationHelper.hasAnimation("fire", stack) && GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks)=="fire")
	                        {
		                        if (GunLegacyAnimationHelper.hasAttachmentAnimation(stack, type))
		                        {
		                        	float cooldown = 0F;
		                        	ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
		                            cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
		                        	animTrans = (GunLegacyAnimationHelper.getAttachmentTranslation(stack, type, cooldown));
		                        }
	                    	}
                        }

                        /* Translates the attachment to a standard position by removing the origin */
                        Vec3 origin = PropertyHelper.getModelOrigin(attachmentStack, PropertyHelper.ATTACHMENT_DEFAULT_ORIGIN);
                        poseStack.translate(-origin.x * 0.0625, -origin.y * 0.0625, -origin.z * 0.0625);

                        /* Translation to the origin on the weapon */
                        Vec3 gunOrigin = PropertyHelper.getModelOrigin(stack, PropertyHelper.GUN_DEFAULT_ORIGIN);
                        poseStack.translate(gunOrigin.x * 0.0625, gunOrigin.y * 0.0625, gunOrigin.z * 0.0625);

                        Vec3 translation = PropertyHelper.getAttachmentPosition(stack, modifiedGun, type).subtract(gunOrigin);
                        /* Translate to the position this attachment mounts on the weapon */
                        poseStack.translate((translation.x) * 0.0625, (translation.y) * 0.0625, (translation.z) * 0.0625);
                        
                        /* Apply attachment animation translations */
                        if (animateableContext)
                        {
                        	poseStack.translate((animTrans.x) * 0.0625, (animTrans.y) * 0.0625, (animTrans.z) * 0.0625);
                    	}

                        /* Scales the attachment. Also translates the delta of the attachment origin to (8, 8, 8) since this is the centered origin for scaling */
                        Vec3 scale = PropertyHelper.getAttachmentScale(stack, modifiedGun, type);
                        Vec3 center = origin.subtract(8, 8, 8).scale(0.0625);
                        poseStack.translate(center.x, center.y, center.z);
                        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
                        poseStack.translate(-center.x, -center.y, -center.z);
                        
                        /* Lastly, rotate the attachment */
                        if (animateableContext)
                        {
                        	Vec3 rotations = PropertyHelper.getAttachmentPosition(stack, modifiedGun, type).subtract(gunOrigin);
                        	GunAnimationHelper.rotateAroundOffset(poseStack, animRot, animType, stack, "forwardHand");
                    	}

                        IOverrideModel model = ModelOverrides.getModel(attachmentStack);
                        if(model != null)
                        {
                            model.render(partialTicks, transformType, attachmentStack, stack, entity, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY);
                        }
                        else
                        {
                            Level level = entity != null ? entity.level : null;
                            BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(attachmentStack, level, entity, 0);
                            Minecraft.getInstance().getItemRenderer().render(attachmentStack, ItemTransforms.TransformType.NONE, false, poseStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, GunModel.wrap(bakedModel));
                        }

                        poseStack.popPose();
                    }
                }
            }
        }
    }

    private void renderMuzzleFlash(@Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, ItemStack weapon, ItemTransforms.TransformType transformType, float partialTicks)
    {
        Gun modifiedGun = ((GunItem) weapon.getItem()).getModifiedGun(weapon);
        if(modifiedGun.getDisplay().getFlash() == null)
            return;

        if(transformType != ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND && transformType != ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND && transformType != ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND && transformType != ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND)
            return;

        if(entity == null || !this.entityIdForMuzzleFlash.contains(entity.getId()))
            return;

        float randomValue = this.entityIdToRandomValue.get(entity.getId());
        this.drawMuzzleFlash(weapon, modifiedGun, randomValue, randomValue >= 0.5F, poseStack, buffer, partialTicks);
    }

    private void drawMuzzleFlash(ItemStack weapon, Gun modifiedGun, float random, boolean flip, PoseStack poseStack, MultiBufferSource buffer, float partialTicks)
    {
        if(!PropertyHelper.hasMuzzleFlash(weapon, modifiedGun))
            return;

        poseStack.pushPose();

        // Translate to the position where the muzzle flash should spawn
        Vec3 weaponOrigin = PropertyHelper.getModelOrigin(weapon, PropertyHelper.GUN_DEFAULT_ORIGIN);
        Vec3 flashPosition = PropertyHelper.getMuzzleFlashPosition(weapon, modifiedGun).subtract(weaponOrigin);
        poseStack.translate(weaponOrigin.x * 0.0625, weaponOrigin.y * 0.0625, weaponOrigin.z * 0.0625);
        poseStack.translate(flashPosition.x * 0.0625, flashPosition.y * 0.0625, flashPosition.z * 0.0625);
        poseStack.translate(-0.5, -0.5, -0.5);

        // Legacy method to move muzzle flash to be at the end of the barrel attachment
        ItemStack barrelStack = Gun.getAttachment(IAttachment.Type.BARREL, weapon);
        if(!barrelStack.isEmpty() && barrelStack.getItem() instanceof IBarrel barrel && !PropertyHelper.isUsingBarrelMuzzleFlash(barrelStack))
        {
            Vec3 scale = PropertyHelper.getAttachmentScale(weapon, modifiedGun, IAttachment.Type.BARREL);
            double length = barrel.getProperties().getLength();
            poseStack.translate(0, 0, -length * 0.0625 * scale.z);
        }

        poseStack.mulPose(Vector3f.ZP.rotationDegrees(360F * random));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(flip ? 180F : 0F));

        Vec3 flashScale = PropertyHelper.getMuzzleFlashScale(weapon, modifiedGun);
        float adjustedPartialTicks = Math.max(partialTicks-0.5F,0);
        float scaleX = ((float) flashScale.x / 2F) - ((float) flashScale.x / 2F) * (adjustedPartialTicks);
        float scaleY = ((float) flashScale.y / 2F) - ((float) flashScale.y / 2F) * (adjustedPartialTicks);
        poseStack.scale(scaleX, scaleY, 1.0F);

        float scaleModifier = (float) GunModifierHelper.getMuzzleFlashScale(weapon, 1.0);
        poseStack.scale(scaleModifier, scaleModifier, 1.0F);

        // Center the texture
        poseStack.translate(-0.5, -0.5, 0);
        
        int flashType = PropertyHelper.getMuzzleFlashType(weapon, modifiedGun);
        int flashVariant = PropertyHelper.getMuzzleFlashVariant(weapon, modifiedGun);

        float minU = weapon.isEnchanted() ? 0.5F : 0.0F;
        float maxU = weapon.isEnchanted() ? 1.0F : 0.5F;
        float minC = 0.0F;
        float maxC = 1.0F;
        
        if (flashType==1)
        {
        	int variant = Math.max(Math.min(flashVariant,7),0);
        	minC = (float) (variant)*(0.125F);
        	maxC = (float) (variant+1)*(0.125F);
        }
        
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer builder = buffer.getBuffer(GunRenderType.getMuzzleFlash(flashType));
        builder.vertex(matrix, 0, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, maxC).uv2(15728880).endVertex();
        builder.vertex(matrix, 1, 0, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, maxC).uv2(15728880).endVertex();
        builder.vertex(matrix, 1, 1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(minU, minC).uv2(15728880).endVertex();
        builder.vertex(matrix, 0, 1, 0).color(1.0F, 1.0F, 1.0F, 1.0F).uv(maxU, minC).uv2(15728880).endVertex();

        poseStack.popPose();
    }

    private void renderReloadArm(PoseStack poseStack, MultiBufferSource buffer, int light, Gun modifiedGun, ItemStack stack, HumanoidArm hand, float translateX)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || ReloadHandler.get().getReloadTimer() == 0 || (!modifiedGun.getGeneral().usesMagReload() && getReloadDeltaTime(stack)<0.5F))
            return;
        
        if(GunAnimationHelper.hasAnimation("reload", stack))
        	return;

        Item item = ForgeRegistries.ITEMS.getValue(modifiedGun.getProjectile().getItem());

        poseStack.pushPose();

        int side = hand.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(translateX * side, 0, 0);

        float baseReload = getReloadCycleProgress(stack);
        float reload = (baseReload*2) % 1;
        if (!modifiedGun.getGeneral().getUseMagReload())
        {
        	float progressOffset = 0.63F;
        	reload = (baseReload+progressOffset) % 1;
    	}
        
        float percent = 1.0F - reload;
        if(percent >= 0.5F)
        {
            percent = 1.0F - percent;
        }
        percent *= 2F;
        percent = percent < 0.5 ? 2 * percent * percent : -1 + (4 - 2 * percent) * percent;

        poseStack.translate(3.5 * side * 0.0625, -0.5625 -( (1-ReloadHandler.get().getReloadProgress(mc.getFrameTime())) * 0.7), -0.5625);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));
        poseStack.translate(0, -0.35 * (1.0 - percent), 0);
        poseStack.translate(side * 0.0625, 0, 0);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(90F));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(35F * -side));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-75F * percent));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        RenderUtil.renderFirstPersonArm(mc.player, hand.getOpposite(), poseStack, buffer, light);

        if(reload < 0.5F && ReloadHandler.get().getReloadTimer() == 1 && getReloadDeltaTime(stack)>=0.5F && item != null)
        {
            poseStack.pushPose();
            poseStack.translate(-side * 5 * 0.0625, 15 * 0.0625, -1 * 0.0625);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180F));
            poseStack.scale(0.75F, 0.75F, 0.75F);
            int amount = (modifiedGun.getGeneral().getUseMagReload() ? modifiedGun.getGeneral().getMaxAmmo() : modifiedGun.getGeneral().getReloadAmount());
            int ammoPerItem = modifiedGun.getGeneral().getAmmoPerItem();
            double itemCount = Math.ceil((float) (amount/ammoPerItem));
            ItemStack ammo = new ItemStack(item, (int) Math.max(itemCount, 1));
            BakedModel model = RenderUtil.getModel(ammo);
            boolean isModel = model.isGui3d();
            this.random.setSeed(Item.getId(item));
            int count = Math.min((int) Math.max(itemCount, 1), 5);
            for(int i = 0; i < count; ++i)
            {
                poseStack.pushPose();
                if(i > 0)
                {
                    if(isModel)
                    {
                        float x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float z = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        poseStack.translate(x, y, z);
                    }
                    else
                    {
                        float x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        float y = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        poseStack.translate(x, y, 0);
                    }
                }

                RenderUtil.renderModel(ammo, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, poseStack, buffer, light, OverlayTexture.NO_OVERLAY, null);
                poseStack.popPose();

                if(!isModel)
                {
                    poseStack.translate(0.0, 0.0, 0.09375F);
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }
    
    public void updateReloadProgress(ItemStack stack)
    {
    	Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;
        
        if(!(stack.getItem() instanceof GunItem))
            return;
    	
        if(ReloadHandler.get().getReloading(mc.player) || ReloadHandler.get().getReloadProgress(mc.getFrameTime()) >= 0.5F)
        {
	    	float reloadInterval = GunCompositeStatHelper.getRealReloadSpeed(stack, ReloadHandler.get().isDoMagReload(), ReloadHandler.get().isReloadFromEmpty());
	    	int reloadStartDelay = 5;
	    	if (stack.getItem() instanceof GunItem gunItem)
	    	{
	    		Gun gun = gunItem.getModifiedGun(stack);
	    		if (ReloadHandler.get().isReloadFromEmpty())
		    	reloadStartDelay = Math.max(gun.getGeneral().getReloadEmptyStartDelay(),0);
	    		else
    	    		reloadStartDelay = Math.max(gun.getGeneral().getReloadStartDelay(),0);
	    	}
	    	if (ReloadHandler.get().getStartReloadTick()>0)
	    	    this.lastStartReloadTick = ReloadHandler.get().getStartReloadTick();
	    	
    		float reloadDelta = (mc.player.tickCount - (lastStartReloadTick + reloadStartDelay) + mc.getFrameTime()) / reloadInterval;
    		this.lastReloadDeltaTime = reloadDelta;
	    	this.lastReloadCycle = reloadDelta % 1F;
    	}
    }
    
    public float getReloadCycleProgress(ItemStack stack)
    {
    	Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return 0;
    	
        updateReloadProgress(stack);
        return this.lastReloadCycle;
    }
    
    public float getReloadDeltaTime(ItemStack stack)
    {
    	Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return 0;

        updateReloadProgress(stack);
        return this.lastReloadDeltaTime;
    }

    /**
     * A temporary hack to get the equip progress until Forge fixes the issue.
     */
    private float getEquipProgress(float partialTicks)
    {
        if(this.equippedProgressMainHandField == null)
        {
            this.equippedProgressMainHandField = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109302_");
            this.equippedProgressMainHandField.setAccessible(true);
        }
        if(this.prevEquippedProgressMainHandField == null)
        {
            this.prevEquippedProgressMainHandField = ObfuscationReflectionHelper.findField(ItemInHandRenderer.class, "f_109303_");
            this.prevEquippedProgressMainHandField.setAccessible(true);
        }
        ItemInHandRenderer firstPersonRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
        try
        {
            float equippedProgressMainHand = (float) this.equippedProgressMainHandField.get(firstPersonRenderer);
            float prevEquippedProgressMainHand = (float) this.prevEquippedProgressMainHandField.get(firstPersonRenderer);
            return 1.0F - Mth.lerp(partialTicks, prevEquippedProgressMainHand, equippedProgressMainHand);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return 0.0F;
    }

    private void updateImmersiveCamera()
    {
        this.prevImmersiveRoll = this.immersiveRoll;
        this.prevFallSway = this.fallSway;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        ItemStack heldItem = mc.player.getMainHandItem();
        float targetAngle = heldItem.getItem() instanceof GunItem || !Config.CLIENT.restrictCameraRollToWeapons.get() ? mc.player.input.leftImpulse: 0F;
        float speed = mc.player.input.leftImpulse != 0 ? 0.1F : 0.15F;
        this.immersiveRoll = Mth.lerp(speed, this.immersiveRoll, targetAngle);

        float deltaY = (float) Mth.clamp((mc.player.yo - mc.player.getY()), -1.0, 1.0);
        deltaY *= 1.0 - AimingHandler.get().getNormalisedAdsProgress();
        deltaY *= 1.0 - (Mth.abs(mc.player.getXRot()) / 90.0F);
        this.fallSway = Mth.approach(this.fallSway, deltaY * 60F * Config.CLIENT.swaySensitivity.get().floatValue(), 10.0F);

        float intensity = mc.player.isSprinting() ? 0.75F : 1.0F;
        this.sprintIntensity = Mth.approach(this.sprintIntensity, intensity, 0.1F);
    }

    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.ComputeCameraAngles event)
    {
        if(Config.CLIENT.cameraRollEffect.get())
        {
            float roll = (float) Mth.lerp(event.getPartialTick(), this.prevImmersiveRoll, this.immersiveRoll);
            roll = (float) Math.sin((roll * Math.PI) / 2.0);
            roll *= Config.CLIENT.cameraRollAngle.get().floatValue();
            event.setRoll(-roll);
        }
    }
}

package com.mrcrayfish.guns.client.render.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.client.render.IHeldAnimation;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.GunLegacyAnimationHelper;
import com.mrcrayfish.guns.client.util.PropertyHelper;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class TwoHandedPistolPose extends WeaponPose
{
    @Override
    protected AimPose getUpPose()
    {
        AimPose upPose = new AimPose();
        upPose.getIdle().setRenderYawOffset(15F).setItemRotation(new Vector3f(7.5F, -5F, 15F)).setItemTranslate(new Vector3f(0, 0, -0.7F)).setRightArm(new LimbPose().setRotationAngleX(-150F).setRotationAngleY(-30F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-130F).setRotationAngleY(64F).setRotationAngleZ(-20F).setRotationPointX(-10).setRotationPointY(1.0F).setRotationPointZ(-1));
        upPose.getAiming().setRenderYawOffset(15F).setItemRotation(new Vector3f(-12.5F, -10F, 25F)).setItemTranslate(new Vector3f(-1, 0, -0.8F)).setRightArm(new LimbPose().setRotationAngleX(-170F).setRotationAngleY(-35F).setRotationPointX(-5).setRotationPointY(3).setRotationPointZ(0)).setLeftArm(new LimbPose().setRotationAngleX(-145F).setRotationAngleY(80F).setRotationAngleZ(-35F).setRotationPointX(-10).setRotationPointY(0.0F).setRotationPointZ(-1));
        return upPose;
    }

    @Override
    protected AimPose getForwardPose()
    {
        AimPose forwardPose = new AimPose();
        forwardPose.getIdle().setRenderYawOffset(15F).setItemRotation(new Vector3f(-7.5F, -13.5F, -5F)).setItemTranslate(new Vector3f(0.4F, 0, -1)).setRightArm(new LimbPose().setRotationAngleX(-75F).setRotationAngleY(-30F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2).setRotationPointZ(1)).setLeftArm(new LimbPose().setRotationAngleX(-61F).setRotationAngleY(45F).setRotationAngleZ(10F).setRotationPointY(2.1F).setRotationPointZ(-1));
        forwardPose.getAiming().setRenderYawOffset(15F).setItemRotation(new Vector3f(-25F, -18F, -5.5F)).setItemTranslate(new Vector3f(0.4F, 0, -1)).setRightArm(new LimbPose().setRotationAngleX(-92F).setRotationAngleY(-35F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-81F).setRotationAngleY(42F).setRotationAngleZ(5F).setRotationPointY(2.1F).setRotationPointZ(0));
        return forwardPose;
    }

    @Override
    protected AimPose getDownPose()
    {
        AimPose downPose = new AimPose();
        downPose.getIdle().setRenderYawOffset(15F).setItemRotation(new Vector3f(-37.5F, -2F, 0F)).setItemTranslate(new Vector3f(0, -0.5F, -1.5F)).setRightArm(new LimbPose().setRotationAngleX(-22F).setRotationAngleY(-30F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(2)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(35F).setRotationAngleZ(50F).setRotationPointY(2.5F).setRotationPointZ(0));
        downPose.getAiming().setRenderYawOffset(15F).setItemRotation(new Vector3f(-42.5F, -2F, -10F)).setItemTranslate(new Vector3f(0, -0.5F, -1F)).setRightArm(new LimbPose().setRotationAngleX(-22F).setRotationAngleY(-30F).setRotationAngleZ(0F).setRotationPointX(-5).setRotationPointY(1)).setLeftArm(new LimbPose().setRotationAngleX(-10F).setRotationAngleY(35F).setRotationAngleZ(55F).setRotationPointY(2.5F).setRotationPointZ(0));
        return downPose;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!player.getOffhandItem().isEmpty())
        {
            boolean right = mc.options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
            ModelPart arm = right ? rightArm : leftArm;
            IHeldAnimation.copyModelAngles(head, arm);
            arm.xRot += (float) Math.toRadians(-70F - (aimProgress * 25));

            if (player.getUseItem().getItem() == Items.SHIELD)
                arm.xRot = (float) Math.toRadians(-105F);
            else if (ModSyncedDataKeys.RELOADING.getValue(player))
                arm.xRot = (float) Math.toRadians(-40F);

            /* Sprint animation for local player */
            /*
            else if (mc.player.equals(player))
            {
                float sprintTransition = GunRenderingHandler.get().getSprintTransition(Minecraft.getInstance().getPartialTick());
                if (sprintTransition > 0) {
                    arm.xRot = Mth.lerp(sprintTransition, arm.xRot, -105F + 720);
                    //arm.xRot = (float) Math.toRadians(-105F*sprintTransition);
                }
            }
            */
            else if (player.isSprinting())
                arm.xRot = (float) Math.toRadians(-105F);
        }
        else {
        	super.applyPlayerModelRotation(player, rightArm, leftArm, head, hand, aimProgress);
        	float angle = this.getPlayerPitch(player);
        	head.xRot = (float) Math.toRadians(angle > 0.0 ? angle * 70F : angle * 90F);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerPreRender(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
    	if (!player.getOffhandItem().isEmpty())
	        return;
        super.applyPlayerPreRender(player, hand, aimProgress, poseStack, buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
    	if (!player.getOffhandItem().isEmpty())
        {
    		if(hand == InteractionHand.MAIN_HAND)
            {
            	//boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
            	poseStack.translate(0, 0, 0.05);
               	//poseStack.mulPose(Vector3f.YP.rotationDegrees((-aimProgress*15) * (right ? 1F : -1F)));
               	poseStack.mulPose(Vector3f.XP.rotationDegrees((-aimProgress*27)));
               	//poseStack.mulPose(Vector3f.ZP.rotationDegrees((aimProgress*1.5F) * (right ? 1F : -1F)));
            }
        }
    	else
    		super.applyHeldItemTransforms(player, hand, aimProgress, poseStack, buffer);
    }

    @Override
    public void renderFirstPersonArms(Player player, HumanoidArm hand, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, float partialTicks)
    {
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, player.level, player, 0);
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        int side = hand.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        float handDiv = (float) GunAnimationHelper.getAnimationValuePublic(GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks), GunAnimationHelper.getItemLocationKey(stack), "handScale");
        float handScale = 1/(handDiv==0 ? 1 : handDiv);
        poseStack.translate(translateX * side, 0, 0);

        boolean slim = Objects.requireNonNull(Minecraft.getInstance().player).getModelName().equals("slim");
        float armWidth = slim ? 3.0F : 4.0F;
        
        if (!(stack.getItem() instanceof GunItem gunStack))
        	return;
        Gun gun = gunStack.getModifiedGun(stack);
        
        ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
        float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());

        // Off-hand arm
        poseStack.pushPose();
        if (player.getOffhandItem().isEmpty() || ModSyncedDataKeys.RELOADING.getValue(player))
        {
        	Vec3 posHand = PropertyHelper.getHandPosition(stack, gun, false);
        	
        	Vec3 translations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "forwardHand");
            Vec3 rotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "forwardHand");
        	if(!GunAnimationHelper.hasAnimation("fire", stack) && GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks).equals("fire"))
        	{
        		translations = GunLegacyAnimationHelper.getHandTranslation(stack, false, cooldown);
        	}
        	if(!GunAnimationHelper.hasAnimation("reload", stack))
        	{
        		float reloadProg = ReloadHandler.get().getReloadProgress(partialTicks);
                poseStack.translate(0, (-24 * reloadProg) * 0.0625, (-6 * reloadProg) * 0.0625);
        	}

            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate((2.9 + posHand.x) * 0.0625 * side, (2.2 + posHand.y) * 0.0625, (-11.2 - posHand.z) * 0.0625);
            //poseStack.translate((1.55) * 0.0625 * side, (0.4) * 0.0625, (-3.5) * 0.0625);

            String animType = GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks);
            poseStack.translate(translations.x * handScale * side * 0.0625, translations.y * handScale * 0.0625, -translations.z * handScale * 0.0625);
            GunAnimationHelper.rotateAroundOffset(poseStack, rotations, animType, stack, "forwardHand");
            
            poseStack.translate((armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.translate(-0.3125 * side, -0.1, -0.4375);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(75F));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(25F * -side));

        	if(GunAnimationHelper.hasAnimation("reload", stack) || ReloadHandler.get().getReloadProgress(partialTicks) < 1)
            {
                RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand.getOpposite(), poseStack, buffer, light);
            }
        }
        poseStack.popPose();

        // Main-hand arm
        poseStack.pushPose();
        {
        	Vec3 posHand = PropertyHelper.getHandPosition(stack, gun, true);
        	
        	Vec3 translations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "rearHand");
            Vec3 rotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "rearHand");
        	if(!GunAnimationHelper.hasAnimation("fire", stack) && GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks).equals("fire"))
        	{
        		translations = GunLegacyAnimationHelper.getHandTranslation(stack, true, cooldown);
        	}

            {
            	poseStack.translate(0, 0.1, -0.675);
            	poseStack.scale(0.5F, 0.5F, 0.5F);
            	poseStack.translate((-1.7 + posHand.x) * 0.0625 * side, (0 + posHand.y) * 0.0625, (3.2 - posHand.z) * 0.0625);
            	//poseStack.translate((-4.0) * 0.0625 * side, (0) * 0.0625, (0) * 0.0625);
                
                String animType = GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks);
            	poseStack.translate(translations.x * handScale * side * 0.0625, translations.y * handScale * 0.0625, -translations.z * handScale * 0.0625);
                GunAnimationHelper.rotateAroundOffset(poseStack, rotations, animType, stack, "rearHand");
                
            	poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);
            	poseStack.mulPose(Vector3f.XP.rotationDegrees(80F));
            	poseStack.mulPose(Vector3f.ZP.rotationDegrees(12F * side));
            }
            RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand, poseStack, buffer, light);
        }
        poseStack.popPose();
    }

    @Override
    public boolean applyOffhandTransforms(Player player, PlayerModel model, ItemStack stack, PoseStack poseStack, float partialTicks)
    {
    	poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180F));

        if(player.isCrouching())
        {
            poseStack.translate(-4.5 * 0.0625, -15 * 0.0625, -4 * 0.0625);
        }
        else if(!player.getItemBySlot(EquipmentSlot.LEGS).isEmpty())
        {
            poseStack.translate(-4.0 * 0.0625, -13 * 0.0625, 1 * 0.0625);
        }
        else
        {
            poseStack.translate(-3.5 * 0.0625, -13 * 0.0625, 1 * 0.0625);
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(90F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(75F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees((float) (Math.toDegrees(model.rightLeg.xRot) / 10F)));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        return true;
    }

    @Override
    public boolean canRenderOffhandItem()
    {
        return true;
    }

    @Override
    public double getFallSwayZOffset()
    {
        return 0.5;
    }

	@Override
	public boolean doRaiseWhenSprint() {
		return true;
	}
}

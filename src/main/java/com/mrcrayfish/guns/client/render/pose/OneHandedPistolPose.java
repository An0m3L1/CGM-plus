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
import com.mrcrayfish.guns.common.Gun.Display.RearHandPos;
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
public class OneHandedPistolPose implements IHeldAnimation
{
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress)
    {
        Minecraft mc = Minecraft.getInstance();
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
            arm.xRot = (float) Math.toRadians(-100F);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if(hand == InteractionHand.MAIN_HAND)
        {
        	//boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
        	poseStack.translate(0, 0, 0.05);
            poseStack.mulPose(Vector3f.XP.rotationDegrees((-aimProgress*27)));
        }
    }

    @Override
    public void renderFirstPersonArms(Player player, HumanoidArm hand, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, float partialTicks)
    {
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, player.level, player, 0);
        float translateX = model.getTransforms().firstPersonRightHand.translation.x();
        float translateZ = model.getTransforms().firstPersonRightHand.translation.z();
        int side = hand.getOpposite() == HumanoidArm.RIGHT ? 1 : -1;
        float handDiv = (float) GunAnimationHelper.getAnimationValuePublic(GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks), GunAnimationHelper.getItemLocationKey(stack), "handScale");
        float handScale = 1/(handDiv==0 ? 1 : handDiv);
        poseStack.translate(translateX * side, 0, -translateZ);

        boolean slim = Objects.requireNonNull(Minecraft.getInstance().player).getModelName().equals("slim");
        float armWidth = slim ? 3.0F : 4.0F;
        
        if (!(stack.getItem() instanceof GunItem gunStack))
        	return;
        Gun gun = gunStack.getModifiedGun(stack);
        RearHandPos posHandDisplay = gun.getDisplay().getRearHand();
        double xOffset = (posHandDisplay != null ? posHandDisplay.getXOffset() : 0);
        double yOffset = (posHandDisplay != null ? posHandDisplay.getYOffset() : 0);
        double zOffset = (posHandDisplay != null ? posHandDisplay.getZOffset() : 0);

        // Off-hand arm
        if (ModSyncedDataKeys.RELOADING.getValue(player))
        {
            poseStack.pushPose();
            {
                Vec3 posHand = PropertyHelper.getHandPosition(stack, gun, false);

                Vec3 translations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "forwardHand");
                Vec3 rotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "forwardHand");
                if(!GunAnimationHelper.hasAnimation("fire", stack) && GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks)=="fire")
                {
                    ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
                    float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
                    translations = GunLegacyAnimationHelper.getHandTranslation(stack, false, cooldown);
                }
                if(!GunAnimationHelper.hasAnimation("reload", stack))
                {
                    float reloadProg = ReloadHandler.get().getReloadProgress(partialTicks);
                    poseStack.translate(0, (-24 * reloadProg) * 0.0625, (-6 * reloadProg) * 0.0625);
                }

                poseStack.scale(0.5F, 0.5F, 0.5F);
                poseStack.translate((2.9 + xOffset + posHand.x) * 0.0625 * side, (2.2 + yOffset + posHand.y) * 0.0625, (-16.2 + zOffset - posHand.z) * 0.0625);
                //poseStack.translate((1.55) * 0.0625 * side, (0.4) * 0.0625, (-3.5) * 0.0625);

                String animType = GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks);
                poseStack.translate(translations.x * handScale * side * 0.0625, translations.y * handScale * 0.0625, -translations.z * handScale * 0.0625);
                GunAnimationHelper.rotateAroundOffset(poseStack, rotations, animType, stack, "forwardHand");

                poseStack.translate((armWidth / 2.0) * 0.0625 * side, 0, 0);
                poseStack.translate(-0.3125 * side, -0.1, -0.4375);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(75F));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(25F * -side));

                if(GunAnimationHelper.hasAnimation("reload", stack) || ReloadHandler.get().getReloadProgress(partialTicks) < 1)
                    RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand.getOpposite(), poseStack, buffer, light);
            }
            poseStack.popPose();
        }

        // Main-hand arm
        poseStack.pushPose();
        {
            Vec3 translations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "rearHand");
            Vec3 rotations = GunAnimationHelper.getSmartAnimationRot(stack, player, partialTicks, "rearHand");

            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate((-4.0 + xOffset) * 0.0625 * side, (0 + yOffset) * 0.0625, (0 + zOffset) * 0.0625);
            //poseStack.translate(-4.0 * 0.0625 * side, 0, 0);

            String animType = GunAnimationHelper.getSmartAnimationType(stack, player, partialTicks);
            poseStack.translate(translations.x * handScale * side * 0.0625, translations.y * handScale * 0.0625, -translations.z * handScale * 0.0625);
            GunAnimationHelper.rotateAroundOffset(poseStack, rotations, animType, stack, "rearHand");

            poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);
            poseStack.translate(0, 0.15, -1.3125);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(75F));

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
}

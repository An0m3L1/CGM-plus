package com.mrcrayfish.guns.client.render.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mrcrayfish.guns.client.render.IHeldAnimation;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class OneHandedPose implements IHeldAnimation
{
    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyPlayerModelRotation(Player player, ModelPart rightArm, ModelPart leftArm, ModelPart head, InteractionHand hand, float aimProgress)
    {
        boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND;
        ModelPart arm = right ? rightArm : leftArm;
        IHeldAnimation.copyModelAngles(head, arm);
        arm.xRot += (float) Math.toRadians(-70F - (aimProgress*25));

        if(player.getUseItem().getItem() == Items.SHIELD)
        {
            arm.xRot = (float) Math.toRadians(-105F);
        }
        if(player.isSprinting() || ModSyncedDataKeys.RELOADING.getValue(player))
        {
        	arm.xRot = (float) Math.toRadians(-105F);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyHeldItemTransforms(Player player, InteractionHand hand, float aimProgress, PoseStack poseStack, MultiBufferSource buffer)
    {
        if(hand == InteractionHand.MAIN_HAND)
        {
        	boolean right = Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT;
        	poseStack.translate(0, 0, 0.05);
           	poseStack.mulPose(Vector3f.XP.rotationDegrees((-aimProgress*27) * (right ? 1F : -1F)));
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
        poseStack.translate(translateX * side, 0, -translateZ);

        boolean slim = Objects.requireNonNull(Minecraft.getInstance().player).getModelName().equals("slim");
        float armWidth = slim ? 3.0F : 4.0F;
        
        if (!(stack.getItem() instanceof GunItem gunStack))
        	return;
        Gun gun = gunStack.getModifiedGun(stack);
        RearHandPos posHand = gun.getDisplay().getRearHand();
        double xOffset = (posHand != null ? posHand.getXOffset() : 0);
        double yOffset = (posHand != null ? posHand.getYOffset() : 0);
        double zOffset = (posHand != null ? posHand.getZOffset() : 0);

        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate((-4.0 + xOffset) * 0.0625 * side, (0 + yOffset) * 0.0625, (0 + zOffset) * 0.0625);
        poseStack.translate(-(armWidth / 2.0) * 0.0625 * side, 0, 0);

        poseStack.translate(0, 0.15, -1.3125);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(75F));

        RenderUtil.renderFirstPersonArm((LocalPlayer) player, hand, poseStack, buffer, light);
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

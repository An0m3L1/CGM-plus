package com.mrcrayfish.guns.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.GunModel;
import com.mrcrayfish.guns.client.SpecialModels;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.attachment.impl.IAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 * Modified by zaeonNineZero
 * Attachment detection logic based off of code from Mo' Guns by Bomb787 and AlanorMiga (MigaMi)
 */
public class SemiAutoShotgunModel implements IOverrideModel
{
    private boolean disableAnimations = false;

    @Override
    // This class renders a multi-part model that supports animations and removeable parts.
    // We'll render the non-moving/static parts first, then render the animated parts.

    // We start by declaring our render function that will handle rendering the core baked model (which is a non-moving part).
    public void render(float partialTicks, ItemTransforms.TransformType transformType, ItemStack stack, ItemStack parent, @Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        // Render the item's BakedModel, which will serve as the core of our custom model.
        BakedModel bakedModel = SpecialModels.SEMI_AUTO_SHOTGUN_BASE.getModel();
        Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, buffer, light, overlay, GunModel.wrap(bakedModel));

        // Render the iron sights element, which is only present when a scope is not attached.
        // We have to grab the gun's scope attachment slot and check whether it is empty or not.
        // If the isEmpty function returns true, then we render the iron sights.
        ItemStack scopeStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
        ItemStack handleStack = Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack);

        if(scopeStack.isEmpty())
        {
            RenderUtil.renderModel(SpecialModels.SEMI_AUTO_SHOTGUN_SIGHTS.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }

        if (handleStack.isEmpty()) {
            RenderUtil.renderModel(SpecialModels.SEMI_AUTO_SHOTGUN_NO_HANDLE.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }
        else{
            RenderUtil.renderModel(SpecialModels.SEMI_AUTO_SHOTGUN_HANDLE.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }

        // Render the top rail element that appears when a scope is attached.
        // We have to grab the gun's scope attachment slot and check whether it is empty or not.
        // If the isEmpty function returns false, then we render the attachment rail.

        // Special animated segment for compat with the CGM Expanded fork.
        // First, some variables for animation building
        boolean isPlayer = entity != null && entity.equals(Minecraft.getInstance().player);
        boolean isFirstPerson = (transformType.firstPerson());
        boolean correctContext = (transformType.firstPerson() || transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);

        Vec3 slideTranslations = Vec3.ZERO;

        if(isPlayer && correctContext && !disableAnimations)
        {
            try {
                Player player = (Player) entity;
                slideTranslations = GunAnimationHelper.getSmartAnimationTrans(stack, player, partialTicks, "slide");
            }
            catch(NoClassDefFoundError ignored) {
                disableAnimations = true;
            }
            catch(Exception e) {
                GunMod.LOGGER.error("NZGE encountered an error trying to apply animations.");
                e.printStackTrace();
                disableAnimations = true;
            }
        }

        // Fire animation is done the old way, and added onto the existing animation.
        GunItem gunStack = (GunItem) stack.getItem();
        Gun gun = gunStack.getModifiedGun(stack);
        if(isPlayer && correctContext)
        {
            float cooldownDivider = 1.0F*Math.max((float) gun.getGeneral().getRate()/3F,1);
            float cooldownOffset1 = cooldownDivider - 1.0F;
            float intensity = 1.0F +1;

            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());
            cooldown *= cooldownDivider;
            float cooldown_a = cooldown-cooldownOffset1;

            float cooldown_b = Math.min(Math.max(cooldown_a*intensity,0),1);
            float cooldown_c = Math.min(Math.max((-cooldown_a*intensity)+intensity,0),1);
            float cooldown_d = Math.min(cooldown_b,cooldown_c);

            slideTranslations = slideTranslations.add(0, 0, cooldown_d * 2.0);
        }

        // Pistol slide. This animated part kicks backward on firing, then moves back to its resting position.
        poseStack.pushPose();
        // Apply transformations to this part.
        if(isPlayer)
            poseStack.translate(0, 0, slideTranslations.z * 0.0625);
        // Render the transformed model.
        RenderUtil.renderModel(SpecialModels.SEMI_AUTO_SHOTGUN_SLIDE.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // Pop pose to compile everything in the render matrix.
        poseStack.popPose();
    }
}
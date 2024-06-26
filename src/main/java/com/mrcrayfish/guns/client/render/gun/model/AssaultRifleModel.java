package com.mrcrayfish.guns.client.render.gun.model;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.client.GunModel;
import com.mrcrayfish.guns.client.SpecialModels;
import com.mrcrayfish.guns.client.handler.ReloadHandler;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.util.GunReloadAnimationHelper;
import com.mrcrayfish.guns.client.util.GunAnimationHelper;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 * Modified by zaeonNineZero for Nine Zero's Gun Expansion
 * Attachment detection logic based off of code from Mo' Guns by Bomb787 and AlanorMiga (MigaMi)
 */
public class AssaultRifleModel implements IOverrideModel
{
    private boolean disableAnimations = false;
    private Method getReloadCycleProgress = null;
    private Method getAnimationTrans = null;

    @Override
    public void render(float partialTicks, ItemTransforms.TransformType transformType, ItemStack stack, ItemStack parent, @Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        BakedModel bakedModel = SpecialModels.ASSAULT_RIFLE_BASE.getModel();
        Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, buffer, light, overlay, GunModel.wrap(bakedModel));

        ItemStack attachmentStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
        if(attachmentStack.isEmpty())
        {
            RenderUtil.renderModel(SpecialModels.ASSAULT_RIFLE_SIGHTS.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }
        else
        {
            RenderUtil.renderModel(SpecialModels.ASSAULT_RIFLE_NO_SIGHTS.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }

        // Special animated segment for compat with the CGM Expanded fork.
        boolean isPlayer = (entity != null && entity.equals(Minecraft.getInstance().player));
        boolean isFirstPerson = (transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND);

        Vec3 translations = Vec3.ZERO;
        Vec3 rotations = Vec3.ZERO;
        String animType = "none";

        if(isPlayer && isFirstPerson && !disableAnimations)
        {
            try {
                translations = GunAnimationHelper.getSmartAnimationTrans(stack, (Player) entity, partialTicks, "magazine");
                rotations = GunAnimationHelper.getSmartAnimationRot(stack, (Player) entity, partialTicks, "magazine");
                animType = GunAnimationHelper.getSmartAnimationType(stack, (Player) entity, partialTicks);
            }
            catch(Exception e) {
                GunMod.LOGGER.error("Guns encountered an error trying to apply animations, disabling animations");
                e.printStackTrace();
                disableAnimations = true;
            }
        }

        poseStack.pushPose();
        // Now we apply our transformations.
        // All we need to do is move the model based on the cooldown variable.
        if(isPlayer && !disableAnimations)
        {
            if(translations!=Vec3.ZERO)
                poseStack.translate(translations.x*0.0625, translations.y*0.0625, translations.z*0.0625);
            if(rotations!=Vec3.ZERO)
                GunAnimationHelper.rotateAroundOffset(poseStack, rotations, animType, stack, "magazine");
        }
        // Our transformations are done - now we can render the model.
        RenderUtil.renderModel(SpecialModels.ASSAULT_RIFLE_MAG.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // Pop pose to compile everything in the render matrix.
        poseStack.popPose();
    }
}
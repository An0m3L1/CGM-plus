package com.an0m3l1.guns.client.render.gun.model;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.client.GunModel;
import com.an0m3l1.guns.client.handler.AimingHandler;
import com.an0m3l1.guns.client.render.gun.IOverrideModel;
import com.an0m3l1.guns.util.OptifineHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Deprecated. This scope utilizes the new scope rendering and doesn't need custom rendering.
 * To upgrade, create a .cgmmeta file for your scope and customize the properties.
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class MediumScopeModel implements IOverrideModel
{
	private static final ResourceLocation HOLO_RETICLE = new ResourceLocation(GunMod.MOD_ID, "textures/effect/holo_reticle.png");
	private static final ResourceLocation HOLO_RETICLE_GLOW = new ResourceLocation(GunMod.MOD_ID, "textures/effect/holo_reticle_glow.png");
	private static final ResourceLocation VIGNETTE = new ResourceLocation(GunMod.MOD_ID, "textures/effect/scope_vignette.png");
	
	@Override
	public void render(float partialTicks, ItemTransforms.TransformType transformType, ItemStack stack, ItemStack parent,
	                   @Nullable
	                   LivingEntity entity, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, int overlay)
	{
		if(OptifineHelper.isShadersEnabled())
		{
			double transition = 1.0 - Math.pow(1.0 - AimingHandler.get().getNormalisedAdsProgress(), 2);
			double zScale = 0.05 + 0.95 * (1.0 - transition);
			poseStack.scale(1.0F, 1.0F, (float) zScale);
		}
		
		BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
		Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, renderTypeBuffer, light, overlay, GunModel.wrap(bakedModel));
	}
}
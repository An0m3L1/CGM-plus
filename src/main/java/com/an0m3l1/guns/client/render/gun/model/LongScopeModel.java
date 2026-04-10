package com.an0m3l1.guns.client.render.gun.model;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.client.GunModel;
import com.an0m3l1.guns.client.render.gun.IOverrideModel;
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
public class LongScopeModel implements IOverrideModel
{
	private static final ResourceLocation RETICLE = new ResourceLocation(GunMod.MOD_ID, "textures/effect/x4_scope_reticle.png");
	private static final ResourceLocation VIGNETTE = new ResourceLocation(GunMod.MOD_ID, "textures/effect/scope_vignette.png");
	
	@Override
	public void render(float partialTicks, ItemTransforms.TransformType transformType, ItemStack stack, ItemStack parent,
	                   @Nullable
	                   LivingEntity entity, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, int overlay)
	{
		BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
		Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.NONE, false, poseStack, renderTypeBuffer, light, overlay, GunModel.wrap(bakedModel));
	}
	
	private boolean isFirstPerson(ItemTransforms.TransformType transformType)
	{
		return transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND || transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
	}
}
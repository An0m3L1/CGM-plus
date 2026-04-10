package com.an0m3l1.guns.client.render.entity;

import com.an0m3l1.guns.entity.LightSourceEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightSourceRenderer extends EntityRenderer<LightSourceEntity>
{
	public LightSourceRenderer(EntityRendererProvider.Context context)
	{
		super(context);
	}
	
	@Override
	public @Nullable ResourceLocation getTextureLocation(@NotNull LightSourceEntity entity)
	{
		return null;
	}
}

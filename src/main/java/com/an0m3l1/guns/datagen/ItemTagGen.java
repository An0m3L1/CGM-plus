package com.an0m3l1.guns.datagen;

import com.an0m3l1.guns.GunMod;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemTagGen extends ItemTagsProvider
{
	public ItemTagGen(DataGenerator generator, BlockTagsProvider blockTagsProvider, ExistingFileHelper existingFileHelper)
	{
		super(generator, blockTagsProvider, GunMod.MOD_ID, existingFileHelper);
	}
	
	@Override
	protected void addTags()
	{
	}
}

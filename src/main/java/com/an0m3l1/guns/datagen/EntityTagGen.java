package com.an0m3l1.guns.datagen;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.init.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EntityTagGen extends EntityTypeTagsProvider
{
	public EntityTagGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
	{
		super(generator, GunMod.MOD_ID, existingFileHelper);
	}
	
	@Override
	protected void addTags()
	{
		this.tag(ModTags.Entities.RESISTANT).addTag(Tags.EntityTypes.BOSSES).add(EntityType.WARDEN).add(EntityType.ELDER_GUARDIAN);
		this.tag(ModTags.Entities.IMMUNE);
	}
}

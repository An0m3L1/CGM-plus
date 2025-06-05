package com.mrcrayfish.guns.datagen;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EntityTagGen extends EntityTypeTagsProvider
{
    public EntityTagGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        this.tag(ModTags.Entities.HIT_RESISTANT)
                .addTag(Tags.EntityTypes.BOSSES)
                .add(EntityType.WARDEN)
                .add(EntityType.ELDER_GUARDIAN);
        this.tag(ModTags.Entities.HIT_IMMUNE);
    }
}

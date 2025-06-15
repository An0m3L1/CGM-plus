package com.mrcrayfish.guns.datagen;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.init.ModTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagGen extends BlockTagsProvider
{
    public BlockTagGen(DataGenerator generator, ExistingFileHelper existingFileHelper)
    {
        super(generator, Reference.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags()
    {
        this.tag(ModTags.Blocks.FRAGILE)
                /* Wooden */
                .addTag(BlockTags.PLANKS)
                .addTag(BlockTags.WOODEN_DOORS)
                .addTag(BlockTags.WOODEN_TRAPDOORS)
                .addTag(BlockTags.WOODEN_SLABS)
                .addTag(BlockTags.WOODEN_STAIRS)
                .addTag(BlockTags.WOODEN_BUTTONS)
                .addTag(BlockTags.WOODEN_PRESSURE_PLATES)
                .addTag(Tags.Blocks.FENCES_WOODEN)
                .addTag(Tags.Blocks.FENCE_GATES_WOODEN)
                .addTag(BlockTags.BEDS)
                .addTag(BlockTags.SIGNS)
                .add(Blocks.BOOKSHELF)
                .add(Blocks.LECTERN)
                .add(Blocks.CRAFTING_TABLE)
                .add(Blocks.NOTE_BLOCK)
                .add(Blocks.FLETCHING_TABLE)
                .add(Blocks.COMPOSTER)
                .add(Blocks.LOOM)
                .add(Blocks.JUKEBOX)
                .add(Blocks.CARTOGRAPHY_TABLE)
                /* Glass */
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(Tags.Blocks.GLASS)
                /* Wool */
                .addTag(BlockTags.WOOL)
                .addTag(BlockTags.WOOL_CARPETS)
                /* Other */
                .addTag(BlockTags.CANDLES)
                .addTag(BlockTags.ICE)
                .addTag(BlockTags.FLOWER_POTS)
                .addTag(BlockTags.BANNERS)
                .add(Blocks.LILY_PAD)
                .add(Blocks.COCOA)
                .add(Blocks.END_ROD)
                .add(Blocks.SCAFFOLDING)
                .add(Blocks.SEA_PICKLE)
                .add(Blocks.TURTLE_EGG)
                .add(Blocks.GLOWSTONE)
                .add(Blocks.SEA_LANTERN)
                .add(Blocks.BAMBOO)
                .add(Blocks.SMALL_AMETHYST_BUD)
                .add(Blocks.MEDIUM_AMETHYST_BUD)
                .add(Blocks.LARGE_AMETHYST_BUD)
                .add(Blocks.AMETHYST_CLUSTER)
                .add(Blocks.LANTERN)
                .add(Blocks.SOUL_LANTERN)
                .add(Blocks.POINTED_DRIPSTONE);
    }
}

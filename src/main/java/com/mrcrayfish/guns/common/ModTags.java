package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.Reference;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags
{
    public static class Items
    {

    }

    public static class Blocks
    {
        public static final TagKey<Block> FRAGILE = modBlockTag("fragile");
    }

    public static class Entities
    {
        public static final TagKey<EntityType<?>> HIT_IMMUNE = modEntityTag("hit_immune");
        public static final TagKey<EntityType<?>> HIT_RESISTANT = modEntityTag("hit_resistant");
    }

    private static TagKey<Item> modItemTag(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Reference.MOD_ID, name));
    }
    private static TagKey<Block> modBlockTag(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(Reference.MOD_ID, name));
    }
    private static TagKey<EntityType<?>> modEntityTag(String name) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(Reference.MOD_ID, name));
    }
}

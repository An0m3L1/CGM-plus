package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.GunMod;
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
		public static final TagKey<Item> HEAVY = modItemTag("heavy");
		public static final TagKey<Item> GUN = modItemTag("gun");
		public static final TagKey<Item> AMMO = modItemTag("ammo");
		public static final TagKey<Item> GRENADE = modItemTag("grenade");
	}
	
	public static class Blocks
	{
		public static final TagKey<Block> DESTRUCTIBLE = modBlockTag("destructible");
		public static final TagKey<Block> HARDNESS_NONE = modBlockTag("hardness_none");
		public static final TagKey<Block> HARDNESS_LOW = modBlockTag("hardness_low");
		public static final TagKey<Block> HARDNESS_MEDIUM = modBlockTag("hardness_medium");
		public static final TagKey<Block> HARDNESS_HIGH = modBlockTag("hardness_high");
	}
	
	public static class Entities
	{
		public static final TagKey<EntityType<?>> IMMUNE = modEntityTag("immune");
		public static final TagKey<EntityType<?>> RESISTANT = modEntityTag("resistant");
	}
	
	private static TagKey<Item> modItemTag(String name)
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(GunMod.MOD_ID, name));
	}
	
	private static TagKey<Block> modBlockTag(String name)
	{
		return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(GunMod.MOD_ID, name));
	}
	
	private static TagKey<EntityType<?>> modEntityTag(String name)
	{
		return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(GunMod.MOD_ID, name));
	}
}

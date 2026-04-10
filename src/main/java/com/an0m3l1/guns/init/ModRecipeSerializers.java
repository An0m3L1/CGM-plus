package com.an0m3l1.guns.init;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.crafting.DyeItemRecipe;
import com.an0m3l1.guns.crafting.WorkbenchRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModRecipeSerializers
{
	public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GunMod.MOD_ID);
	
	public static final RegistryObject<SimpleRecipeSerializer<DyeItemRecipe>> DYE_ITEM = REGISTER.register("dye_item", () -> new SimpleRecipeSerializer<>(DyeItemRecipe::new));
	public static final RegistryObject<WorkbenchRecipeSerializer> WORKBENCH = REGISTER.register("workbench", WorkbenchRecipeSerializer::new);
}

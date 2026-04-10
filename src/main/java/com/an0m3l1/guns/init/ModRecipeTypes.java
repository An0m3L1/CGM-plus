package com.an0m3l1.guns.init;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.crafting.WorkbenchRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModRecipeTypes
{
	public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, GunMod.MOD_ID);
	
	public static final RegistryObject<RecipeType<WorkbenchRecipe>> WORKBENCH = create("workbench");
	
	private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> create(String name)
	{
		return REGISTER.register(name, () -> new RecipeType<>()
		{
			@Override
			public String toString()
			{
				return name;
			}
		});
	}
}

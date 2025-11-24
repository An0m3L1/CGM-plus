package com.mrcrayfish.guns.crafting;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class WorkbenchRecipeSerializer implements RecipeSerializer<WorkbenchRecipe>
{
    @Override
    public WorkbenchRecipe fromJson(ResourceLocation recipeId, JsonObject parent)
    {
        ImmutableList.Builder<WorkbenchIngredient> builder = ImmutableList.builder();
        JsonArray input = GsonHelper.getAsJsonArray(parent, "materials");
        for(int i = 0; i < input.size(); i++)
        {
            JsonObject object = input.get(i).getAsJsonObject();
            builder.add(WorkbenchIngredient.fromJson(object));
        }
        if(!parent.has("result"))
        {
            throw new JsonSyntaxException("Missing result item entry");
        }
        JsonObject resultObject = GsonHelper.getAsJsonObject(parent, "result");
        ItemStack resultItem = ShapedRecipe.itemStackFromJson(resultObject);

        List<ItemStack> returnItems = new ArrayList<>();
        if(parent.has("return_items"))
        {
            JsonArray returnItemsArray = GsonHelper.getAsJsonArray(parent, "return_items");
            for(int i = 0; i < returnItemsArray.size(); i++)
            {
                JsonObject returnItemObject = returnItemsArray.get(i).getAsJsonObject();
                ItemStack returnItem = ShapedRecipe.itemStackFromJson(returnItemObject);
                returnItems.add(returnItem);
            }
        }

        return new WorkbenchRecipe(recipeId, resultItem, builder.build(), returnItems);
    }

    @Nullable
    @Override
    public WorkbenchRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
    {
        ItemStack result = buffer.readItem();
        ImmutableList.Builder<WorkbenchIngredient> builder = ImmutableList.builder();
        int size = buffer.readVarInt();
        for(int i = 0; i < size; i++)
        {
            builder.add((WorkbenchIngredient) Ingredient.fromNetwork(buffer));
        }

        List<ItemStack> returnItems = new ArrayList<>();
        int returnItemCount = buffer.readVarInt();
        for(int i = 0; i < returnItemCount; i++)
        {
            returnItems.add(buffer.readItem());
        }

        return new WorkbenchRecipe(recipeId, result, builder.build(), returnItems);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, WorkbenchRecipe recipe)
    {
        buffer.writeItem(recipe.getItem());
        buffer.writeVarInt(recipe.getMaterials().size());
        for(WorkbenchIngredient ingredient : recipe.getMaterials())
        {
            ingredient.toNetwork(buffer);
        }

        List<ItemStack> returnItems = recipe.getReturnItems();
        buffer.writeVarInt(returnItems.size());
        for(ItemStack returnItem : returnItems)
        {
            buffer.writeItem(returnItem);
        }
    }
}
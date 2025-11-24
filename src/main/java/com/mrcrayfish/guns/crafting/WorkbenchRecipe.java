package com.mrcrayfish.guns.crafting;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.guns.blockentity.WorkbenchBlockEntity;
import com.mrcrayfish.guns.init.ModRecipeSerializers;
import com.mrcrayfish.guns.init.ModRecipeTypes;
import com.mrcrayfish.guns.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class WorkbenchRecipe implements Recipe<WorkbenchBlockEntity>
{
    private final ResourceLocation id;
    private final ItemStack item;
    private final ImmutableList<WorkbenchIngredient> materials;
    private final List<ItemStack> returnItems;

    public WorkbenchRecipe(ResourceLocation id, ItemStack item, ImmutableList<WorkbenchIngredient> materials, List<ItemStack> returnItems)
    {
        this.id = id;
        this.item = item;
        this.materials = materials;
        this.returnItems = returnItems;
    }

    public ItemStack getItem()
    {
        return this.item.copy();
    }

    public ImmutableList<WorkbenchIngredient> getMaterials()
    {
        return this.materials;
    }

    public WorkbenchIngredient getSpecificMaterial(int i)
    {
        return this.materials.get(i);
    }

    public List<ItemStack> getReturnItems()
    {
        return this.returnItems;
    }

    @Override
    public boolean matches(WorkbenchBlockEntity inv, Level worldIn)
    {
        return false;
    }

    @Override
    public ItemStack assemble(WorkbenchBlockEntity inv)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    @Override
    public ItemStack getResultItem()
    {
        return this.item.copy();
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.WORKBENCH.get();
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType()
    {
        return ModRecipeTypes.WORKBENCH.get();
    }

    public boolean hasMaterials(Player player)
    {
        for(WorkbenchIngredient ingredient : this.getMaterials())
        {
            if(!InventoryUtil.hasWorkstationIngredient(player, ingredient))
            {
                return false;
            }
        }
        return true;
    }

    public void consumeMaterials(Player player)
    {
        for(WorkbenchIngredient ingredient : this.getMaterials())
        {
            InventoryUtil.removeWorkstationIngredient(player, ingredient);
        }

        for(ItemStack returnItem : this.returnItems)
        {
            ItemStack returnItemCopy = returnItem.copy();
            if(!player.getInventory().add(returnItemCopy))
            {
                player.level.addFreshEntity(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), returnItemCopy));
            }
        }
    }
}

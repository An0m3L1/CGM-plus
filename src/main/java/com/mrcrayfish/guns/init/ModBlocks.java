package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.block.HempCropBlock;
import com.mrcrayfish.guns.block.WorkbenchBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import java.util.function.Supplier;
/**
 * Author: MrCrayfish
 */
public class ModBlocks
{
    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MOD_ID);

    public static final RegistryObject<Block> WORKBENCH = registerBlock("workbench",
            () -> new WorkbenchBlock(Block.Properties.of(Material.WOOD)
            .requiresCorrectToolForDrops()
            .strength(1.0F)), GunMod.MATERIALS);
    public static final RegistryObject<Block> CAST_IRON_BLOCK = registerBlock("cast_iron_block",
            () -> new Block(BlockBehaviour.Properties.of(Material.METAL)
            .requiresCorrectToolForDrops()
            .strength(6.0F)), GunMod.MATERIALS);
    public static final RegistryObject<RotatedPillarBlock> STEEL_BLOCK = registerBlock("steel_block",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.METAL)
            .requiresCorrectToolForDrops()
            .strength(6.0F)), GunMod.MATERIALS);
    public static final RegistryObject<HempCropBlock> HEMP_CROP_BLOCK = REGISTER.register("hemp_crop",
            () -> new HempCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab)
    {
        RegistryObject<T> toReturn = REGISTER.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item>registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab)
    {
        return ModItems.REGISTER.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }
}

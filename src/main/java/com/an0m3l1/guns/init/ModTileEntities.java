package com.an0m3l1.guns.init;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.blockentity.WorkbenchBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class ModTileEntities
{
	public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GunMod.MOD_ID);
	
	public static final RegistryObject<BlockEntityType<WorkbenchBlockEntity>> WORKBENCH = register("workbench", WorkbenchBlockEntity::new, () -> new Block[]{ModBlocks.GUN_WORKBENCH.get()});
	
	private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String id, BlockEntityType.BlockEntitySupplier<T> factoryIn, Supplier<Block[]> validBlocksSupplier)
	{
		//noinspection DataFlowIssue
		return REGISTER.register(id, () -> BlockEntityType.Builder.of(factoryIn, validBlocksSupplier.get()).build(null));
	}
}

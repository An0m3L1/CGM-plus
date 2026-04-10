package com.an0m3l1.guns.block;

import com.an0m3l1.guns.init.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;

public class HempCropBlock extends CropBlock
{
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
	
	public HempCropBlock(Properties properties)
	{
		super(properties);
	}
	
	@Override
	protected @NotNull ItemLike getBaseSeedId()
	{
		return ModItems.HEMP_SEEDS.get();
	}
	
	@Override
	public @NotNull IntegerProperty getAgeProperty()
	{
		return AGE;
	}
	
	@Override
	public int getMaxAge()
	{
		return 7;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(AGE);
	}
}

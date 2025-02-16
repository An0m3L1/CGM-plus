package com.mrcrayfish.guns.block;

import com.mrcrayfish.guns.init.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class HempCropBlock extends CropBlock
{
    public static final IntegerProperty AGE = IntegerProperty.create("age",0,7);
    public HempCropBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    protected ItemLike getBaseSeedId()
    {
        return ModItems.HEMP_SEEDS.get();
    }

    @Override
    public IntegerProperty getAgeProperty()
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

package com.an0m3l1.guns.block;

import com.an0m3l1.guns.blockentity.WorkbenchBlockEntity;
import com.an0m3l1.guns.util.VoxelShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class WorkbenchBlock extends RotatedObjectBlock implements EntityBlock
{
	private final Map<BlockState, VoxelShape> SHAPES = new HashMap<>();
	
	public WorkbenchBlock(Block.Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
	}
	
	private VoxelShape getShape(BlockState state)
	{
		if(SHAPES.containsKey(state))
		{
			return SHAPES.get(state);
		}
		Direction direction = state.getValue(FACING);
		List<VoxelShape> shapes = new ArrayList<>();
		
		shapes.add(Block.box(1, 2, 1, 15, 14, 15));
		shapes.add(Block.box(0, 14, 0, 16, 16, 16));
		shapes.add(Block.box(1, 0, 1, 3, 2, 3));
		shapes.add(Block.box(13, 0, 1, 15, 2, 3));
		shapes.add(Block.box(1, 0, 13, 3, 2, 15));
		shapes.add(Block.box(13, 0, 13, 15, 2, 15));
		
		VoxelShape shape = VoxelShapeHelper.combineAll(shapes);
		SHAPES.put(state, shape);
		return shape;
	}
	
	public @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighbourState, @NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockPos neighbourPos)
	{
		if(state.getValue(BlockStateProperties.WATERLOGGED))
		{
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		return state;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.WATERLOGGED);
	}
	
	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos, @NotNull CollisionContext context)
	{
		return this.getShape(state);
	}
	
	@Override
	public @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos)
	{
		return this.getShape(state);
	}
	
	public @NotNull FluidState getFluidState(BlockState state)
	{
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}
	
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Player playerEntity, @NotNull InteractionHand hand, @NotNull BlockHitResult result)
	{
		if(!world.isClientSide())
		{
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if(tileEntity instanceof MenuProvider)
			{
				NetworkHooks.openScreen((ServerPlayer) playerEntity, (MenuProvider) tileEntity, pos);
			}
		}
		return InteractionResult.SUCCESS;
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
	{
		return new WorkbenchBlockEntity(pos, state);
	}
	
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
		return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(FACING, context.getHorizontalDirection()).setValue(BlockStateProperties.WATERLOGGED, ifluidstate.getType() == Fluids.WATER);
	}
}

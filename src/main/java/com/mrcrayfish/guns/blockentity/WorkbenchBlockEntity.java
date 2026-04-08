package com.mrcrayfish.guns.blockentity;

import com.mrcrayfish.guns.blockentity.inventory.IStorageBlock;
import com.mrcrayfish.guns.common.container.WorkbenchContainer;
import com.mrcrayfish.guns.init.ModTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class WorkbenchBlockEntity extends SyncedBlockEntity implements IStorageBlock
{
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	
	public WorkbenchBlockEntity(BlockPos pos, BlockState state)
	{
		super(ModTileEntities.WORKBENCH.get(), pos, state);
	}
	
	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return this.inventory;
	}
	
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag)
	{
		ContainerHelper.saveAllItems(tag, this.inventory);
	}
	
	@Override
	public void load(@NotNull CompoundTag tag)
	{
		super.load(tag);
		ContainerHelper.loadAllItems(tag, this.inventory);
	}
	
	@Override
	public boolean canPlaceItem(int index, @NotNull ItemStack stack)
	{
		return index != 0 || (stack.getItem() instanceof DyeItem && this.inventory.get(index).getCount() < 1);
	}
	
	@Override
	public boolean stillValid(@NotNull Player player)
	{
		return Objects.requireNonNull(this.level).getBlockEntity(this.worldPosition) == this && player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
	}
	
	@Override
	public @NotNull Component getDisplayName()
	{
		return Component.translatable("container.cgm.workbench");
	}
	
	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity)
	{
		return new WorkbenchContainer(windowId, playerInventory, this);
	}
}

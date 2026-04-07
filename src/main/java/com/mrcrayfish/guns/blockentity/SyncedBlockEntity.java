package com.mrcrayfish.guns.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SyncedBlockEntity extends BlockEntity
{
	public SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}
	
	protected void syncToClient()
	{
		this.setChanged();
		if(this.level != null && !this.level.isClientSide)
		{
			if(this.level instanceof ServerLevel server)
			{
				ClientboundBlockEntityDataPacket packet = this.getUpdatePacket();
				if(packet != null)
				{
					List<ServerPlayer> players = server.getChunkSource().chunkMap.getPlayers(new ChunkPos(this.worldPosition), false);
					players.forEach(player -> player.connection.send(packet));
				}
			}
		}
	}
	
	@Override
	public @NotNull CompoundTag getUpdateTag()
	{
		return this.saveWithFullMetadata();
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket pkt)
	{
		this.deserializeNBT(pkt.getTag());
	}
}
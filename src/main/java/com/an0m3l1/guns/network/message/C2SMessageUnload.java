package com.an0m3l1.guns.network.message;

import com.an0m3l1.guns.common.network.ServerPlayHandler;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload extends PlayMessage<C2SMessageUnload>
{
	private boolean partial;
	
	public C2SMessageUnload()
	{
	}
	
	public C2SMessageUnload(boolean partial)
	{
		this.partial = partial;
	}
	
	@Override
	public void encode(C2SMessageUnload message, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(message.partial);
	}
	
	@Override
	public C2SMessageUnload decode(FriendlyByteBuf buffer)
	{
		boolean partial = buffer.readBoolean();
		return new C2SMessageUnload(partial);
	}
	
	@Override
	public void handle(C2SMessageUnload message, MessageContext context)
	{
		context.execute(() ->
		{
			ServerPlayer player = context.getPlayer();
			if(player != null && !player.isSpectator())
			{
				ServerPlayHandler.handleUnload(player, message.partial);
			}
		});
		context.setHandled(true);
	}
}

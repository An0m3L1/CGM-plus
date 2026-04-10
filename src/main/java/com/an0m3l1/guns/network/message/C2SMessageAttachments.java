package com.an0m3l1.guns.network.message;

import com.an0m3l1.guns.common.network.ServerPlayHandler;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Author: MrCrayfish
 */
public class C2SMessageAttachments extends PlayMessage<C2SMessageAttachments>
{
	public C2SMessageAttachments()
	{
	}
	
	@Override
	public void encode(C2SMessageAttachments message, FriendlyByteBuf buffer)
	{
	}
	
	@Override
	public C2SMessageAttachments decode(FriendlyByteBuf buffer)
	{
		return new C2SMessageAttachments();
	}
	
	@Override
	public void handle(C2SMessageAttachments message, MessageContext context)
	{
		context.execute(() ->
		{
			ServerPlayer player = context.getPlayer();
			if(player != null)
			{
				ServerPlayHandler.handleAttachments(player);
			}
		});
		context.setHandled(true);
	}
}

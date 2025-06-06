package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class C2SMessageAim extends PlayMessage<C2SMessageAim>
{
	private static final UUID AIMING_SPEED_UUID = UUID.fromString("a9f8e3e7-2b4c-4d38-9d6a-57d9a5b6b6a9");
	private boolean aiming;

	public C2SMessageAim() {}

	public C2SMessageAim(boolean aiming)
	{
		this.aiming = aiming;
	}

	@Override
	public void encode(C2SMessageAim message, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(message.aiming);
	}

	@Override
	public C2SMessageAim decode(FriendlyByteBuf buffer)
	{
		return new C2SMessageAim(buffer.readBoolean());
	}

	@Override
	public void handle(C2SMessageAim message, MessageContext context)
	{
		context.execute(() ->
		{
			ServerPlayer player = context.getPlayer();
			if(player != null && !player.isSpectator())
			{
				ModSyncedDataKeys.AIMING.setValue(player, message.aiming);
				AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

				if(speedAttribute != null)
				{
					AttributeModifier modifier = speedAttribute.getModifier(AIMING_SPEED_UUID);
					if(modifier != null)
					{
						speedAttribute.removeModifier(AIMING_SPEED_UUID);
					}

					if(message.aiming)
					{
						float reduction = Config.COMMON.gameplay.aimingMovementSpeedMultiplier.get().floatValue();
						speedAttribute.addTransientModifier(new AttributeModifier(
								AIMING_SPEED_UUID,
								"Aiming speed reduction",
								-reduction,
								AttributeModifier.Operation.MULTIPLY_TOTAL
						));
					}
				}
			}
		});
		context.setHandled(true);
	}
}

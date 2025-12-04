package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.client.network.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class C2SMessageForceSetReserveAmmo extends PlayMessage<C2SMessageForceSetReserveAmmo>
{
    private int ammoAfterLoad;

    public C2SMessageForceSetReserveAmmo() {}

    public C2SMessageForceSetReserveAmmo(int ammoAfterLoad)
    {
        this.ammoAfterLoad = ammoAfterLoad;
    }

    @Override
    public void encode(C2SMessageForceSetReserveAmmo message, FriendlyByteBuf buffer)
    {
        buffer.writeInt(message.ammoAfterLoad);
    }

    @Override
    public C2SMessageForceSetReserveAmmo decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageForceSetReserveAmmo(buffer.readInt());
    }

    @Override
    public void handle(C2SMessageForceSetReserveAmmo message, MessageContext context)
    {
    	context.execute(() -> ClientPlayHandler.handleForceSetReserveAmmo(message));
        context.setHandled(true);
    }

    public int getAmmoAfterLoad()
    {
        return this.ammoAfterLoad;
    }
}
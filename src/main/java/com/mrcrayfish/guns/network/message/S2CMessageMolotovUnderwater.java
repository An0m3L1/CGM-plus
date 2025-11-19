package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.client.network.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

public class S2CMessageMolotovUnderwater extends PlayMessage<S2CMessageMolotovUnderwater>
{
    private double x, y, z;

    public S2CMessageMolotovUnderwater() {}

    public S2CMessageMolotovUnderwater(double x, double y, double z)
    {
        this.z = z;
        this.y = y;
        this.x = x;
    }

    @Override
    public void encode(S2CMessageMolotovUnderwater message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    @Override
    public S2CMessageMolotovUnderwater decode(FriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        return new S2CMessageMolotovUnderwater(x, y, z);
    }

    @Override
    public void handle(S2CMessageMolotovUnderwater message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleMolotovUnderwater(message));
        context.setHandled(true);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double getZ()
    {
        return z;
    }
}
package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.client.network.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

public class S2CMessageIncendiaryGrenade extends PlayMessage<S2CMessageIncendiaryGrenade>
{
    private double x, y, z;

    public S2CMessageIncendiaryGrenade() {}

    public S2CMessageIncendiaryGrenade(double x, double y, double z)
    {
        this.z = z;
        this.y = y;
        this.x = x;
    }

    @Override
    public void encode(S2CMessageIncendiaryGrenade message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    @Override
    public S2CMessageIncendiaryGrenade decode(FriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        return new S2CMessageIncendiaryGrenade(x, y, z);
    }

    @Override
    public void handle(S2CMessageIncendiaryGrenade message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleExplosionIncendiaryGrenade(message));
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
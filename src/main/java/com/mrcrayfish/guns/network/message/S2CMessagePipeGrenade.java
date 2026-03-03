package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.client.network.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;

public class S2CMessagePipeGrenade extends PlayMessage<S2CMessagePipeGrenade>
{
    private double x, y, z;
    private float explosionRadius;

    public S2CMessagePipeGrenade() {}

    public S2CMessagePipeGrenade(double x, double y, double z, float explosionRadius)
    {
        this.z = z;
        this.y = y;
        this.x = x;
        this.explosionRadius = explosionRadius;
    }

    @Override
    public void encode(S2CMessagePipeGrenade message, FriendlyByteBuf buffer)
    {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeFloat(message.explosionRadius);
    }

    @Override
    public S2CMessagePipeGrenade decode(FriendlyByteBuf buffer)
    {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        float explosionRadius = buffer.readFloat();
        return new S2CMessagePipeGrenade(x, y, z, explosionRadius);
    }

    @Override
    public void handle(S2CMessagePipeGrenade message, MessageContext context)
    {
        context.execute(() -> ClientPlayHandler.handleExplosionPipeGrenade(message));
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

    public float getExplosionRadius()
    {
        return explosionRadius;
    }
}
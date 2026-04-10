package com.an0m3l1.guns.network.message;

import com.an0m3l1.guns.client.network.ClientPlayHandler;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;

public class S2CMessageRocket extends PlayMessage<S2CMessageRocket>
{
	private double x, y, z;
	private float explosionRadius;
	
	public S2CMessageRocket()
	{
	}
	
	public S2CMessageRocket(double x, double y, double z, float explosionRadius)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.explosionRadius = explosionRadius;
	}
	
	@Override
	public void encode(S2CMessageRocket message, FriendlyByteBuf buffer)
	{
		buffer.writeDouble(message.x);
		buffer.writeDouble(message.y);
		buffer.writeDouble(message.z);
		buffer.writeFloat(message.explosionRadius);
	}
	
	@Override
	public S2CMessageRocket decode(FriendlyByteBuf buffer)
	{
		double x = buffer.readDouble();
		double y = buffer.readDouble();
		double z = buffer.readDouble();
		float explosionRadius = buffer.readFloat();
		return new S2CMessageRocket(x, y, z, explosionRadius);
	}
	
	@Override
	public void handle(S2CMessageRocket message, MessageContext context)
	{
		context.execute(() -> ClientPlayHandler.handleExplosionRocket(message));
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
package com.mrcrayfish.guns.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.guns.init.ModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Author: MrCrayfish
 */
public record TrailData(boolean enchanted) implements ParticleOptions
{
	public static final Codec<TrailData> CODEC = RecordCodecBuilder.create((builder) -> builder.group(Codec.BOOL.fieldOf("enchanted").forGetter((data) -> data.enchanted)).apply(builder, TrailData::new));
	
	public static final Deserializer<TrailData> DESERIALIZER = new Deserializer<>()
	{
		@Override
		public TrailData fromCommand(ParticleType<TrailData> particleType, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			return new TrailData(reader.readBoolean());
		}
		
		@Override
		public TrailData fromNetwork(ParticleType<TrailData> particleType, FriendlyByteBuf buffer)
		{
			return new TrailData(buffer.readBoolean());
		}
	};
	
	@Override
	public ParticleType<?> getType()
	{
		return ModParticleTypes.TRAIL.get();
	}
	
	@Override
	public void writeToNetwork(FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(this.enchanted);
	}
	
	@Override
	public String writeToString()
	{
		return ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()) + " " + this.enchanted;
	}
}

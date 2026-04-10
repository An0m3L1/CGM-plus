package com.an0m3l1.guns.particles;

import com.an0m3l1.guns.init.ModParticleTypes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public record TrailData(boolean enchanted) implements ParticleOptions
{
	public static final Codec<TrailData> CODEC = RecordCodecBuilder.create((builder) -> builder.group(Codec.BOOL.fieldOf("enchanted").forGetter((data) -> data.enchanted)).apply(builder, TrailData::new));
	
	public static final Deserializer<TrailData> DESERIALIZER = new Deserializer<>()
	{
		@Override
		public @NotNull TrailData fromCommand(@NotNull ParticleType<TrailData> particleType, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			return new TrailData(reader.readBoolean());
		}
		
		@Override
		public @NotNull TrailData fromNetwork(@NotNull ParticleType<TrailData> particleType, FriendlyByteBuf buffer)
		{
			return new TrailData(buffer.readBoolean());
		}
	};
	
	@Override
	public @NotNull ParticleType<?> getType()
	{
		return ModParticleTypes.TRAIL.get();
	}
	
	@Override
	public void writeToNetwork(FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(this.enchanted);
	}
	
	@Override
	public @NotNull String writeToString()
	{
		return ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()) + " " + this.enchanted;
	}
}

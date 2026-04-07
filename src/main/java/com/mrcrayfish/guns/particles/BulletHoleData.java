package com.mrcrayfish.guns.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.guns.init.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public record BulletHoleData(Direction direction, BlockPos pos) implements ParticleOptions
{
	public static final Codec<BulletHoleData> CODEC = RecordCodecBuilder.create((builder) -> builder.group(Codec.INT.fieldOf("dir").forGetter((data) -> data.direction.ordinal()), Codec.LONG.fieldOf("pos").forGetter((p_239806_0_) -> p_239806_0_.pos.asLong())).apply(builder, BulletHoleData::new));
	
	public static final Deserializer<BulletHoleData> DESERIALIZER = new Deserializer<>()
	{
		@Override
		public @NotNull BulletHoleData fromCommand(@NotNull ParticleType<BulletHoleData> particleType, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			int dir = reader.readInt();
			reader.expect(' ');
			long pos = reader.readLong();
			return new BulletHoleData(dir, pos);
		}
		
		@Override
		public @NotNull BulletHoleData fromNetwork(@NotNull ParticleType<BulletHoleData> particleType, FriendlyByteBuf buffer)
		{
			return new BulletHoleData(buffer.readInt(), buffer.readLong());
		}
	};
	
	public BulletHoleData(int dir, long pos)
	{
		this(Direction.values()[dir], BlockPos.of(pos));
	}
	
	@Override
	public @NotNull ParticleType<?> getType()
	{
		return ModParticleTypes.BULLET_HOLE.get();
	}
	
	@Override
	public void writeToNetwork(FriendlyByteBuf buffer)
	{
		buffer.writeEnum(this.direction);
		buffer.writeBlockPos(this.pos);
	}
	
	@Override
	public @NotNull String writeToString()
	{
		return ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()) + " " + this.direction.getName();
	}
}

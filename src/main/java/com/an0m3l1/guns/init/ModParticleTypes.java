package com.an0m3l1.guns.init;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.particles.BulletHoleData;
import com.an0m3l1.guns.particles.TrailData;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class ModParticleTypes
{
	public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, GunMod.MOD_ID);
	
	public static final RegistryObject<ParticleType<BulletHoleData>> BULLET_HOLE = REGISTER.register("bullet_hole", () -> new ParticleType<>(false, BulletHoleData.DESERIALIZER)
	{
		@Override
		public @NotNull Codec<BulletHoleData> codec()
		{
			return BulletHoleData.CODEC;
		}
	});
	public static final RegistryObject<SimpleParticleType> BLOOD = REGISTER.register("blood", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> HEADSHOT = REGISTER.register("headshot", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> EXPLOSION = REGISTER.register("explosion", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> SMOKE_CLOUD = REGISTER.register("smoke_cloud", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> SMOKE_EFFECT = REGISTER.register("smoke_effect", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> CASING = REGISTER.register("casing", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BRASS_CASING = REGISTER.register("brass_casing", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> SHELL_CASING = REGISTER.register("shell_casing", () -> new SimpleParticleType(true));
	public static final RegistryObject<ParticleType<TrailData>> TRAIL = REGISTER.register("trail", () -> new ParticleType<>(false, TrailData.DESERIALIZER)
	{
		@Override
		public @NotNull Codec<TrailData> codec()
		{
			return TrailData.CODEC;
		}
	});
}

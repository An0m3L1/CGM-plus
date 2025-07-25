package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.entity.LightSourceEntity;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.entity.grenade.*;
import com.mrcrayfish.guns.entity.projectile.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.MOD_ID);

    public static final RegistryObject<EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("projectile", ProjectileEntity::new);
    public static final RegistryObject<EntityType<LightBulletEntity>> LIGHT_BULLET = registerBasic("light_bullet", LightBulletEntity::new);
    public static final RegistryObject<EntityType<MediumBulletEntity>> MEDIUM_BULLET = registerBasic("medium_bullet", MediumBulletEntity::new);
    public static final RegistryObject<EntityType<HeavyBulletEntity>> HEAVY_BULLET = registerBasic("heavy_bullet", HeavyBulletEntity::new);
    public static final RegistryObject<EntityType<BuckshotEntity>> BUCKSHOT_SHELL = registerBasic("buckshot_shell", BuckshotEntity::new);
    public static final RegistryObject<EntityType<GrenadeEntity>> GRENADE = registerBasic("grenade", GrenadeEntity::new);
    public static final RegistryObject<EntityType<PipeGrenadeEntity>> PIPE_GRENADE = registerBasic("pipe_grenade", PipeGrenadeEntity::new);
    public static final RegistryObject<EntityType<RocketEntity>> ROCKET = registerBasic("rocket", RocketEntity::new);
    public static final RegistryObject<EntityType<ThrowableGrenadeEntity>> THROWABLE_GRENADE = registerBasic("throwable_grenade", ThrowableGrenadeEntity::new);
    public static final RegistryObject<EntityType<ThrowableImpactGrenadeEntity>> THROWABLE_IMPACT_GRENADE = registerBasic("throwable_impact_grenade", ThrowableImpactGrenadeEntity::new);
    public static final RegistryObject<EntityType<ThrowableStunGrenadeEntity>> THROWABLE_STUN_GRENADE = registerBasic("throwable_stun_grenade", ThrowableStunGrenadeEntity::new);
    public static final RegistryObject<EntityType<ThrowableSmokeGrenadeEntity>> THROWABLE_SMOKE_GRENADE = registerBasic("throwable_smoke_grenade", ThrowableSmokeGrenadeEntity::new);
    public static final RegistryObject<EntityType<ThrowableIncendiaryGrenadeEntity>> THROWABLE_INCENDIARY_GRENADE = registerBasic("throwable_incendiary_grenade", ThrowableIncendiaryGrenadeEntity::new);
    public static final RegistryObject<EntityType<ThrowableMolotovEntity>> THROWABLE_MOLOTOV = registerBasic("throwable_molotov", ThrowableMolotovEntity::new);
    public static final RegistryObject<EntityType<LightSourceEntity>> LIGHT_SOURCE = registerLight("light_source", LightSourceEntity::new);

    private static <T extends Entity> RegistryObject<EntityType<T>> registerBasic(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(256)
                .setUpdateInterval(1)
                .noSummon()
                .fireImmune()
                .setShouldReceiveVelocityUpdates(true).build(id));
    }

    /**
     * Entity registration that prevents the entity from being sent and tracked by clients. Projectiles
     * are rendered separately from Minecraft's entity rendering system and their logic is handled
     * exclusively by the server, why send them to the client. Projectiles also have very short time
     * in the world and are spawned many times a tick. There is no reason to send unnecessary packets
     * when it can be avoided to drastically improve the performance of the game.
     *
     * @param id       the id of the projectile
     * @param function the factory to spawn the projectile for the server
     * @param <T>      an entity that is a projectile entity
     * @return A registry object containing the new entity type
     */
    private static <T extends ProjectileEntity> RegistryObject<EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(0)
                .noSummon()
                .fireImmune()
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> null)
                .build(id));
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> registerLight(String id, BiFunction<EntityType<T>, Level, T> function)
    {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.01F, 0.01F)
                .setTrackingRange(256)
                .setUpdateInterval(1)
                .noSummon()
                .fireImmune()
                .build(id));
    }
}

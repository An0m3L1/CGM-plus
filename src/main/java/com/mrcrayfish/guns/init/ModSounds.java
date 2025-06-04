package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds 
{
	public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.MOD_ID);

	public static final RegistryObject<SoundEvent> ITEM_PISTOL_FIRE = register("item.tactical_pistol.fire");
	public static final RegistryObject<SoundEvent> ITEM_PISTOL_SILENCED_FIRE = register("item.tactical_pistol.silenced_fire");
	public static final RegistryObject<SoundEvent> ITEM_PISTOL_ENCHANTED_FIRE = register("item.tactical_pistol.enchanted_fire");
	public static final RegistryObject<SoundEvent> ITEM_PISTOL_RELOAD = register("item.tactical_pistol.reload");
	public static final RegistryObject<SoundEvent> ITEM_PISTOL_COCK = register("item.tactical_pistol.cock");
	public static final RegistryObject<SoundEvent> ITEM_SHOTGUN_FIRE = register("item.semi_auto_shotgun.fire");
	public static final RegistryObject<SoundEvent> ITEM_SHOTGUN_SILENCED_FIRE = register("item.semi_auto_shotgun.silenced_fire");
	public static final RegistryObject<SoundEvent> ITEM_SHOTGUN_ENCHANTED_FIRE = register("item.semi_auto_shotgun.enchanted_fire");
	public static final RegistryObject<SoundEvent> ITEM_SHOTGUN_COCK = register("item.semi_auto_shotgun.cock");
	public static final RegistryObject<SoundEvent> ITEM_RIFLE_FIRE = register("item.sniper_rifle.fire");
	public static final RegistryObject<SoundEvent> ITEM_RIFLE_SILENCED_FIRE = register("item.sniper_rifle.silenced_fire");
	public static final RegistryObject<SoundEvent> ITEM_RIFLE_ENCHANTED_FIRE = register("item.sniper_rifle.enchanted_fire");
	public static final RegistryObject<SoundEvent> ITEM_RIFLE_COCK = register("item.sniper_rifle.cock");
	public static final RegistryObject<SoundEvent> ITEM_ASSAULT_RIFLE_FIRE = register("item.assault_rifle.fire");
	public static final RegistryObject<SoundEvent> ITEM_ASSAULT_RIFLE_SILENCED_FIRE = register("item.assault_rifle.silenced_fire");
	public static final RegistryObject<SoundEvent> ITEM_ASSAULT_RIFLE_ENCHANTED_FIRE = register("item.assault_rifle.enchanted_fire");
	public static final RegistryObject<SoundEvent> ITEM_ASSAULT_RIFLE_COCK = register("item.assault_rifle.cock");
	public static final RegistryObject<SoundEvent> ITEM_GRENADE_LAUNCHER_FIRE = register("item.grenade_launcher.fire");
	public static final RegistryObject<SoundEvent> ITEM_BAZOOKA_FIRE = register("item.rocket_launcher.fire");
	public static final RegistryObject<SoundEvent> ITEM_MINI_GUN_FIRE = register("item.mini_gun.fire");
	public static final RegistryObject<SoundEvent> ITEM_MINI_GUN_ENCHANTED_FIRE = register("item.mini_gun.enchanted_fire");
	public static final RegistryObject<SoundEvent> ITEM_MACHINE_PISTOL_FIRE = register("item.automatic_pistol.fire");
	public static final RegistryObject<SoundEvent> ITEM_MACHINE_PISTOL_SILENCED_FIRE = register("item.automatic_pistol.silenced_fire");
	public static final RegistryObject<SoundEvent> ITEM_MACHINE_PISTOL_ENCHANTED_FIRE = register("item.automatic_pistol.enchanted_fire");
	public static final RegistryObject<SoundEvent> ITEM_HEAVY_RIFLE_FIRE = register("item.heavy_sniper_rifle.fire");
	public static final RegistryObject<SoundEvent> ITEM_HEAVY_RIFLE_SILENCED_FIRE = register("item.heavy_sniper_rifle.silenced_fire");
	public static final RegistryObject<SoundEvent> ITEM_HEAVY_RIFLE_ENCHANTED_FIRE = register("item.heavy_sniper_rifle.enchanted_fire");
	public static final RegistryObject<SoundEvent> ITEM_HEAVY_RIFLE_COCK = register("item.heavy_sniper_rifle.cock");
	public static final RegistryObject<SoundEvent> ITEM_GRENADE_PIN = register("item.grenade.pin");
	public static final RegistryObject<SoundEvent> ITEM_GRENADE_LIGHT = register("item.grenade.light");
	public static final RegistryObject<SoundEvent> ENTITY_ROCKET_EXPLOSION = register("entity.rocket.explosion");
	public static final RegistryObject<SoundEvent> ENTITY_PIPE_GRENADE_EXPLOSION = register("entity.pipe_grenade.explosion");
	public static final RegistryObject<SoundEvent> ENTITY_GRENADE_EXPLOSION = register("entity.grenade.explosion");
	public static final RegistryObject<SoundEvent> ENTITY_INCENDIARY_GRENADE_EXPLOSION = register("entity.incendiary_grenade.explosion");
	public static final RegistryObject<SoundEvent> ENTITY_MOLOTOV_EXPLOSION = register("entity.molotov.explosion");
	public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_EXPLOSION = register("entity.stun_grenade.explosion");
	public static final RegistryObject<SoundEvent> ENTITY_SMOKE_GRENADE_EXPLOSION = register("entity.smoke_grenade.explosion");
	public static final RegistryObject<SoundEvent> ENTITY_STUN_GRENADE_RING = register("entity.stun_grenade.ring");
	public static final RegistryObject<SoundEvent> ENTITY_FLYBY = register("entity.flyby.flyby");
	public static final RegistryObject<SoundEvent> UI_WEAPON_ATTACH = register("ui.weapon.attach");
	public static final RegistryObject<SoundEvent> ITEM_EMPTY_CLICK = register("item.empty_click");
	public static final RegistryObject<SoundEvent> GRENADE_THROW = register("item.grenade.throw");

	private static RegistryObject<SoundEvent> register(String key)
	{
		return REGISTER.register(key, () -> new SoundEvent(new ResourceLocation(Reference.MOD_ID, key)));
	}
}

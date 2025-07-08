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

	/* Assault rifles */
	public static final RegistryObject<SoundEvent> ASSAULT_RIFLE_FIRE = register("item.assault_rifle.fire");
	public static final RegistryObject<SoundEvent> ASSAULT_RIFLE_SILENCED_FIRE = register("item.assault_rifle.silenced_fire");
	public static final RegistryObject<SoundEvent> ASSAULT_RIFLE_MAG_OUT = register("item.assault_rifle.mag_out");
	public static final RegistryObject<SoundEvent> ASSAULT_RIFLE_MAG_IN = register("item.assault_rifle.mag_in");
	public static final RegistryObject<SoundEvent> ASSAULT_RIFLE_SLAP = register("item.assault_rifle.slap");

	/* Sniper rifles */
	public static final RegistryObject<SoundEvent> HEAVY_SNIPER_RIFLE_FIRE = register("item.heavy_sniper_rifle.fire");
	public static final RegistryObject<SoundEvent> HEAVY_SNIPER_RIFLE_MAG_OUT = register("item.heavy_sniper_rifle.mag_out");
	public static final RegistryObject<SoundEvent> HEAVY_SNIPER_RIFLE_MAG_IN = register("item.heavy_sniper_rifle.mag_in");
	public static final RegistryObject<SoundEvent> HEAVY_SNIPER_RIFLE_COCK = register("item.heavy_sniper_rifle.cock");

	public static final RegistryObject<SoundEvent> SNIPER_RIFLE_FIRE = register("item.sniper_rifle.fire");
	public static final RegistryObject<SoundEvent> SNIPER_RIFLE_SILENCED_FIRE = register("item.sniper_rifle.silenced_fire");
	public static final RegistryObject<SoundEvent> SNIPER_RIFLE_MAG_OUT = register("item.sniper_rifle.mag_out");
	public static final RegistryObject<SoundEvent> SNIPER_RIFLE_MAG_IN = register("item.sniper_rifle.mag_in");
	public static final RegistryObject<SoundEvent> SNIPER_RIFLE_COCK = register("item.sniper_rifle.cock");

	/* Shotguns */
	public static final RegistryObject<SoundEvent> SEMI_AUTO_SHOTGUN_FIRE = register("item.semi_auto_shotgun.fire");
	public static final RegistryObject<SoundEvent> SEMI_AUTO_SHOTGUN_RELOAD = register("item.semi_auto_shotgun.reload");

	/* Machine guns */
	public static final RegistryObject<SoundEvent> MINI_GUN_FIRE = register("item.mini_gun.fire");

	/* Submachine guns */

	/* Pistols */
	public static final RegistryObject<SoundEvent> TACTICAL_PISTOL_FIRE = register("item.tactical_pistol.fire");
	public static final RegistryObject<SoundEvent> TACTICAL_PISTOL_SILENCED_FIRE = register("item.tactical_pistol.silenced_fire");
	public static final RegistryObject<SoundEvent> TACTICAL_PISTOL_MAG_OUT = register("item.tactical_pistol.mag_out");
	public static final RegistryObject<SoundEvent> TACTICAL_PISTOL_MAG_IN = register("item.tactical_pistol.mag_in");
	public static final RegistryObject<SoundEvent> TACTICAL_PISTOL_SLIDE_BACK = register("item.tactical_pistol.slide_back");
	public static final RegistryObject<SoundEvent> TACTICAL_PISTOL_SLIDE_FORWARD = register("item.tactical_pistol.slide_forward");

	public static final RegistryObject<SoundEvent> AUTOMATIC_PISTOL_FIRE = register("item.automatic_pistol.fire");
	public static final RegistryObject<SoundEvent> AUTOMATIC_PISTOL_SILENCED_FIRE = register("item.automatic_pistol.silenced_fire");
	public static final RegistryObject<SoundEvent> AUTOMATIC_PISTOL_MAG_OUT = register("item.automatic_pistol.mag_out");
	public static final RegistryObject<SoundEvent> AUTOMATIC_PISTOL_MAG_IN = register("item.automatic_pistol.mag_in");
	public static final RegistryObject<SoundEvent> AUTOMATIC_PISTOL_SLIDE_BACK = register("item.automatic_pistol.slide_back");
	public static final RegistryObject<SoundEvent> AUTOMATIC_PISTOL_SLIDE_FORWARD = register("item.automatic_pistol.slide_forward");

	/* Explosives */
	public static final RegistryObject<SoundEvent> ROCKET_LAUNCHER_FIRE = register("item.rocket_launcher.fire");
	public static final RegistryObject<SoundEvent> ROCKET_LAUNCHER_RELOAD = register("item.rocket_launcher.reload");

	public static final RegistryObject<SoundEvent> GRENADE_LAUNCHER_FIRE = register("item.grenade_launcher.fire");
	public static final RegistryObject<SoundEvent> GRENADE_LAUNCHER_RELOAD = register("item.grenade_launcher.reload");
	public static final RegistryObject<SoundEvent> GRENADE_LAUNCHER_COCK = register("item.grenade_launcher.cock");

	/* Grenades */
	public static final RegistryObject<SoundEvent> GRENADE_THROW = register("item.grenade.throw");
	public static final RegistryObject<SoundEvent> GRENADE_PIN = register("item.grenade.pin");
	public static final RegistryObject<SoundEvent> GRENADE_LIGHT = register("item.grenade.light");

	/* Explosions */
	public static final RegistryObject<SoundEvent> ROCKET_EXPLOSION = register("entity.rocket.explosion");
	public static final RegistryObject<SoundEvent> PIPE_GRENADE_EXPLOSION = register("entity.pipe_grenade.explosion");
	public static final RegistryObject<SoundEvent> GRENADE_EXPLOSION = register("entity.grenade.explosion");
	public static final RegistryObject<SoundEvent> INCENDIARY_GRENADE_EXPLOSION = register("entity.incendiary_grenade.explosion");
	public static final RegistryObject<SoundEvent> MOLOTOV_EXPLOSION = register("entity.molotov.explosion");
	public static final RegistryObject<SoundEvent> STUN_GRENADE_EXPLOSION = register("entity.stun_grenade.explosion");
	public static final RegistryObject<SoundEvent> STUN_GRENADE_RING = register("entity.stun_grenade.ring");
	public static final RegistryObject<SoundEvent> SMOKE_GRENADE_EXPLOSION = register("entity.smoke_grenade.explosion");

	/* Other sounds */
	public static final RegistryObject<SoundEvent> ATTACHMENT = register("ui.attach");
	public static final RegistryObject<SoundEvent> EMPTY_CLICK = register("item.empty_click");
	public static final RegistryObject<SoundEvent> FIRE_SWITCH = register("item.fire_switch");
	public static final RegistryObject<SoundEvent> LOW_DURABILITY = register("item.low_durability");
	public static final RegistryObject<SoundEvent> FLYBY = register("entity.flyby");

	private static RegistryObject<SoundEvent> register(String key)
	{
		return REGISTER.register(key, () -> new SoundEvent(new ResourceLocation(Reference.MOD_ID, key)));
	}
}

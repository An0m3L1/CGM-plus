package com.mrcrayfish.guns;

import com.mrcrayfish.guns.client.DotRenderMode;
import com.mrcrayfish.guns.client.SwayType;
import com.mrcrayfish.guns.client.screen.ButtonAlignment;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class Config
{
    /**
     * Client related config options
     */
    public static class Client
    {
        public final Sounds sounds;
        public final Display display;
        public final Particle particle;
        public final Controls controls;
        public final Experimental experimental;
        public final ForgeConfigSpec.BooleanValue hideConfigButton;
        public final ForgeConfigSpec.EnumValue<ButtonAlignment> buttonAlignment;

        public Client(ForgeConfigSpec.Builder builder)
        {
            builder.push("client");
            {
                this.sounds = new Sounds(builder);
                this.display = new Display(builder);
                this.particle = new Particle(builder);
                this.controls = new Controls(builder);
                this.experimental = new Experimental(builder);
            }
            builder.pop();
            this.hideConfigButton = builder.comment("If enabled, hides the config button from the attachment screen.").define("hideConfigButton", true);
            this.buttonAlignment = builder.comment("The alignment of the button in the attachment screen.").defineEnum("buttonAlignment", ButtonAlignment.RIGHT);
        }
    }

    /**
     * Sound related config options
     */
    public static class Sounds
    {
        public final ForgeConfigSpec.BooleanValue playHitSound;
        public final ForgeConfigSpec.BooleanValue hitSoundOnlyAgainstPlayers;
        public final ForgeConfigSpec.ConfigValue<String> hitSound;
        public final ForgeConfigSpec.BooleanValue playSoundWhenHeadshot;
        public final ForgeConfigSpec.ConfigValue<String> headshotSound;
        public final ForgeConfigSpec.BooleanValue playSoundWhenCritical;
        public final ForgeConfigSpec.ConfigValue<String> criticalSound;
        public final ForgeConfigSpec.DoubleValue impactSoundDistance;

        public Sounds(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Control sounds triggered by guns.").push("sounds");
            {
            	this.playHitSound = builder.comment("If true, a sound will play when you hit an entity with a gun.").define("playHitSound", true);
            	this.hitSoundOnlyAgainstPlayers = builder.comment("If true, the normal hit sound will only play when hitting players. Does not apply to headshots and crits.").define("hitSoundOnlyAgainstPlayers", true);
                this.hitSound = builder.comment("The sound to play when a hit occurs.").define("hitSound", "minecraft:item.trident.hit");
                this.playSoundWhenHeadshot = builder.comment("If true, a sound will play when you hit a headshot.").define("playSoundWhenHeadshot", false);
                this.headshotSound = builder.comment("The sound to play when a headshot occurs.").define("headshotSound", "minecraft:entity.player.attack.crit");
                this.playSoundWhenCritical = builder.comment("If true, a sound will play when you hit a crit.").define("playSoundWhenCritical", false);
                this.criticalSound = builder.comment("The sound to play when a crit occurs.").define("criticalSound", "minecraft:entity.player.attack.crit");
                this.impactSoundDistance = builder.comment("The maximum distance impact sounds from bullets can be heard.").defineInRange("impactSoundDistance", 16.0, 0.0, 32.0);
            }
            builder.pop();
        }
    }

    /**
     * Display related config options
     */
    public static class Display
    {
        public final ForgeConfigSpec.BooleanValue oldAnimations;
        public final ForgeConfigSpec.ConfigValue<String> crosshair;
        public final ForgeConfigSpec.BooleanValue blendCrosshair;
        public final ForgeConfigSpec.DoubleValue dynamicCrosshairBaseSpread;
        public final ForgeConfigSpec.DoubleValue dynamicCrosshairSpreadMultiplier;
        public final ForgeConfigSpec.DoubleValue dynamicCrosshairReactivity;
        public final ForgeConfigSpec.EnumValue<DotRenderMode> dynamicCrosshairDotMode;
        public final ForgeConfigSpec.BooleanValue onlyRenderDotWhileAiming;
        public final ForgeConfigSpec.DoubleValue dynamicCrosshairDotThreshold;
        public final ForgeConfigSpec.BooleanValue displayAmmoCount;
        public final ForgeConfigSpec.BooleanValue cooldownIndicator;
        public final ForgeConfigSpec.BooleanValue weaponSway;
        public final ForgeConfigSpec.DoubleValue swaySensitivity;
        public final ForgeConfigSpec.EnumValue<SwayType> swayType;
        public final ForgeConfigSpec.BooleanValue cameraRollEffect;
        public final ForgeConfigSpec.DoubleValue cameraRollAngle;
        public final ForgeConfigSpec.BooleanValue restrictCameraRollToWeapons;
        public final ForgeConfigSpec.BooleanValue useOldCameraRecoil;
        public final ForgeConfigSpec.BooleanValue forceFirstPersonOnZoomedAim;
        public final ForgeConfigSpec.DoubleValue firstPersonAimZoomThreshold;
        public final ForgeConfigSpec.BooleanValue sprintAnimation;
        public final ForgeConfigSpec.DoubleValue bobbingIntensity;
        public final ForgeConfigSpec.BooleanValue fireLights;

        public Display(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Configuration for display related options.").push("display");
            {
                this.oldAnimations = builder.comment("If true, uses the old animation poses for weapons.").define("oldAnimations", false);
                this.crosshair = builder.comment("The custom crosshair to use for weapons.").define("crosshair", "cgm:dynamic");
                this.blendCrosshair = builder.comment("If enabled, blends all custom crosshairs to match the behavior of the default crosshair.").define("blendCrosshair", true);
                this.dynamicCrosshairBaseSpread = builder.comment("The resting size of the Dynamic Crosshair when spread is zero.").defineInRange("dynamicCrosshairBaseSpread", 1.0, 0, 5);
                this.dynamicCrosshairSpreadMultiplier = builder.comment("The bloom factor of the Dynamic Crosshair when spread increases.").defineInRange("dynamicCrosshairSpreadMultiplier", 1.0, 1.0, 1.5);
                this.dynamicCrosshairReactivity = builder.comment("How reactive the Dynamic Crosshair is to shooting.").defineInRange("dynamicCrosshairReactivity", 2.0, 0, 10);
                this.dynamicCrosshairDotMode = builder.comment("The rendering mode used for the Dynamic Crosshair's center dot. At Min Spread will only render the dot when gun spread is stable.").defineEnum("dynamicCrosshairDotMode", DotRenderMode.ALWAYS);
                this.onlyRenderDotWhileAiming = builder.comment("If true, the Dynamic Crosshair's center dot will only render while aiming. Obeys dynamicCrosshairDotMode, and has no effect when mode is set to Never.").define("onlyRenderDotWhileAiming", true);
                this.dynamicCrosshairDotThreshold = builder.comment("The threshold of spread (including modifiers) below which the Dynamic Crosshair's center dot is rendered. Affects the At Min Spread and Threshold modes only.").defineInRange("dynamicCrosshairDotThreshold", 0.8, 0, 90);
                this.displayAmmoCount = builder.comment("If enabled, renders a HUD element displaying the gun's ammo count and ammo capacity.").define("displayAmmoCount", true);
                this.cooldownIndicator = builder.comment("If enabled, renders a cooldown indicator to make it easier to learn when you fire again.").define("cooldownIndicator", false);
                this.weaponSway = builder.comment("If enabled, the weapon will sway when the player looks around. This does not affect aiming and is only visual.").define("weaponSway", true);
                this.swaySensitivity = builder.comment("The sensistivity of the visual weapon sway.").defineInRange("swaySensitivity", 0.2, 0.0, 1.0);
                this.swayType = builder.comment("The animation to use for sway. Directional follows the camera, while Drag lags behind").defineEnum("swayType", SwayType.DIRECTIONAL);
                this.cameraRollEffect = builder.comment("If enabled, the camera will roll when strafing while holding a gun.").define("cameraRollEffect", false);
                this.cameraRollAngle = builder.comment("When Camera Roll Effect is enabled, this is the absolute maximum angle the roll on the camera can approach.").defineInRange("cameraRollAngle", 1.5F, 0F, 45F);
                this.restrictCameraRollToWeapons = builder.comment("When enabled, the Camera Roll Effect is only applied when holding a weapon.").define("restrictCameraRollToWeapons", true);
                this.useOldCameraRecoil = builder.comment("Toggles using the default camera recoil  logic from base CGM. Recommended as the new camera recoil is unfinished.").define("useOldCameraRecoil", false);
                this.forceFirstPersonOnZoomedAim = builder.comment("When enabled, temporarily switches the camera to first person while aiming. Aim zoom must be above firstPersonAimZoomThreshold, and only applies to third person rear camera modes.").define("forceFirstPersonOnZoomedAim", true);
                this.firstPersonAimZoomThreshold = builder.comment("The zoom threshold at which the camera switches to first person while aiming. Requires forceFirstPersonOnZoomedAim to be set to true.").defineInRange("firstPersonAimZoomThreshold", 0.25, 0.0, 1.0);
                this.sprintAnimation = builder.comment("Enables the sprinting animation for guns. This only applies to weapons that support a sprinting animation.").define("sprintingAnimation", true);
                this.bobbingIntensity = builder.comment("The intensity of the custom bobbing animation while holding a gun.").defineInRange("bobbingIntensity", 1.0, 0.0, 2.0);
                this.fireLights = builder.comment("Enables dynamic light sources when firing guns.").define("fireLights", true);
            }
            builder.pop();
        }
    }

    /**
     * Particle related config options
     */
    public static class Particle
    {
        public final ForgeConfigSpec.IntValue trailLife;
        public final ForgeConfigSpec.IntValue bulletHoleLifeMin;
        public final ForgeConfigSpec.IntValue bulletHoleLifeMax;
        public final ForgeConfigSpec.DoubleValue bulletHoleFadeThreshold;
        public final ForgeConfigSpec.BooleanValue enableHitParticle;
        public final ForgeConfigSpec.BooleanValue enableHeadshotParticle;
        public final ForgeConfigSpec.BooleanValue enableBlood;
        public final ForgeConfigSpec.DoubleValue impactParticleDistance;

        public Particle(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to particles").push("particle");
            {
                this.trailLife = builder.comment("Duration in ticks before trail will disappear. Set to 0 to disable trails.").defineInRange("trailLife", 1, 0, Integer.MAX_VALUE);
                this.bulletHoleLifeMin = builder.comment("The minimum duration in ticks before bullet holes will disappear.").defineInRange("bulletHoleLifeMin", 200, 0, Integer.MAX_VALUE);
                this.bulletHoleLifeMax = builder.comment("The maximum duration in ticks before bullet holes will disappear.").defineInRange("bulletHoleLifeMax", 300, 0, Integer.MAX_VALUE);
                this.bulletHoleFadeThreshold = builder.comment("The percentage of the maximum life that must pass before particles begin fading away. 0 makes the particles always fade and 1 removes fading completely.").defineInRange("bulletHoleFadeThreshold", 0.98, 0, 1.0);
                this.enableHitParticle = builder.comment("If true, particles will spawn from entities that are hit by a projectile.").define("enableHitParticle", true);
                this.enableHeadshotParticle = builder.comment("If true, particles will spawn from entities that are hit in the head.").define("enableHeadshotParticle", true);
                this.enableBlood = builder.comment("If enabled, replaces hit particles with blood.").define("enableBlood", true);
                this.impactParticleDistance = builder.comment("The maximum distance impact particles can be seen from.").defineInRange("impactParticleDistance", 256.0, 0.0, Double.MAX_VALUE);
            }
            builder.pop();
        }
    }

    public static class Controls
    {
        public final ForgeConfigSpec.DoubleValue aimDownSightSensitivity;
        public final ForgeConfigSpec.BooleanValue flipControls;

        public Controls(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to controls.").push("controls");
            {
                this.aimDownSightSensitivity = builder.comment("A value to multiply mouse sensitivity by when ADS.").defineInRange("aimDownSightSensitivity", 1.0, 0.0, 1.0);
                this.flipControls = builder.comment("When enabled, switches the shoot and aim controls of weapons. Due to technical reasons, you won't be able to use offhand items if you enable this setting.").define("flipControls", false);
            }
            builder.pop();
        }
    }

    public static class Experimental
    {
        public Experimental(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Experimental options").push("experimental");
            {
            }
            builder.pop();
        }
    }

    /**
     * Common config options
     */
    public static class Common
    {
        public final Gameplay gameplay;
        public final Network network;
        public final AggroMobs aggroMobs;
        public final Explosives explosives;
        public final StunGrenades stunGrenades;
        public final ProjectileSpread projectileSpread;

        public Common(ForgeConfigSpec.Builder builder)
        {
            builder.push("common");
            {
                this.gameplay = new Gameplay(builder);
                this.network = new Network(builder);
                this.aggroMobs = new AggroMobs(builder);
                this.explosives = new Explosives(builder);
                this.stunGrenades = new StunGrenades(builder);
                this.projectileSpread = new ProjectileSpread(builder);
            }
            builder.pop();
        }
    }

    /**
     * Gameplay related config options
     */
    public static class Gameplay
    {
        public final Griefing griefing;
        public final ForgeConfigSpec.DoubleValue growBoundingBoxAmount;
        public final ForgeConfigSpec.BooleanValue enableHeadShots;
        public final ForgeConfigSpec.BooleanValue enableImmuneEntities;
        public final ForgeConfigSpec.DoubleValue headShotDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue criticalDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue enableResistantEntities;
        public final ForgeConfigSpec.DoubleValue resistantDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue ignoreLeaves;
        public final ForgeConfigSpec.BooleanValue enableKnockback;
        public final ForgeConfigSpec.DoubleValue knockbackStrength;
        public final ForgeConfigSpec.BooleanValue improvedHitboxes;
        public final ForgeConfigSpec.DoubleValue aimingMovementSpeedMultiplier;


        public Gameplay(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to gameplay.").push("gameplay");
            {
                this.griefing = new Griefing(builder);
                this.growBoundingBoxAmount = builder.comment("The extra amount to expand an entity's bounding box when checking for projectile collision.").defineInRange("growBoundingBoxAmount", 0.1, 0.0, 1.0);
                this.enableImmuneEntities = builder.comment("If true, entities with tag 'hit_immune' can't be hit with projectiles.").define("enableImmuneEntities", true);
                this.enableHeadShots = builder.comment("If true, headshots deal extra damage.").define("enableHeadShots", true);
                this.headShotDamageMultiplier = builder.comment("The value to multiply the damage by if a headshot occurs.").defineInRange("headShotDamageMultiplier", 2.0, 1.0, Double.MAX_VALUE);
                this.criticalDamageMultiplier = builder.comment("The value to multiply the damage by if a crit occurs.").defineInRange("criticalDamageMultiplier", 1.5, 1.0, Double.MAX_VALUE);
                this.enableResistantEntities = builder.comment("If true, entities with tag 'hit_resistant' take less damage and disallow projectile piercing.").define("enableResistantEntities", true);
                this.resistantDamageMultiplier = builder.comment("The value to multiply the damage by if projectile hits a resistant entity.").defineInRange("resistantDamageMultiplier", 0.5, 0.0, 1.0);
                this.ignoreLeaves = builder.comment("If true, projectiles will ignore leaves when checking for collision.").define("ignoreLeaves", true);
                this.enableKnockback = builder.comment("If true, projectiles will cause knockback when an entity is hit.").define("enableKnockback", true);
                this.knockbackStrength = builder.comment("Sets the strength of knockback when hit. Knockback must be enabled for this to take effect. If value is equal to zero, knockback will use default minecraft value.").defineInRange("knockbackStrength", 0.1, 0.0, 1.0);
                this.improvedHitboxes = builder.comment("If true, improves the accuracy of weapons by considering the ping of the player. This has no affect on singleplayer. This will add a little overhead if enabled.").define("improvedHitboxes", true);
                this.aimingMovementSpeedMultiplier = builder.comment("Sets the multiplier to movement speed when aiming. Set to 1.0 to disable slowing down when aiming.").defineInRange("aimingMovementSpeedMultiplier", 0.65,0.0,1.0);
            }
            builder.pop();
        }
    }

    /**
     * Gun griefing related config options
     */
    public static class Griefing
    {
        public final ForgeConfigSpec.BooleanValue enableFragileBreaking;
        public final ForgeConfigSpec.BooleanValue fragileBlockDrops;
        public final ForgeConfigSpec.DoubleValue fragileBaseBreakChance;
        public final ForgeConfigSpec.BooleanValue setFireToBlocks;

        public Griefing(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties related to gun griefing.").push("griefing");
            {
                this.setFireToBlocks = builder.comment("If true, projectiles will set blocks on fire.").define("setFireToBlocks", false);
                this.enableFragileBreaking = builder.comment("If enabled, projectiles will destroy fragile blocks.").define("enableFragileBreaking", true);
                this.fragileBlockDrops = builder.comment("If enabled, fragile blocks will drop when broken.").define("fragileBlockDrops", false);
                this.fragileBaseBreakChance = builder.comment("The base chance that a fragile block is broken when hit by a bullet. The hardness of a block will scale this value; the harder the block, the lower the final calculated chance will be.").defineInRange("fragileBlockBreakChance", 1.0, 0.0, 1.0);
            }
            builder.pop();
        }
    }

    /**
     * Network related config options
     */
    public static class Network
    {
        public final ForgeConfigSpec.DoubleValue projectileTrackingRange;

        public Network(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to network.").push("network");
            {
                this.projectileTrackingRange = builder.comment("The distance players need to be within to be able to track new projectiles trails. Higher values means you can see projectiles from that start from further away.").defineInRange("projectileTrackingRange", 256.0, 1, Double.MAX_VALUE);
            }
            builder.pop();
        }
    }

    /**
     * Mob aggression related config options
     */
    public static class AggroMobs
    {
        public final ForgeConfigSpec.BooleanValue enabled;
        public final ForgeConfigSpec.BooleanValue angerHostileMobs;
        public final ForgeConfigSpec.DoubleValue unsilencedRange;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> exemptEntities;

        public AggroMobs(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to mob aggression.").push("aggro_mobs");
            {
                this.enabled = builder.comment("If true, nearby mobs are angered and/or scared by gun fire.").define("enabled", true);
                this.angerHostileMobs = builder.comment("If true, in addition to causing peaceful mobs to panic, firing a gun will also cause nearby hostile mobs to target the shooter.").define("angerHostileMobs", true);
                this.unsilencedRange = builder.comment("Any mobs within a sphere of this radius will aggro on the shooter of an unsilenced gun.").defineInRange("unsilencedRange", 128.0, 0.0, Double.MAX_VALUE);
                this.exemptEntities = builder.comment("Any mobs from this list will not aggro.").defineList("exemptMobs", Collections.emptyList(), o -> true);
            }
            builder.pop();
        }
    }

    /**
     * Explosive related config options
     */
    public static class Explosives
    {
        public final ForgeConfigSpec.BooleanValue explosionGriefing;
        public final ForgeConfigSpec.DoubleValue rocketExplosionRadius;
        public final ForgeConfigSpec.BooleanValue rocketExplosionGriefing;
        public final ForgeConfigSpec.DoubleValue handGrenadeExplosionRadius;
        public final ForgeConfigSpec.DoubleValue handGrenadeExplosionDamage;
        public final ForgeConfigSpec.BooleanValue handGrenadeExplosionGriefing;
        public final ForgeConfigSpec.DoubleValue pipeGrenadeExplosionRadius;
        public final ForgeConfigSpec.BooleanValue pipeGrenadeExplosionGriefing;
        public final ForgeConfigSpec.DoubleValue smokeGrenadeCloudDiameter;
        public final ForgeConfigSpec.DoubleValue smokeGrenadeDamage;
        public final ForgeConfigSpec.DoubleValue smokeGrenadeCloudDuration;
        public final ForgeConfigSpec.DoubleValue incendiaryGrenadeExplosionRadius;
        public final ForgeConfigSpec.IntValue incendiaryGrenadeFireDuration;
        public final ForgeConfigSpec.DoubleValue molotovExplosionRadius;
        public final ForgeConfigSpec.IntValue molotovFireDuration;

        public Explosives(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to explosives.").push("explosives");
            {
                this.explosionGriefing = builder.comment("If enabled, explosions will destroy blocks. Doesn't affect Incendiary and Molotov Grenades.").define("explosionGriefing", false);
                this.rocketExplosionRadius = builder.comment("Radius of a Rocket explosion.").defineInRange("rocketExplosionRadius", 2.75, 0.0, Double.MAX_VALUE);
                this.rocketExplosionGriefing = builder.comment("If enabled, Rockets will destroy blocks. Requires explosion griefing to be true.").define("rocketExplosionGriefing", true);
                this.handGrenadeExplosionRadius = builder.comment("Radius of a Grenade explosion.").defineInRange("handGrenadeExplosionRadius", 2.25, 0.0, Double.MAX_VALUE);
                this.handGrenadeExplosionDamage = builder.comment("Damage of a Grenade explosion.").defineInRange("handGrenadeExplosionDamage", 15.0, 0.0, Double.MAX_VALUE);
                this.handGrenadeExplosionGriefing = builder.comment("If enabled, Grenades will destroy blocks. Requires explosion griefing to be true.").define("handGrenadeExplosionGriefing", false);
                this.pipeGrenadeExplosionRadius = builder.comment("Radius of a Pipe Grenade explosion.").defineInRange("pipeGrenadeExplosionRadius", 2.5, 0.0, Double.MAX_VALUE);
                this.pipeGrenadeExplosionGriefing = builder.comment("If enabled, Pipe Grenades will destroy blocks. Requires explosion griefing to be true.").define("pipeGrenadeExplosionGriefing", true);
                this.smokeGrenadeCloudDiameter = builder.comment("Diameter of a Smoke Grenade cloud. Use cautiously when setting high, might cause lag.").defineInRange("smokeGrenadeCloudDiameter", 5.0, 0.0, Double.MAX_VALUE);
                this.smokeGrenadeDamage = builder.comment("Damage per second inside a Smoke Grenade cloud.").defineInRange("smokeGrenadeDamage", 1.0, 0.0, Double.MAX_VALUE);
                this.smokeGrenadeCloudDuration = builder.comment("Duration of a Smoke Grenade cloud in seconds.").defineInRange("smokeGrenadeCloudDuration", 20.0, 0.0, Double.MAX_VALUE);
                this.incendiaryGrenadeExplosionRadius = builder.comment("Radius of a Incendiary Grenade explosion.").defineInRange("incendiaryGrenadeExplosionRadius", 3.5, 0.0, Double.MAX_VALUE);
                this.incendiaryGrenadeFireDuration = builder.comment("Duration of fire forcefully set on entities by the Incendiary Grenade.").defineInRange("incendiaryGrenadeFireDuration", 15, 0, Integer.MAX_VALUE);
                this.molotovExplosionRadius = builder.comment("Radius of a Molotov Cocktail explosion.").defineInRange("molotovExplosionRadius", 2.5, 0.0, Double.MAX_VALUE);
                this.molotovFireDuration = builder.comment("Duration of fire forcefully set on entities by the Molotov Cocktail.").defineInRange("molotovFireDuration", 10, 0, Integer.MAX_VALUE);
            }
            builder.pop();
        }
    }

    /**
     * Stun Grenade related config options
     */
    public static class StunGrenades
    {
        public final Blind blind;
        public final Stun stun;

        public StunGrenades(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to Stun Grenades.").push("stun_grenades");
            {
                this.blind = new Blind(builder);
                this.stun = new Stun(builder);
            }
            builder.pop();
        }
    }

    /**
     * Stun grenade blinding related config options
     */
    public static class Blind
    {
        public final EffectCriteria criteria;
        public final ForgeConfigSpec.BooleanValue blindMobs;

        public Blind(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Blinding properties of Stun Grenades.").push("blind");
            {
                this.criteria = new EffectCriteria(builder, 32, 6, 1, 180, 0.75);
                this.blindMobs = builder.comment("If true, hostile mobs will be unable to target entities while they are blinded by a Stun Grenade.").define("blindMobs", true);
            }
            builder.pop();
        }
    }

    /**
     * Stun grenade deafening related config options
     */
    public static class Stun
    {
        public final EffectCriteria criteria;
        public final ForgeConfigSpec.BooleanValue panicMobs;

        public Stun(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Deafening properties of Stun Grenades").push("deafen");
            {
                this.criteria = new EffectCriteria(builder, 32, 6, 1, 360, 0.75);
                this.panicMobs = builder.comment("If true, peaceful mobs will panic upon being stunned by a Stun Grenade.").define("panicMobs", true);
            }
            builder.pop();
        }
    }

    /**
     * Config options for effect criteria
     */
    public static class EffectCriteria
    {
        public final ForgeConfigSpec.DoubleValue radius;
        public final ForgeConfigSpec.IntValue durationMax;
        public final ForgeConfigSpec.IntValue durationMin;
        public final ForgeConfigSpec.DoubleValue angleEffect;
        public final ForgeConfigSpec.DoubleValue angleAttenuationMax;

        public EffectCriteria(ForgeConfigSpec.Builder builder, double radius, int durationMax, int durationMin, double angleEffect, double angleAttenuationMax)
        {
            builder.push("effect_criteria");
            {
                this.radius = builder.comment("Radius of a Stun Grenade explosion.").defineInRange("radius", radius, 0.0, Double.MAX_VALUE);
                this.durationMax = builder.comment("Maximum duration of the effect in seconds.").defineInRange("durationMax", durationMax, 0, Integer.MAX_VALUE);
                this.durationMin = builder.comment("Minumum duration of the effect in seconds.").defineInRange("durationMin", durationMin, 0, Integer.MAX_VALUE);
                this.angleEffect = builder.comment("Angle between the looking direction and a Stun Grenade must be no more than half this many degrees to have an effect.").defineInRange("angleEffect", angleEffect, 0, 360);
                this.angleAttenuationMax = builder.comment("After duration is attenuated by distance, it will be further attenuated depending on the angle (in degrees) between the looking direction and the Stun Grenade. This is done by multiplying it by 1 (no attenuation) if the angle is 0; and by this value if the angle is the maximum within the angle of effect.").defineInRange("angleAttenuationMax", angleAttenuationMax, 0.0, 1.0);
            }
            builder.pop();
        }
    }

    /**
     * Projectile spread config options
     */
    public static class ProjectileSpread
    {
        public final ForgeConfigSpec.IntValue spreadThreshold;
        public final ForgeConfigSpec.IntValue maxCount;
        public final ForgeConfigSpec.BooleanValue doSpreadHipFirePenalty;

        public ProjectileSpread(ForgeConfigSpec.Builder builder)
        {
            builder.comment("Properties relating to projectile spread.").push("projectile_spread");
            {
                this.spreadThreshold = builder.comment("The amount of time in milliseconds (1/50th of a tick) before projectile spread resets to its resting value. The value indicates a reasonable amount of time before a weapon is considered stable again.").defineInRange("spreadThreshold", 350, 0, 1000);
                this.maxCount = builder.comment("The amount of times a player has to shoot within the spread threshold before the maximum amount of spread is applied. Setting the value higher means it will take longer for the spread to be applied.").defineInRange("maxCount", 10, 1, Integer.MAX_VALUE);
                this.doSpreadHipFirePenalty = builder.comment("When enabled, spread increases faster when not aiming.").define("doSpreadHipFirePenalty", true);
            }
            builder.pop();
        }
    }

    /**
     * Server related config options
     */
    public static class Server
    {
        public final ForgeConfigSpec.IntValue alphaOverlay;
        public final ForgeConfigSpec.IntValue alphaFadeThreshold;
        public final ForgeConfigSpec.DoubleValue soundPercentage;
        public final ForgeConfigSpec.IntValue soundFadeThreshold;
        public final ForgeConfigSpec.DoubleValue ringVolume;
        public final ForgeConfigSpec.DoubleValue gunShotSoundDistance;
        public final ForgeConfigSpec.DoubleValue rocketExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue pipeGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue handGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue stunGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue smokeGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue incendiaryGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue molotovExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue reloadSoundDistance;
        public final ForgeConfigSpec.BooleanValue enableCameraRecoil;
        public final ForgeConfigSpec.IntValue cooldownThreshold;
        public final Experimental experimental;

        public Server(ForgeConfigSpec.Builder builder)
        {
            builder.push("server");
            {
                builder.comment("Stun Grenade related properties.").push("stun_grenade");
                {
                    this.alphaOverlay = builder.comment("After the duration drops to this many ticks, the transparency of the overlay when blinded will gradually fade to 0 alpha.").defineInRange("alphaOverlay", 255, 0, 255);
                    this.alphaFadeThreshold = builder.comment("Transparency of the overlay when blinded will be this alpha value, before eventually fading to 0 alpha.").defineInRange("alphaFadeThreshold", 60, 0, Integer.MAX_VALUE);
                    this.soundPercentage = builder.comment("Volume of most game sounds when deafened will play at this percent, before eventually fading back to %100.").defineInRange("soundPercentage", 0.1, 0.0, 1.0);
                    this.soundFadeThreshold = builder.comment("After the duration drops to this many ticks, the ringing volume will gradually fade to 0 and other sound volumes will fade back to %100.").defineInRange("soundFadeThreshold", 100, 0, Integer.MAX_VALUE);
                    this.ringVolume = builder.comment("Volume of the ringing sound when deafened will play at this volume, before eventually fading to 0.").defineInRange("ringVolume", 0.75, 0.0, 1.0);
                }
                builder.pop();

                builder.comment("Audio properties").push("audio");
                {
                    this.gunShotSoundDistance = builder.comment("The maximum distance weapons can be heard by players.").defineInRange("gunShotMaxDistance", 96, 0, Double.MAX_VALUE);
                    this.rocketExplosionSoundDistance = builder.comment("The maximum distance rocket explosions can be heard by players.").defineInRange("rocketExplosionMaxDistance", 96, 0, Double.MAX_VALUE);
                    this.pipeGrenadeExplosionSoundDistance = builder.comment("The maximum distance pipe grenade explosions can be heard by players.").defineInRange("pipeGrenadeExplosionMaxDistance", 96, 0, Double.MAX_VALUE);
                    this.handGrenadeExplosionSoundDistance = builder.comment("The maximum distance grenade explosions can be heard by players.").defineInRange("handGrenadeExplosionMaxDistance", 64, 0, Double.MAX_VALUE);
                    this.stunGrenadeExplosionSoundDistance = builder.comment("The maximum distance stun grenade explosions can be heard by players.").defineInRange("stunGrenadeExplosionMaxDistance", 64, 0, Double.MAX_VALUE);
                    this.smokeGrenadeExplosionSoundDistance = builder.comment("The maximum distance smoke grenade explosions can be heard by players.").defineInRange("smokeGrenadeExplosionMaxDistance", 48, 0, Double.MAX_VALUE);
                    this.incendiaryGrenadeExplosionSoundDistance = builder.comment("The maximum distance incendiary grenade explosions can be heard by players.").defineInRange("incendiaryGrenadeExplosionMaxDistance", 48, 0, Double.MAX_VALUE);
                    this.molotovExplosionSoundDistance = builder.comment("The maximum distance molotov explosions can be heard by players.").defineInRange("molotovExplosionMaxDistance", 48, 0, Double.MAX_VALUE);
                    this.reloadSoundDistance = builder.comment("The maximum distance reloading can be heard by players.").defineInRange("reloadMaxDistance", 16, 0, Double.MAX_VALUE);
                }
                builder.pop();

                this.enableCameraRecoil = builder.comment("If true, enables camera recoil when firing a weapon.").define("enableCameraRecoil", true);
                this.cooldownThreshold = builder.comment("The maximum amount of cooldown time remaining before the server will accept another shoot packet from a client. This allows for a litle slack since the server may be lagging.").defineInRange("cooldownThreshold", 100, 75, 1000);

                this.experimental = new Experimental(builder);
            }
            builder.pop();
        }

        public static class Experimental
        {
            public final ForgeConfigSpec.BooleanValue forceDyeableAttachments;

            public Experimental(ForgeConfigSpec.Builder builder)
            {
                builder.push("experimental");
                this.forceDyeableAttachments = builder.comment("Forces all attachments to be dyeable regardless if this has an effect on the model. This is useful if your server uses custom models for attachments and the models have dyeable elements.").define("forceDyeableAttachments", false);
                builder.pop();
            }
        }
    }

    static final ForgeConfigSpec clientSpec;
    public static final Config.Client CLIENT;

    static final ForgeConfigSpec commonSpec;
    public static final Config.Common COMMON;

    static final ForgeConfigSpec serverSpec;
    public static final Config.Server SERVER;

    static
    {
        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Config.Client::new);
        clientSpec = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();

        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();

        final Pair<Server, ForgeConfigSpec> serverSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = serverSpecPair.getRight();
        SERVER = serverSpecPair.getLeft();
    }

    public static void saveClientConfig()
    {
        clientSpec.save();
    }
}

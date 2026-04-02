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
     * Client config options
     */
    public static class Client
    {
        /* Crosshair */
        public final ForgeConfigSpec.ConfigValue<String> crosshair;
        public final ForgeConfigSpec.DoubleValue dynamicCrosshairBaseSpread;
        public final ForgeConfigSpec.DoubleValue dynamicCrosshairSpreadMultiplier;
        public final ForgeConfigSpec.DoubleValue dynamicCrosshairReactivity;
        public final ForgeConfigSpec.EnumValue<DotRenderMode> dynamicCrosshairDotMode;
        public final ForgeConfigSpec.BooleanValue onlyRenderDotWhileAiming;
        public final ForgeConfigSpec.BooleanValue blendCrosshair;
        public final ForgeConfigSpec.BooleanValue disableCrosshairForSnipers;
        public final ForgeConfigSpec.DoubleValue dynamicCrosshairDotThreshold;

        /* Weapon Animations */
        public final ForgeConfigSpec.BooleanValue weaponSway;
        public final ForgeConfigSpec.DoubleValue swaySensitivity;
        public final ForgeConfigSpec.EnumValue<SwayType> swayType;
        public final ForgeConfigSpec.BooleanValue sprintAnimation;
        public final ForgeConfigSpec.DoubleValue bobbingIntensity;

        /* Camera */
        public final ForgeConfigSpec.BooleanValue cameraRollEffect;
        public final ForgeConfigSpec.DoubleValue cameraRollAngle;
        public final ForgeConfigSpec.BooleanValue restrictCameraRollToWeapons;
        public final ForgeConfigSpec.BooleanValue forceFirstPersonOnZoomedAim;
        public final ForgeConfigSpec.DoubleValue firstPersonAimZoomThreshold;
        public final ForgeConfigSpec.DoubleValue aimDownSightSensitivity;

        /* HUD */
        public final ForgeConfigSpec.BooleanValue displayAmmoCount;
        public final ForgeConfigSpec.BooleanValue cooldownIndicator;
        public final ForgeConfigSpec.EnumValue<ButtonAlignment> buttonAlignment;
        public final ForgeConfigSpec.BooleanValue hideConfigButton;

        /* Particles */
        public final ForgeConfigSpec.IntValue trailLife;
        public final ForgeConfigSpec.IntValue bulletHoleLifeMin;
        public final ForgeConfigSpec.IntValue bulletHoleLifeMax;
        public final ForgeConfigSpec.DoubleValue bulletHoleFadeThreshold;
        public final ForgeConfigSpec.BooleanValue enableHitParticle;
        public final ForgeConfigSpec.BooleanValue enableHeadshotParticle;
        public final ForgeConfigSpec.BooleanValue enableBlood;
        public final ForgeConfigSpec.DoubleValue impactParticleDistance;

        /* Sounds */
        public final ForgeConfigSpec.BooleanValue playHitSound;
        public final ForgeConfigSpec.BooleanValue hitSoundOnlyAgainstPlayers;
        public final ForgeConfigSpec.ConfigValue<String> hitSound;
        public final ForgeConfigSpec.BooleanValue playSoundWhenHeadshot;
        public final ForgeConfigSpec.ConfigValue<String> headshotSound;
        public final ForgeConfigSpec.BooleanValue playSoundWhenCritical;
        public final ForgeConfigSpec.ConfigValue<String> criticalSound;

        public Client(ForgeConfigSpec.Builder builder)
        {
            builder.push("crosshair");
            {
                this.crosshair = builder.comment("A custom crosshair to use for weapons. Available options: arrow, better_default, box, circle, dot, dynamic, filled_circle, hit_marker, line, round, smiley, square, t, tech.").define("crosshair", "cgm:dynamic");
                this.dynamicCrosshairBaseSpread = builder.comment("Resting size of Dynamic Crosshair when spread is zero.").defineInRange("dynamicCrosshairBaseSpread", 1.0, 0, 5);
                this.dynamicCrosshairSpreadMultiplier = builder.comment("Bloom factor of Dynamic Crosshair when spread increases.").defineInRange("dynamicCrosshairSpreadMultiplier", 1.0, 1.0, 1.5);
                this.dynamicCrosshairReactivity = builder.comment("How reactive Dynamic Crosshair is to shooting.").defineInRange("dynamicCrosshairReactivity", 2.0, 0, 10);
                this.dynamicCrosshairDotMode = builder.comment("Rendering mode used for center dot of Dynamic Crosshair.").defineEnum("dynamicCrosshairDotMode", DotRenderMode.ALWAYS);
                this.onlyRenderDotWhileAiming = builder.comment("If enabled, center dot of Dynamic Crosshair will only render while aiming. Obeys dynamicCrosshairDotMode, and has no effect when mode is set to Never.").define("onlyRenderDotWhileAiming", true);
                this.dynamicCrosshairDotThreshold = builder.comment("Threshold of spread (including modifiers) below which center dot of Dynamic Crosshair is rendered. Affects At Min Spread and Threshold modes only.").defineInRange("dynamicCrosshairDotThreshold", 0.8, 0, 90);
                this.disableCrosshairForSnipers = builder.comment("If enabled, weapons with sniper spreading enabled won't render the crosshair at all.").define("disableCrosshairForSnipers", true);
                this.blendCrosshair = builder.comment("If enabled, blends all custom crosshairs to match behavior of default crosshair.").define("blendCrosshair", true);
            }
            builder.pop();
            builder.push("weapon_animations");
            {
                this.weaponSway = builder.comment("If enabled, weapons will sway when you look around. This does not affect aiming and is only visual.").define("weaponSway", true);
                this.swaySensitivity = builder.comment("Sensitivity of visual weapon sway.").defineInRange("swaySensitivity", 0.2, 0.0, 1.0);
                this.swayType = builder.comment("Animation to use for sway. Directional follows the camera, while Drag lags behind and looks sloppy.").defineEnum("swayType", SwayType.DIRECTIONAL);
                this.sprintAnimation = builder.comment("Enables sprinting animation for weapons. This only applies to weapons that support a sprinting animation.").define("sprintingAnimation", true);
                this.bobbingIntensity = builder.comment("Intensity of the custom bobbing animation while holding a weapon.").defineInRange("bobbingIntensity", 1.0, 0.0, 2.0);
            }
            builder.pop();
            builder.push("camera");
            {
                this.cameraRollEffect = builder.comment("If enabled, the camera will roll when strafing while holding a weapon.").define("cameraRollEffect", false);
                this.cameraRollAngle = builder.comment("When Camera Roll Effect is enabled, this is the absolute maximum angle the roll on the camera can approach.").defineInRange("cameraRollAngle", 1.5F, 0F, 45F);
                this.restrictCameraRollToWeapons = builder.comment("When enabled, the Camera Roll Effect is only applied when holding a weapon.").define("restrictCameraRollToWeapons", true);
                this.forceFirstPersonOnZoomedAim = builder.comment("When enabled, temporarily switches the camera to first person while aiming. Aim zoom must be above firstPersonAimZoomThreshold, and only applies to third person rear camera modes.").define("forceFirstPersonOnZoomedAim", true);
                this.firstPersonAimZoomThreshold = builder.comment("The zoom threshold at which the camera switches to first person while aiming. Requires forceFirstPersonOnZoomedAim to be set to true.").defineInRange("firstPersonAimZoomThreshold", 0.25, 0.0, 1.0);
                this.aimDownSightSensitivity = builder.comment("Mouse sensitivity will be multiplied by this value when ADS. If set to 1.0, mouse sensitivity won't be affected by ADS.").defineInRange("aimDownSightSensitivity", 1.0, 0.0, 1.0);
            }
            builder.pop();
            builder.push("hud");
            {
                this.displayAmmoCount = builder.comment("If enabled, renders a HUD element displaying current ammo count and ammo capacity.").define("displayAmmoCount", true);
                this.cooldownIndicator = builder.comment("If enabled, renders a cooldown indicator to make it easier to learn when you fire again.").define("cooldownIndicator", false);
                this.buttonAlignment = builder.comment("The alignment of the button in the attachment screen.").defineEnum("buttonAlignment", ButtonAlignment.RIGHT);
                this.hideConfigButton = builder.comment("If enabled, hides the config button from the attachment screen.").define("hideConfigButton", true);
            }
            builder.pop();
            builder.push("particles");
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
            builder.push("sounds");
            {
                this.playHitSound = builder.comment("If true, a sound will play when you hit an entity with a weapon.").define("playHitSound", true);
                this.hitSoundOnlyAgainstPlayers = builder.comment("If true, the normal hit sound will only play when hitting players. Does not apply to headshots and crits.").define("hitSoundOnlyAgainstPlayers", true);
                this.hitSound = builder.comment("The sound to play when a hit occurs.").define("hitSound", "minecraft:item.trident.hit");
                this.playSoundWhenHeadshot = builder.comment("If true, a sound will play when you hit a headshot.").define("playSoundWhenHeadshot", false);
                this.headshotSound = builder.comment("The sound to play when a headshot occurs.").define("headshotSound", "minecraft:entity.player.attack.crit");
                this.playSoundWhenCritical = builder.comment("If true, a sound will play when you hit a crit.").define("playSoundWhenCritical", false);
                this.criticalSound = builder.comment("The sound to play when a crit occurs.").define("criticalSound", "minecraft:entity.player.attack.crit");
            }
            builder.pop();
        }
    }

    /**
     * Common config options
     */
    public static class Common
    {
        /* Griefing */
        public final ForgeConfigSpec.BooleanValue projectileGriefing;
        public final ForgeConfigSpec.BooleanValue projectileGriefingBlockDrops;
        public final ForgeConfigSpec.BooleanValue universalExplosionGriefing;
        public final ForgeConfigSpec.IntValue hardnessLowValue;
        public final ForgeConfigSpec.IntValue hardnessMediumValue;
        public final ForgeConfigSpec.IntValue hardnessHighValue;

        /* Entities */
        public final ForgeConfigSpec.BooleanValue scareMobs;
        public final ForgeConfigSpec.BooleanValue angerHostileMobs;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> exemptEntities;
        public final ForgeConfigSpec.DoubleValue growBoundingBoxAmount;
        public final ForgeConfigSpec.BooleanValue enableImmuneEntities;
        public final ForgeConfigSpec.BooleanValue enableResistantEntities;
        public final ForgeConfigSpec.DoubleValue resistantEntitiesDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue aimingMovementSpeedMultiplier;
        public final ForgeConfigSpec.BooleanValue improvedHitboxes;
        public final ForgeConfigSpec.BooleanValue blindMobs;
        public final ForgeConfigSpec.BooleanValue panicMobs;

        /* Weapon properties */
        public final ForgeConfigSpec.IntValue spreadThreshold;
        public final ForgeConfigSpec.IntValue maxCount;
        public final ForgeConfigSpec.BooleanValue doSpreadPenalties;
        public final ForgeConfigSpec.DoubleValue criticalDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue enableHeadShots;
        public final ForgeConfigSpec.BooleanValue enableDynamicLights;
        public final ForgeConfigSpec.DoubleValue headShotDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue projectileTrackingRange;
        public final ForgeConfigSpec.BooleanValue enableDurability;
        public final ForgeConfigSpec.BooleanValue enableKnockback;
        public final ForgeConfigSpec.DoubleValue knockbackStrength;

        /* Debug */
        public final ForgeConfigSpec.BooleanValue showDebugMessages;

        public Common(ForgeConfigSpec.Builder builder)
        {
            builder.push("griefing");
            {
                builder.push("block_hardness_values");
                {
                    this.hardnessLowValue = builder.comment("The amount of penetration power required to break low hardness blocks. (Wooden doors/trapdoors, hay bales, etc.)").defineInRange("hardnessLowValue", 1, 1, Integer.MAX_VALUE);
                    this.hardnessMediumValue = builder.comment("The amount of penetration power required to break medium hardness blocks. (Metal doors/trapdoors, planks, logs, etc.)").defineInRange("hardnessMediumValue", 3, 1, Integer.MAX_VALUE);
                    this.hardnessHighValue = builder.comment("The amount of penetration power required to break high hardness blocks. (Bricks, walls, etc.)").defineInRange("hardnessHighValue", 5, 1, Integer.MAX_VALUE);
                }
                builder.pop();
                this.universalExplosionGriefing = builder.comment("If enabled, explosions will destroy all blocks.").define("universalExplosionGriefing", false);
                this.projectileGriefing = builder.comment("If enabled, projectiles and explosions will destroy fragile and destructible blocks. This doesn't require explosion griefing to be enabled.").define("projectileGriefing", true);
                this.projectileGriefingBlockDrops = builder.comment("If enabled, fragile and destructible blocks will drop when broken.").define("projectileGriefingBlockDrops", false);
            }
            builder.pop();
            builder.push("entities");
            {
                this.scareMobs = builder.comment("If true, nearby mobs are scared by weapon fire.").define("scareMobs", true);
                this.angerHostileMobs = builder.comment("If true, in addition to scaring mobs, firing a weapon will also cause nearby hostile mobs to target the shooter.").define("angerHostileMobs", true);
                this.exemptEntities = builder.comment("Any mobs from this list will not anger.").defineList("exemptMobs", Collections.emptyList(), o -> true);
                this.growBoundingBoxAmount = builder.comment("The extra amount to expand an entity's bounding box when checking for projectile collision.").defineInRange("growBoundingBoxAmount", 0.1, 0.0, 1.0);
                this.enableImmuneEntities = builder.comment("If true, entities with tag 'hit_immune' can't be hit with projectiles.").define("enableImmuneEntities", true);
                this.enableResistantEntities = builder.comment("If true, entities with tag 'hit_resistant' take less damage from weapons and can't be pierced.").define("enableResistantEntities", true);
                this.resistantEntitiesDamageMultiplier = builder.comment("The value to multiply the damage by if projectile hits a resistant entity.").defineInRange("resistantEntitiesDamageMultiplier", 0.5, 0.0, 1.0);
                this.aimingMovementSpeedMultiplier = builder.comment("Sets the multiplier to movement speed when aiming. Set to 1.0 to disable slowing down when aiming.").defineInRange("aimingMovementSpeedMultiplier", 0.65,0.0,1.0);
                this.improvedHitboxes = builder.comment("If true, improves the accuracy of weapons by considering the ping of the player. This has no affect on singleplayer. This will add a little overhead if enabled.").define("improvedHitboxes", true);
                this.blindMobs = builder.comment("If true, hostile mobs will be unable to target entities while they are blinded by a Stun Grenade.").define("blindMobs", true);
                this.panicMobs = builder.comment("If true, peaceful mobs will panic upon being stunned by a Stun Grenade.").define("panicMobs", true);
            }
            builder.pop();
            builder.push("weapon_properties");
            {
                this.criticalDamageMultiplier = builder.comment("The value to multiply the damage by if a crit occurs.").defineInRange("criticalDamageMultiplier", 1.5, 1.0, Double.MAX_VALUE);
                this.enableHeadShots = builder.comment("If true, headshots deal extra damage.").define("enableHeadShots", true);
                this.headShotDamageMultiplier = builder.comment("The value to multiply the damage by if a headshot occurs.").defineInRange("headShotDamageMultiplier", 2.0, 1.0, Double.MAX_VALUE);
                this.enableKnockback = builder.comment("If true, projectiles will cause knockback when an entity is hit.").define("enableKnockback", true);
                this.knockbackStrength = builder.comment("Sets the strength of knockback when hit. Knockback must be enabled for this to take effect. If value is equal to zero, knockback will use default minecraft value.").defineInRange("knockbackStrength", 0.1, 0.0, 1.0);
                this.spreadThreshold = builder.comment("The amount of time in milliseconds (1/50th of a tick) before projectile spread resets to its resting value. The value indicates a reasonable amount of time before a weapon is considered stable again.").defineInRange("spreadThreshold", 350, 0, 1000);
                this.maxCount = builder.comment("The amount of times a player has to shoot within the spread threshold before the maximum amount of spread is applied. Setting the value higher means it will take longer for the spread to be applied.").defineInRange("maxCount", 9, 1, Integer.MAX_VALUE);
                this.doSpreadPenalties = builder.comment("When enabled, spread increases faster when not aiming, sprinting and jumping").define("doSpreadPenalties", true);
                this.enableDynamicLights = builder.comment("If true, weapons, explosions and certain projectiles will produce light. Requires dynamic lights reforged. Works best with 'Realtime' setting.").define("enableDynamicLights", true);
                this.projectileTrackingRange = builder.comment("The distance players need to be within to be able to track new projectiles trails. Higher values means you can see projectiles from that start from further away.").defineInRange("projectileTrackingRange", 256.0, 1, Double.MAX_VALUE);
                this.enableDurability = builder.comment("If true, weapons will decrease in durability and break.").define("enableDurability", true);
            }
            builder.pop();
            this.showDebugMessages = builder.comment("Enables debug logging. May affect performance negatively.").define("showDebugMessages", false);
        }
    }

    /**
     * Server related config options
     */
    public static class Server
    {
        /* Grenades */
        public final ForgeConfigSpec.DoubleValue grenadeExplosionRadius;
        public final ForgeConfigSpec.DoubleValue grenadeExplosionDamage;
        public final ForgeConfigSpec.BooleanValue grenadeExplosionGriefing;
        public final ForgeConfigSpec.DoubleValue grenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue impactGrenadeExplosionRadius;
        public final ForgeConfigSpec.DoubleValue impactGrenadeExplosionDamage;
        public final ForgeConfigSpec.BooleanValue impactGrenadeExplosionGriefing;
        public final ForgeConfigSpec.DoubleValue impactGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue smokeGrenadeCloudRadius;
        public final ForgeConfigSpec.DoubleValue smokeGrenadeDamage;
        public final ForgeConfigSpec.DoubleValue smokeGrenadeCloudDuration;
        public final ForgeConfigSpec.DoubleValue smokeGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue incendiaryGrenadeExplosionRadius;
        public final ForgeConfigSpec.IntValue incendiaryGrenadeFireDuration;
        public final ForgeConfigSpec.DoubleValue incendiaryGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue incendiaryGrenadeExtinguishSoundDistance;
        public final ForgeConfigSpec.DoubleValue molotovExplosionRadius;
        public final ForgeConfigSpec.IntValue molotovFireDuration;
        public final ForgeConfigSpec.DoubleValue molotovExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue molotovBreakSoundDistance;
        public final EffectCriteria blindCriteria;
        public final EffectCriteria stunCriteria;
        public final ForgeConfigSpec.IntValue alphaOverlay;
        public final ForgeConfigSpec.IntValue alphaFadeThreshold;
        public final ForgeConfigSpec.DoubleValue soundPercentage;
        public final ForgeConfigSpec.IntValue soundFadeThreshold;
        public final ForgeConfigSpec.DoubleValue ringVolume;
        public final ForgeConfigSpec.DoubleValue stunGrenadeExplosionSoundDistance;

        /* Sounds */
        public final ForgeConfigSpec.DoubleValue gunShotSoundDistance;
        public final ForgeConfigSpec.DoubleValue reloadSoundDistance;
        public final ForgeConfigSpec.DoubleValue switchSoundDistance;
        public final ForgeConfigSpec.DoubleValue grenadeThrowSoundDistance;
        public final ForgeConfigSpec.DoubleValue grenadePinSoundDistance;
        public final ForgeConfigSpec.DoubleValue grenadeBounceSoundDistance;
        public final ForgeConfigSpec.DoubleValue rocketExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue pipeGrenadeExplosionSoundDistance;
        public final ForgeConfigSpec.DoubleValue projectileImpactSoundDistance;

        /* Other */
        public final ForgeConfigSpec.IntValue cooldownThreshold;
        public final ForgeConfigSpec.BooleanValue forceDyeableAttachments;

        public Server(ForgeConfigSpec.Builder builder)
        {
            builder.push("grenades");
            {
                builder.push("grenade");
                {
                    this.grenadeExplosionRadius = builder.comment("Radius of a Grenade explosion.").defineInRange("grenadeExplosionRadius", 3.5, 0.0, Double.MAX_VALUE);
                    this.grenadeExplosionDamage = builder.comment("Damage of a Grenade explosion.").defineInRange("grenadeExplosionDamage", 22.0, 0.0, Double.MAX_VALUE);
                    this.grenadeExplosionGriefing = builder.comment("If enabled, Grenades will destroy blocks.").define("grenadeExplosionGriefing", true);
                    this.grenadeExplosionSoundDistance = builder.comment("The maximum distance grenade explosions can be heard by players.").defineInRange("grenadeExplosionSoundDistance", 96, 0, Double.MAX_VALUE);
                }
                builder.pop();
                builder.push("impact_grenade");
                {
                    this.impactGrenadeExplosionRadius = builder.comment("Radius of a Impact Grenade explosion.").defineInRange("impactGrenadeExplosionRadius", 2.5, 0.0, Double.MAX_VALUE);
                    this.impactGrenadeExplosionDamage = builder.comment("Damage of a Impact Grenade explosion.").defineInRange("impactGrenadeExplosionDamage", 22.0, 0.0, Double.MAX_VALUE);
                    this.impactGrenadeExplosionGriefing = builder.comment("If enabled, Impact Grenades will destroy blocks.").define("impactGrenadeExplosionGriefing", true);
                    this.impactGrenadeExplosionSoundDistance = builder.comment("The maximum distance impact grenade explosions can be heard by players.").defineInRange("impactGrenadeExplosionSoundDistance", 96, 0, Double.MAX_VALUE);
                }
                builder.pop();
                builder.push("smoke_grenade");
                {
                    this.smokeGrenadeCloudRadius = builder.comment("Radius of a Smoke Grenade cloud. Use cautiously when setting high, might cause lag.").defineInRange("smokeGrenadeCloudRadius", 2.5, 0.0, Double.MAX_VALUE);
                    this.smokeGrenadeDamage = builder.comment("Damage per second inside a Smoke Grenade cloud.").defineInRange("smokeGrenadeDamage", 1.0, 0.0, Double.MAX_VALUE);
                    this.smokeGrenadeCloudDuration = builder.comment("Duration of a Smoke Grenade cloud in seconds.").defineInRange("smokeGrenadeCloudDuration", 20.0, 0.0, Double.MAX_VALUE);
                    this.smokeGrenadeExplosionSoundDistance = builder.comment("The maximum distance smoke grenade explosions can be heard by players.").defineInRange("smokeGrenadeExplosionSoundDistance", 48, 0, Double.MAX_VALUE);
                }
                builder.pop();
                builder.push("incendiary_grenade");
                {
                    this.incendiaryGrenadeExplosionRadius = builder.comment("Radius of a Incendiary Grenade explosion.").defineInRange("incendiaryGrenadeExplosionRadius", 3.0, 0.0, Double.MAX_VALUE);
                    this.incendiaryGrenadeFireDuration = builder.comment("Duration of fire forcefully set on entities by the Incendiary Grenade.").defineInRange("incendiaryGrenadeFireDuration", 15, 0, Integer.MAX_VALUE);
                    this.incendiaryGrenadeExplosionSoundDistance = builder.comment("The maximum distance incendiary grenade explosions can be heard by players.").defineInRange("incendiaryGrenadeExplosionSoundDistance", 64, 0, Double.MAX_VALUE);
                    this.incendiaryGrenadeExtinguishSoundDistance = builder.comment("The maximum distance incendiary grenade extinguished explosions can be heard by players.").defineInRange("incendiaryGrenadeExtinguishSoundDistance", 32, 0, Double.MAX_VALUE);
                }
                builder.pop();
                builder.push("molotov_cocktail");
                {
                    this.molotovExplosionRadius = builder.comment("Radius of a Molotov Cocktail explosion.").defineInRange("molotovExplosionRadius", 2.5, 0.0, Double.MAX_VALUE);
                    this.molotovFireDuration = builder.comment("Duration of fire forcefully set on entities by the Molotov Cocktail.").defineInRange("molotovFireDuration", 10, 0, Integer.MAX_VALUE);
                    this.molotovExplosionSoundDistance = builder.comment("The maximum distance Molotov explosions can be heard by players.").defineInRange("molotovExplosionSoundDistance", 64, 0, Double.MAX_VALUE);
                    this.molotovBreakSoundDistance = builder.comment("The maximum distance Molotov bottle breaking can be heard by players.").defineInRange("molotovBreakSoundDistance", 32, 0, Double.MAX_VALUE);
                }
                builder.pop();
                builder.push("stun_grenade");
                {
                    builder.push("blind");
                    {
                        this.blindCriteria = new EffectCriteria(builder, 16, 6, 1, 180, 0.75);
                    }
                    builder.pop();
                    builder.push("stun");
                    {
                        this.stunCriteria = new EffectCriteria(builder, 16, 6, 1, 360, 0.75);
                    }
                    builder.pop();
                    this.alphaOverlay = builder.comment("After the duration drops to this many ticks, the transparency of the overlay when blinded will gradually fade to 0 alpha.").defineInRange("alphaOverlay", 255, 0, 255);
                    this.alphaFadeThreshold = builder.comment("Transparency of the overlay when blinded will be this alpha value, before eventually fading to 0 alpha.").defineInRange("alphaFadeThreshold", 60, 0, Integer.MAX_VALUE);
                    this.soundPercentage = builder.comment("Volume of most game sounds when deafened will play at this percent, before eventually fading back to 100%.").defineInRange("soundPercentage", 0.1, 0.0, 1.0);
                    this.soundFadeThreshold = builder.comment("After the duration drops to this many ticks, the ringing volume will gradually fade to 0 and other sound volumes will fade back to %100.").defineInRange("soundFadeThreshold", 100, 0, Integer.MAX_VALUE);
                    this.ringVolume = builder.comment("Volume of the ringing sound when deafened will play at this volume, before eventually fading to 0.").defineInRange("ringVolume", 0.75, 0.0, 1.0);
                    this.stunGrenadeExplosionSoundDistance = builder.comment("The maximum distance stun grenade explosions can be heard by players.").defineInRange("stunGrenadeExplosionSoundDistance", 96, 0, Double.MAX_VALUE);
                }
                builder.pop();
            }
            builder.pop();
            builder.push("sounds");
            {
                this.gunShotSoundDistance = builder.comment("The maximum distance weapons can be heard by players.").defineInRange("gunShotSoundDistance", 96, 0, Double.MAX_VALUE);
                this.reloadSoundDistance = builder.comment("The maximum distance reloading can be heard by players.").defineInRange("reloadSoundDistance", 16, 0, Double.MAX_VALUE);
                this.switchSoundDistance = builder.comment("The maximum distance switching weapons can be heard by players.").defineInRange("switchSoundDistance", 8, 0, Double.MAX_VALUE);
                this.grenadeThrowSoundDistance = builder.comment("The maximum distance throwing grenades can be heard by players.").defineInRange("grenadeThrowSoundDistance", 8, 0, Double.MAX_VALUE);
                this.grenadePinSoundDistance = builder.comment("The maximum distance pulling pins from grenades can be heard by players.").defineInRange("grenadePinSoundDistance", 8, 0, Double.MAX_VALUE);
                this.grenadeBounceSoundDistance = builder.comment("The maximum distance grenade bouncing can be heard by players.").defineInRange("grenadeBounceSoundDistance", 24, 0, Double.MAX_VALUE);
                this.projectileImpactSoundDistance = builder.comment("The maximum distance impact sounds from projectiles can be heard by players.").defineInRange("impactSoundDistance", 16.0, 0.0, 32.0);
                this.rocketExplosionSoundDistance = builder.comment("The maximum distance rocket explosions can be heard by players.").defineInRange("rocketExplosionSoundDistance", 128, 0, Double.MAX_VALUE);
                this.pipeGrenadeExplosionSoundDistance = builder.comment("The maximum distance pipe grenade explosions can be heard by players.").defineInRange("pipeGrenadeExplosionSoundDistance", 128, 0, Double.MAX_VALUE);
        }
        builder.pop();
            this.forceDyeableAttachments = builder.comment("Forces all attachments to be dyeable regardless if this has an effect on the model. This is useful if your server uses custom models for attachments and the models have dyeable elements.").define("forceDyeableAttachments", false);
            this.cooldownThreshold = builder.comment("The maximum amount of cooldown time remaining before the server will accept another shoot packet from a client. This allows for a little slack since the server may be lagging.").defineInRange("cooldownThreshold", 100, 75, 1000);
        }
    }

    /**
     * Effect criteria helper
     */
    public static class EffectCriteria {
        public final ForgeConfigSpec.DoubleValue radius;
        public final ForgeConfigSpec.IntValue durationMax;
        public final ForgeConfigSpec.IntValue durationMin;
        public final ForgeConfigSpec.DoubleValue angleEffect;
        public final ForgeConfigSpec.DoubleValue angleAttenuationMax;

        public EffectCriteria(ForgeConfigSpec.Builder builder, double radius, int durationMax, int durationMin, double angleEffect, double angleAttenuationMax)
        {
            this.radius = builder.comment("Radius of explosion effect.").defineInRange("radius", radius, 0.0, Double.MAX_VALUE);
            this.durationMax = builder.comment("Maximum duration of the effect in seconds.").defineInRange("durationMax", durationMax, 0, Integer.MAX_VALUE);
            this.durationMin = builder.comment("Minimum duration of the effect in seconds.").defineInRange("durationMin", durationMin, 0, Integer.MAX_VALUE);
            this.angleEffect = builder.comment("Angle between the looking direction and the explosion source must be no more than half this many degrees to have an effect.").defineInRange("angleEffect", angleEffect, 0, 360);
            this.angleAttenuationMax = builder.comment("After duration is attenuated by distance, it will be further attenuated depending on the angle (in degrees) between the looking direction and the explosion source. This is done by multiplying it by 1 (no attenuation) if the angle is 0; and by this value if the angle is the maximum within the angle of effect.").defineInRange("angleAttenuationMax", angleAttenuationMax, 0.0, 1.0);
        }
    }

    static final ForgeConfigSpec clientSpec;
    public static final Config.Client CLIENT;

    static final ForgeConfigSpec commonSpec;
    public static final Config.Common COMMON;

    static final ForgeConfigSpec serverSpec;
    public static final Config.Server SERVER;

    static {
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

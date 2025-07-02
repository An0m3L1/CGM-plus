package com.mrcrayfish.guns.datagen;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModSounds;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
/**
 * Author: MrCrayfish
 */
public class GunGen extends GunProvider
{
    public GunGen(DataGenerator generator)
    {
        super(generator);
    }

    @Override
    protected void registerGuns()
    {
        /* Assault rifles */

        this.addGun(new ResourceLocation(Reference.MOD_ID, "assault_rifle"), Gun.Builder.create()
                        .setGripType(GripType.TWO_HANDED)
                        .setAuto(true)
                        .setFireRate(2)
                        .setBurstCooldown(4)

                        .setAdsSpeed(0.85F)
                        .setRecoilAdsReduction(0.5F)
                        .setSpreadAdsReduction(0.5F)

                        .setRecoilAngle(3.0F)
                        .setRecoilKick(0.3F)
                        .setRecoilDurationOffset(0.0F)
                        .setSpread(6.0F)

                        .setMaxAmmo(30)
                        .setLightMagAmmo(20)
                        .setExtendedMagAmmo(50)

                        .setUseMagReload(true)
                        .setMagReloadTime(40)
                        .setLightMagReloadTimeModifier(0.9)
                        .setExtendedMagReloadTimeModifier(1.1)

                        .setReloadStartDelay(1)
                        .setReloadInterruptDelay(6)
                        .setReloadEndDelay(1)
                        .setReloadAllowedCooldown(0.0F)

                        .setUseFireModes(true)
                        .setHasSemiMode(true)
                        .setHasAutoMode(true)
                        .setHasBurstMode(false)
                        .setUseAutoBurst(true)
                        .setBurstCount(3)

                        .setBarrel(0.5F,0.0F,0.0F,0.0F)
                        .setScope(1.0F,0.0F,0.0F,0.0F)
                        .setStock(1.0F,0.0F,0.0F,0.0F)
                        .setUnderBarrel(1.0F,0.0F,0.0F,0.0F)
                        .setMagazine(0.0F,0.0F,0.0F,0.0F)
                        .setZoom(Gun.Modules.Zoom.builder().setFovModifier(0.75F))

                        .setAmmo(ModItems.MEDIUM_BULLET.get())
                        .setDamage(6.0F)
                        .setHeadshotMultiplierOverride(2.0F)

                        .setProjectileSize(0.1F)
                        .setProjectileSpeed(20.0)
                        .setProjectileLife(20)

                        .setArmorBypass(0.5F)
                        .setProtectionBypass(0.5F)

                        .setBreakFragile(true)
                        .setMaxPierceCount(5)
                        .setPierceDamagePenalty(0.25F)
                        .setPierceDamageMaxPenalty(0.75F)

                        .setFireSound(ModSounds.ASSAULT_RIFLE_FIRE.get())
                        .setSilencedFireSound(ModSounds.ASSAULT_RIFLE_SILENCED_FIRE.get())
                        .setFireSwitchSound(ModSounds.FIRE_SWITCH.get())

                        .setReloadFrames(32)
                        .setReloadClipOut(ModSounds.ASSAULT_RIFLE_MAG_OUT.get())
                        .setReloadClipOutThreshold(6)
                        .setReloadClipIn(ModSounds.ASSAULT_RIFLE_MAG_IN.get())
                        .setReloadClipInThreshold(17)
                        .setReloadLate(ModSounds.ASSAULT_RIFLE_SLAP.get())
                        .setReloadLateThreshold(27)
                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "combat_rifle"), Gun.Builder.create()
                        .setGripType(GripType.TWO_HANDED)
                        .setAuto(true)
                        .setFireRate(2)
                        .setBurstCooldown(4)

                        .setAdsSpeed(0.8F)
                        .setRecoilAdsReduction(0.6F)
                        .setSpreadAdsReduction(0.6F)

                        .setRecoilAngle(3.5F)
                        .setRecoilKick(0.35F)
                        .setRecoilDurationOffset(0.0F)
                        .setSpread(6.5F)

                        .setMaxAmmo(30)
                        .setLightMagAmmo(20)
                        .setExtendedMagAmmo(50)

                        .setUseMagReload(true)
                        .setMagReloadTime(40)
                        .setLightMagReloadTimeModifier(0.9)
                        .setExtendedMagReloadTimeModifier(1.1)

                        .setReloadStartDelay(1)
                        .setReloadInterruptDelay(6)
                        .setReloadEndDelay(1)
                        .setReloadAllowedCooldown(0.0F)

                        .setUseFireModes(true)
                        .setHasSemiMode(true)
                        .setHasAutoMode(true)
                        .setHasBurstMode(false)
                        .setUseAutoBurst(true)
                        .setBurstCount(3)

                        .setBarrel(0.5F,0.0F,0.0F,0.0F)
                        .setStock(1.0F,0.0F,0.0F,0.0F)
                        .setMagazine(0.0F,0.0F,0.0F,0.0F)
                        .setZoom(Gun.Modules.Zoom.builder().setFovModifier(0.75F))

                        .setAmmo(ModItems.MEDIUM_BULLET.get())
                        .setDamage(6.0F)
                        .setHeadshotMultiplierOverride(2.0F)

                        .setProjectileSize(0.1F)
                        .setProjectileSpeed(20.0)
                        .setProjectileLife(20)

                        .setArmorBypass(0.5F)
                        .setProtectionBypass(0.5F)

                        .setBreakFragile(true)
                        .setMaxPierceCount(5)
                        .setPierceDamagePenalty(0.25F)
                        .setPierceDamageMaxPenalty(0.75F)

                        .setFireSound(ModSounds.ASSAULT_RIFLE_FIRE.get())
                        .setSilencedFireSound(ModSounds.ASSAULT_RIFLE_SILENCED_FIRE.get())
                        .setFireSwitchSound(ModSounds.FIRE_SWITCH.get())

                        .setReloadFrames(32)
                        .setReloadClipOut(ModSounds.ASSAULT_RIFLE_MAG_OUT.get())
                        .setReloadClipOutThreshold(6)
                        .setReloadClipIn(ModSounds.ASSAULT_RIFLE_MAG_IN.get())
                        .setReloadClipInThreshold(17)
                        .setReloadLate(ModSounds.ASSAULT_RIFLE_SLAP.get())
                        .setReloadLateThreshold(27)
                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "tactical_rifle"), Gun.Builder.create()
                        .setGripType(GripType.TWO_HANDED)
                        .setAuto(true)
                        .setFireRate(2)
                        .setBurstCooldown(4)

                        .setAdsSpeed(0.9F)
                        .setRecoilAdsReduction(0.4F)
                        .setSpreadAdsReduction(0.4F)

                        .setRecoilAngle(3.0F)
                        .setRecoilKick(0.3F)
                        .setRecoilDurationOffset(0.0F)
                        .setSpread(6.0F)

                        .setMaxAmmo(30)
                        .setLightMagAmmo(20)
                        .setExtendedMagAmmo(50)

                        .setUseMagReload(true)
                        .setMagReloadTime(35)
                        .setLightMagReloadTimeModifier(0.9)
                        .setExtendedMagReloadTimeModifier(1.1)

                        .setReloadStartDelay(1)
                        .setReloadInterruptDelay(6)
                        .setReloadEndDelay(1)
                        .setReloadAllowedCooldown(0.0F)

                        .setUseFireModes(true)
                        .setHasSemiMode(true)
                        .setHasAutoMode(true)
                        .setHasBurstMode(true)
                        .setUseAutoBurst(true)
                        .setBurstCount(3)

                        .setBarrel(0.5F,0.0F,0.0F,0.0F)
                        .setScope(1.0F,0.0F,0.0F,0.0F)
                        .setStock(1.0F,0.0F,0.0F,0.0F)
                        .setUnderBarrel(1.0F,0.0F,0.0F,0.0F)
                        .setMagazine(0.0F,0.0F,0.0F,0.0F)
                        .setTactical(1.0F,0.0F,0.0F,0.0F)
                        .setZoom(Gun.Modules.Zoom.builder().setFovModifier(0.75F))

                        .setAmmo(ModItems.MEDIUM_BULLET.get())
                        .setDamage(6.0F)
                        .setHeadshotMultiplierOverride(2.0F)

                        .setProjectileSize(0.1F)
                        .setProjectileSpeed(20.0)
                        .setProjectileLife(20)

                        .setArmorBypass(0.5F)
                        .setProtectionBypass(0.5F)

                        .setBreakFragile(true)
                        .setMaxPierceCount(5)
                        .setPierceDamagePenalty(0.25F)
                        .setPierceDamageMaxPenalty(0.75F)

                        .setFireSound(ModSounds.ASSAULT_RIFLE_FIRE.get())
                        .setSilencedFireSound(ModSounds.ASSAULT_RIFLE_SILENCED_FIRE.get())
                        .setFireSwitchSound(ModSounds.FIRE_SWITCH.get())

                        .setReloadFrames(32)
                        .setReloadClipOut(ModSounds.ASSAULT_RIFLE_MAG_OUT.get())
                        .setReloadClipOutThreshold(6)
                        .setReloadClipIn(ModSounds.ASSAULT_RIFLE_MAG_IN.get())
                        .setReloadClipInThreshold(17)
                        .setReloadLate(ModSounds.ASSAULT_RIFLE_SLAP.get())
                        .setReloadLateThreshold(27)
                .build());

        /* Sniper rifles */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "heavy_sniper_rifle"), Gun.Builder.create()
                        .setGripType(GripType.TWO_HANDED)
                        .setAuto(false)
                        .setFireRate(29)

                        .setAdsSpeed(0.6F)
                        .setRecoilAdsReduction(0.25F)
                        .setSpreadAdsReduction(1.0F)

                        .setRecoilAngle(8.0F)
                        .setRecoilKick(2.0F)
                        .setRecoilDurationOffset(0.5F)
                        .setSpread(24.0F)
                        .setRestingSpread(12.0F)

                        .setMaxAmmo(5)
                        .setUseMagReload(true)
                        .setMagReloadTime(60)

                        .setReloadStartDelay(1)
                        .setReloadInterruptDelay(6)
                        .setReloadEndDelay(1)
                        .setReloadAllowedCooldown(0.0F)

                        .setScope(1.0F,0.0F,0.0F,0.0F)
                        .setZoom(Gun.Modules.Zoom.builder().setFovModifier(0.75F))

                        .setAmmo(ModItems.HEAVY_BULLET.get())
                        .setDamage(20.0F)
                        .setHeadshotMultiplierOverride(2.5F)

                        .setProjectileSize(0.1F)
                        .setProjectileSpeed(15.0)
                        .setProjectileLife(30)

                        .setArmorBypass(1.0F)
                        .setProtectionBypass(0.75F)

                        .setBreakFragile(true)
                        .setMaxPierceCount(20)
                        .setPierceDamagePenalty(0.25F)
                        .setPierceDamageMaxPenalty(0.75F)

                        .setFireSound(ModSounds.HEAVY_SNIPER_RIFLE_FIRE.get())

                        .setReloadFrames(62)
                        .setReloadClipOut(ModSounds.HEAVY_SNIPER_RIFLE_MAG_OUT.get())
                        .setReloadClipOutThreshold(11)
                        .setReloadClipIn(ModSounds.HEAVY_SNIPER_RIFLE_MAG_IN.get())
                        .setReloadClipInThreshold(30)
                        .setReloadLate(ModSounds.HEAVY_SNIPER_RIFLE_COCK.get())
                        .setReloadLateThreshold(42)
                        .setCockSound(ModSounds.HEAVY_SNIPER_RIFLE_COCK.get())
                        .setCycleDelay(11)
                .build());

        //TODO: Update datagen
        this.addGun(new ResourceLocation(Reference.MOD_ID, "sniper_rifle"), Gun.Builder.create()
                .setFireRate(8)
                .setGripType(GripType.TWO_HANDED)
                .setMaxAmmo(10)
                .setReloadAmount(2)
                .setRecoilAngle(10.0F)
                .setRecoilKick(0.5F)
                .setRecoilAdsReduction(0.5F)
                .setAlwaysSpread(true)
                .setSpread(1.0F)
                .setAmmo(ModItems.HEAVY_BULLET.get())
                .setDamage(15.0F)
                .setProjectileAffectedByGravity(true)
                .setProjectileSize(0.0625F)
                .setProjectileSpeed(20.0F)
                .setProjectileLife(30)
                .setFireSound(ModSounds.SNIPER_RIFLE_FIRE.get())
                .setCockSound(ModSounds.SNIPER_RIFLE_COCK.get())
                .setSilencedFireSound(ModSounds.SNIPER_RIFLE_SILENCED_FIRE.get())
                .setMuzzleFlash(0.5, 0.0, 3.8365, -10.21)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 4.6223, 6.0))
                .setScope(1.0F, 0.0, 4.3, 3.3)
                .setBarrel(0.45F, 0.0, 3.8365,-10.2)
                .setStock(1.0F, 0.0, 3.1294, 8.3)
                .setUnderBarrel(1.0F, 0.0, 2.63, -0.5)
                .build());

        /* Shotguns */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "semi_auto_shotgun"), Gun.Builder.create()
                        .setGripType(GripType.TWO_HANDED)
                        .setAuto(false)
                        .setFireRate(5)

                        .setAdsSpeed(0.85F)
                        .setRecoilAdsReduction(0.35F)
                        .setSpreadAdsReduction(0.0F)

                        .setRecoilAngle(8.0F)
                        .setRecoilKick(1.5F)
                        .setRecoilDurationOffset(0.5F)
                        .setSpread(3.0F)
                        .setAlwaysSpread(true)
                        .setUseShotgunSpread(true)

                        .setMaxAmmo(7)
                        .setProjectileAmount(7)

                        .setReloadAmount(1)
                        .setReloadRate(11)
                        .setReloadStartDelay(6)
                        .setReloadInterruptDelay(6)
                        .setReloadEndDelay(8)
                        .setReloadAllowedCooldown(0.0F)

                        .setScope(1.0F,0.0F,0.0F,0.0F)
                        .setStock(1.0F,0.0F,0.0F,0.0F)
                        .setUnderBarrel(1.0F,0.0F,0.0F,0.0F)
                        .setZoom(Gun.Modules.Zoom.builder().setFovModifier(0.75F))

                        .setAmmo(ModItems.BUCKSHOT_SHELL.get())
                        .setDamage(14.0F)
                        .setHeadshotMultiplierOverride(1.5F)

                        .setProjectileSize(0.1F)
                        .setProjectileSpeed(20.0)
                        .setProjectileLife(7)

                        .setArmorBypass(0.25F)
                        .setProtectionBypass(0.25F)

                        .setBreakFragile(true)
                        .setMaxPierceCount(3)
                        .setPierceDamagePenalty(0.25F)
                        .setPierceDamageMaxPenalty(0.75F)

                        .setFireSound(ModSounds.SEMI_AUTO_SHOTGUN_FIRE.get())
                        .setReloadSound(ModSounds.SEMI_AUTO_SHOTGUN_RELOAD.get())
                .build());

        /* Machine guns */
        //TODO: Update datagen
        this.addGun(new ResourceLocation(Reference.MOD_ID, "mini_gun"), Gun.Builder.create()
                .setAuto(true)
                .setFireRate(2)
                .setGripType(GripType.MINI_GUN)
                .setMaxAmmo(100)
                .setReloadAmount(10)
                .setRecoilAngle(1.0F)
                .setAlwaysSpread(true)
                .setSpread(7.0F)
                .setAmmo(ModItems.LIGHT_BULLET.get())
                .setDamage(5.0F)
                .setProjectileSize(0.2F)
                .setProjectileSpeed(8.0F)
                .setProjectileLife(30)
                .setFireSound(ModSounds.MINI_GUN_FIRE.get())
                .setMuzzleFlash(0.5, 0.0, 2.7, -11.51)
                .build());

        /* Submachine guns */

        /* Pistols */
        this.addGun(new ResourceLocation(Reference.MOD_ID, "tactical_pistol"), Gun.Builder.create()
                        .setGripType(GripType.TWO_HANDED_PISTOL)
                        .setAuto(false)
                        .setFireRate(3)

                        .setAdsSpeed(1.0F)
                        .setRecoilAdsReduction(0.25F)
                        .setSpreadAdsReduction(0.25F)

                        .setRecoilAngle(2.5F)
                        .setRecoilKick(0.25F)
                        .setRecoilDurationOffset(0.0F)
                        .setSpread(4.5F)
                        .setRestingSpread(0.5F)

                        .setMaxAmmo(12)
                        .setLightMagAmmo(10)
                        .setExtendedMagAmmo(17)

                        .setUseMagReload(true)
                        .setMagReloadTime(40)
                        .setLightMagReloadTimeModifier(0.85)
                        .setExtendedMagReloadTimeModifier(1.15)

                        .setReloadStartDelay(1)
                        .setReloadInterruptDelay(6)
                        .setReloadEndDelay(1)
                        .setReloadAllowedCooldown(0.0F)

                        .setBarrel(0.5F,0.0F,0.0F,0.0F)
                        .setMagazine(0.0F,0.0F,0.0F,0.0F)
                        .setZoom(Gun.Modules.Zoom.builder().setFovModifier(0.75F))

                        .setAmmo(ModItems.LIGHT_BULLET.get())
                        .setDamage(5.0F)
                        .setDamageReduceOverLife(true)
                        .setMaxRangeDamageMultiplier(0.5F)
                        .setHeadshotMultiplierOverride(2.0F)

                        .setProjectileSize(0.1F)
                        .setProjectileSpeed(20.0)
                        .setProjectileLife(7)

                        .setArmorBypass(0.5F)
                        .setProtectionBypass(0.25F)

                        .setBreakFragile(true)
                        .setMaxPierceCount(4)
                        .setPierceDamagePenalty(0.25F)
                        .setPierceDamageMaxPenalty(0.75F)

                        .setFireSound(ModSounds.TACTICAL_PISTOL_FIRE.get())
                        .setSilencedFireSound(ModSounds.TACTICAL_PISTOL_SILENCED_FIRE.get())

                        .setReloadFrames(29)
                        .setReloadClipOut(ModSounds.TACTICAL_PISTOL_MAG_OUT.get())
                        .setReloadClipOutThreshold(3.5F)
                        .setReloadClipIn(ModSounds.TACTICAL_PISTOL_MAG_IN.get())
                        .setReloadClipInThreshold(13.1F)
                        .setReloadMid(ModSounds.TACTICAL_PISTOL_SLIDE_BACK.get())
                        .setReloadMidThreshold(20.9F)
                        .setReloadLate(ModSounds.TACTICAL_PISTOL_SLIDE_FORWARD.get())
                        .setReloadLateThreshold(23.5F)
                .build());

        this.addGun(new ResourceLocation(Reference.MOD_ID, "automatic_pistol"), Gun.Builder.create()
                        .setGripType(GripType.TWO_HANDED_PISTOL)
                        .setAuto(true)
                        .setFireRate(1)

                        .setAdsSpeed(1.0F)
                        .setRecoilAdsReduction(0.25F)
                        .setSpreadAdsReduction(0.25F)

                        .setRecoilAngle(2.0F)
                        .setRecoilKick(0.2F)
                        .setRecoilDurationOffset(0.0F)
                        .setSpread(6.0F)
                        .setRestingSpread(1.5F)

                        .setMaxAmmo(32)
                        .setLightMagAmmo(20)
                        .setExtendedMagAmmo(50)

                        .setUseMagReload(true)
                        .setMagReloadTime(40)
                        .setLightMagReloadTimeModifier(0.85)
                        .setExtendedMagReloadTimeModifier(1.15)

                        .setReloadStartDelay(1)
                        .setReloadInterruptDelay(6)
                        .setReloadEndDelay(1)
                        .setReloadAllowedCooldown(0.0F)

                        .setUseFireModes(true)
                        .setHasSemiMode(true)
                        .setHasAutoMode(true)
                        .setHasBurstMode(false)

                        .setScope(0.5F,0.0F,0.0F,0.0F)
                        .setMagazine(0.0F,0.0F,0.0F,0.0F)
                        .setZoom(Gun.Modules.Zoom.builder().setFovModifier(0.75F))

                        .setAmmo(ModItems.LIGHT_BULLET.get())
                        .setDamage(4.0F)
                        .setDamageReduceOverLife(true)
                        .setMaxRangeDamageMultiplier(0.5F)
                        .setHeadshotMultiplierOverride(2.0F)

                        .setProjectileSize(0.1F)
                        .setProjectileSpeed(20.0)
                        .setProjectileLife(5)

                        .setArmorBypass(0.25F)
                        .setProtectionBypass(0.25F)

                        .setBreakFragile(true)
                        .setMaxPierceCount(2)
                        .setPierceDamagePenalty(0.5F)
                        .setPierceDamageMaxPenalty(0.5F)

                        .setFireSound(ModSounds.AUTOMATIC_PISTOL_FIRE.get())

                        .setReloadFrames(29)
                        .setReloadClipOut(ModSounds.AUTOMATIC_PISTOL_MAG_OUT.get())
                        .setReloadClipOutThreshold(3.5F)
                        .setReloadClipIn(ModSounds.AUTOMATIC_PISTOL_MAG_IN.get())
                        .setReloadClipInThreshold(13.1F)
                        .setReloadMid(ModSounds.AUTOMATIC_PISTOL_SLIDE_BACK.get())
                        .setReloadMidThreshold(20.9F)
                        .setReloadLate(ModSounds.AUTOMATIC_PISTOL_SLIDE_FORWARD.get())
                        .setReloadLateThreshold(23.5F)
                .build());

        /* Explosives */
        //TODO: Update datagen
        this.addGun(new ResourceLocation(Reference.MOD_ID, "grenade_launcher"), Gun.Builder.create()
                .setFireRate(20)
                .setGripType(GripType.TWO_HANDED)
                .setMaxAmmo(1)
                .setRecoilAngle(5.0F)
                .setRecoilKick(1.0F)
                .setRecoilDurationOffset(0.25F)
                .setAmmo(ModItems.GRENADE.get())
                .setDamage(15.0F)
                .setProjectileVisible(true)
                .setProjectileSize(0.2F)
                .setProjectileSpeed(1.5F)
                .setProjectileLife(50)
                .setProjectileAffectedByGravity(true)
                .setFireSound(ModSounds.GRENADE_LAUNCHER_FIRE.get())
                .setMuzzleFlash(0.75, 0.0, 3.5, -3.8)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 5.7, 3.0))
                .setScope(1.0F, 0.0, 5.2, 2.7)
                .setStock(1.0F, 0.0, 3.6, 8.2)
                .build());

        //TODO: Update datagen
        this.addGun(new ResourceLocation(Reference.MOD_ID, "rocket_launcher"), Gun.Builder.create()
                .setFireRate(20)
                .setGripType(GripType.TWO_HANDED)
                .setMaxAmmo(1)
                .setRecoilAngle(5.0F)
                .setRecoilKick(1.0F)
                .setRecoilDurationOffset(0.25F)
                .setAmmo(ModItems.ROCKET.get())
                .setDamage(15.0F)
                .setProjectileVisible(true)
                .setProjectileSize(0.2F)
                .setProjectileSpeed(1.5F)
                .setProjectileLife(50)
                .setProjectileAffectedByGravity(true)
                .setFireSound(ModSounds.ROCKET_LAUNCHER_FIRE.get())
                .setMuzzleFlash(0.75, 0.0, 3.5, -3.8)
                .setZoom(Gun.Modules.Zoom.builder()
                        .setFovModifier(0.6F)
                        .setOffset(0.0, 5.7, 3.0))
                .setScope(1.0F, 0.0, 5.2, 2.7)
                .setStock(1.0F, 0.0, 3.6, 8.2)
                .build());
    }
}

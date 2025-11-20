package com.mrcrayfish.guns.client.audio;

import com.mrcrayfish.guns.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.function.Supplier;

public class DistancedSound extends AbstractSoundInstance
{
    public DistancedSound(ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source, Supplier<Number> distanceSupplier)
    {
        super(soundIn, categoryIn, source);
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.attenuation = Attenuation.NONE;

        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null)
        {
            float distance = distanceSupplier.get().floatValue();
            this.volume = volume * (1.0F - Math.min(1.0F, (float) Math.sqrt(player.distanceToSqr(x, y, z)) / distance));
            this.volume *= this.volume; //Ease the volume instead of linear
        }
    }

    public static DistancedSound gunshotOrReload
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, boolean reload, RandomSource source)
    {
        Supplier<Number> distanceSupplier = reload ?
                Config.SERVER.reloadSoundDistance::get :
                Config.SERVER.gunShotSoundDistance::get;
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, distanceSupplier);
    }

    public static DistancedSound grenadeBounce
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.grenadeBounceSoundDistance::get);
    }

    public static DistancedSound grenadeExplosion
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.handGrenadeExplosionSoundDistance::get);
    }

    public static DistancedSound impactGrenadeExplosion
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.handGrenadeExplosionSoundDistance::get);
    }

    public static DistancedSound incendiaryGrenadeExplosion
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.incendiaryGrenadeExplosionSoundDistance::get);
    }

    public static DistancedSound molotovExplosion
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.molotovExplosionSoundDistance::get);
    }

    public static DistancedSound pipeGrenadeExplosion
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.pipeGrenadeExplosionSoundDistance::get);
    }

    public static DistancedSound rocketExplosion
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.rocketExplosionSoundDistance::get);
    }

    public static DistancedSound smokeGrenadeExplosion
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.smokeGrenadeExplosionSoundDistance::get);
    }

    public static DistancedSound stunGrenadeExplosion
            (ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
    {
        return new DistancedSound(soundIn, categoryIn, x, y, z, volume, pitch, source, Config.SERVER.stunGrenadeExplosionSoundDistance::get);
    }
}
package com.mrcrayfish.guns.client.audio;

import com.mrcrayfish.guns.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Author: MrCrayfish
 */
public class ExplosionSound extends AbstractSoundInstance
{
    public ExplosionSound(ResourceLocation soundIn, SoundSource categoryIn, float x, float y, float z, float volume, float pitch, RandomSource source)
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
            float distance = Config.SERVER.grenadeExplosionMaxDistance.get();
            this.volume = volume * (1.0F - Math.min(1.0F, (float) Math.sqrt(player.distanceToSqr(x, y, z)) / distance));
            this.volume *= this.volume; //Ease the volume instead of linear
        }
    }
}

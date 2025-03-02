package com.mrcrayfish.guns.client.particle;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.particles.TrailData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class TrailParticle extends BaseAshSmokeParticle

{
    protected TrailParticle(ClientLevel world, double x, double y, double z, float scale, float red, float green, float blue, SpriteSet spriteSet)
    {
        super(world, x, y, z, 0.0F, 0.0F, 0.0F, 0.0, 0.0, 0.0, scale, spriteSet, 0.2F, 0, 0, false);
        this.lifetime = Config.CLIENT.particle.trailLife.get()-1;
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
        this.alpha = 0.25F;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public int getLightColor(float pPartialTick)
    {
        return 255;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<TrailData>
    {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(TrailData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            float red = 0.99609F;
            float green = 0.91796F;
            float blue = 0.57812F;
            return new TrailParticle(worldIn, x, y, z, 1.0F, red, green, blue, this.spriteSet);
        }
    }
}

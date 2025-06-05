package com.mrcrayfish.guns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class HeadshotParticle extends TextureSheetParticle
{
    public HeadshotParticle(ClientLevel world, double x, double y, double z)
    {
        super(world, x, y, z, 0.1, 0.25, 0.1);
        this.setColor(1.0F, 1.0F, 1.0F);
        this.gravity = 0F;
        this.quadSize = 0.05F;
        this.lifetime = (int)(4.0F);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
    
    public int getLightColor(float pPartialTick)
    {
        return 255;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites)
        {
            this.sprites = sprites;
        }

        public Particle createParticle(@NotNull SimpleParticleType typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            HeadshotParticle particle = new HeadshotParticle(worldIn, x, y, z);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}

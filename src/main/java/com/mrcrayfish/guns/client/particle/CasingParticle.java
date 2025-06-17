package com.mrcrayfish.guns.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CasingParticle extends TextureSheetParticle {
    CasingParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.gravity = 0.75F;
        this.friction = 0.999F;
        this.quadSize = 0.35F;
        this.lifetime = (int) (12.0D / (Math.random() * 0.8D + 0.2D));
    }

    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float pScaleFactor) {
        float f = ((float) this.age + pScaleFactor) / (float) this.lifetime;
        float growthFactor = Math.min((float) this.age / 5.0F, 1.0F);
        return this.quadSize * growthFactor * (1.0F - f * f);
    }

    public void tick()
    {
        super.tick();
        if (!this.removed) {
            float f = (float) this.age / (float) this.lifetime;
            this.random.nextFloat();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            CasingParticle casingParticle = new CasingParticle(pLevel, pX, pY, pZ);
            casingParticle.setSpriteFromAge(this.sprite);
            return casingParticle;
        }
    }
}
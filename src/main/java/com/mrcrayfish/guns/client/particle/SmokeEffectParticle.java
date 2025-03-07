package com.mrcrayfish.guns.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class SmokeEffectParticle extends TextureSheetParticle
{
    public SmokeEffectParticle(ClientLevel world, double x, double y, double z)
    {
        super(world, x, y, z, 0, 0, 0);
        this.setColor(1.0F, 1.0F, 1.0F);
        this.setSize(0.0F,0.0F);
        this.setLifetime(0);
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            SmokeEffectParticle particle = new SmokeEffectParticle(worldIn, x, y, z);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}

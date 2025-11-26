package com.mrcrayfish.guns.client.network;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.client.BulletTrail;
import com.mrcrayfish.guns.client.CustomGunManager;
import com.mrcrayfish.guns.client.audio.DistancedSound;
import com.mrcrayfish.guns.client.handler.BulletTrailRenderingHandler;
import com.mrcrayfish.guns.client.handler.GunRenderingHandler;
import com.mrcrayfish.guns.common.NetworkGunManager;
import com.mrcrayfish.guns.init.ModParticleTypes;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.network.message.*;
import com.mrcrayfish.guns.particles.BulletHoleData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Author: MrCrayfish
 */
public class ClientPlayHandler
{
    private static final Map<Long, List<Vec3>> projectileHitsPerTick = new HashMap<>();
    private static long lastProcessedTick = -1;

    public static void handleMessageGunshotOrReload(S2CMessageGunshotOrReload message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        if(mc.player == null || mc.level == null)
            return;

        if(message.showMuzzleFlash())
        {
            GunRenderingHandler.get().showMuzzleFlashForPlayer(message.getShooterId());
        }

        if(message.getShooterId() == mc.player.getId())
        {
            soundManager.play(new SimpleSoundInstance(message.getId(), SoundSource.PLAYERS, message.getVolume(), message.getPitch(), mc.level.getRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
        else
        {
            soundManager.play(DistancedSound.gunshotOrReload(message.getId(), SoundSource.PLAYERS, message.getX(), message.getY(), message.getZ(), message.getVolume(), message.getPitch(), message.isReload(), mc.level.getRandom()));
        }
    }

    public static void handleMessageBlood(S2CMessageBlood message)
    {
        Level world = Minecraft.getInstance().level;
        if(!Config.CLIENT.enableHitParticle.get())
        {
            return;
        }
        if(world != null)
        {
            if(Config.CLIENT.enableHeadshotParticle.get())
            {
                if (message.isHeadshot())
                {
                    for(int i = 0; i < 3; i++)
                    {
                        world.addParticle(ModParticleTypes.HEADSHOT.get(), true, message.getX(), message.getY(), message.getZ(), 0, 0.25, 0);
                    }
                }
            }

            if (Config.CLIENT.enableBlood.get() && message.getAllowBlood())
            {
                for(int i = 0; i < 10; i++)
                {
                    world.addParticle(ModParticleTypes.BLOOD.get(), true, message.getX(), message.getY(), message.getZ(), 0.5, 0, 0.5);
                }
            }
            else
            {
                for(int i = 0; i < 3; i++)
                {
                    world.addParticle(ParticleTypes.SMOKE, true, message.getX(), message.getY(), message.getZ(), (Math.random()-0.5)*0.15, (Math.random()*0.02)-0.04, (Math.random()-0.5)*0.15);
                }
            }
        }
    }

    public static void handleMessageBulletTrail(S2CMessageBulletTrail message)
    {
        Level world = Minecraft.getInstance().level;
        if(world != null)
        {
            int[] entityIds = message.getEntityIds();
            Vec3[] positions = message.getPositions();
            Vec3[] motions = message.getMotions();
            ItemStack item = message.getItem();
            int trailColor = message.getTrailColor();
            double trailLengthMultiplier = message.getTrailLengthMultiplier();
            int life = message.getLife();
            double gravity = message.getGravity();
            int shooterId = message.getShooterId();
            boolean enchanted = message.isEnchanted();
            ParticleOptions data = message.getParticleData();
            for(int i = 0; i < message.getCount(); i++)
            {
                BulletTrailRenderingHandler.get().add(new BulletTrail(entityIds[i], positions[i], motions[i], item, trailColor, trailLengthMultiplier, life, gravity, shooterId, enchanted, data));
            }
        }
    }

    public static void handleExplosionGrenade(S2CMessageGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        float size = Config.COMMON.handGrenadeExplosionRadius.get().floatValue() * 2.0F;
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        soundManager.play(DistancedSound.grenadeExplosion(ModSounds.GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        //Spawn explosion particle
        Particle explosion = spawnParticle(particleManager, ModParticleTypes.EXPLOSION.get(), x, y, z, level.random, 0.0);
        explosion.scale(size);

        //Spawn lingering smoke particles
        for(int i = 0; i < 60; i++)
        {
            spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, level.random, 0.2);
        }

        //Spawn fast moving flame/spark particles
        for(int i = 0; i < 60; i++)
        {
            Particle flame = spawnParticle(particleManager, ParticleTypes.FLAME, x, y, z, level.random, 2.0);
            flame.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.6)) * 0.5));
            spawnParticle(particleManager, ParticleTypes.CRIT, x, y, z, level.random, 3.0);
        }
    }

    public static void handleExplosionImpactGrenade(S2CMessageImpactGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        float size = Config.COMMON.impactGrenadeExplosionRadius.get().floatValue() * 2.0F;
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        soundManager.play(DistancedSound.impactGrenadeExplosion(ModSounds.GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        //Spawn explosion particle
        Particle explosion = spawnParticle(particleManager, ModParticleTypes.EXPLOSION.get(), x, y, z, level.random, 0.0);
        explosion.scale(size);

        //Spawn lingering smoke particles
        for(int i = 0; i < 60; i++)
        {
            spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, level.random, 0.2);
        }

        //Spawn fast moving flame/spark particles
        for(int i = 0; i < 60; i++)
        {
            Particle flame = spawnParticle(particleManager, ParticleTypes.FLAME, x, y, z, level.random, 2.0);
            flame.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.6)) * 0.5));
            spawnParticle(particleManager, ParticleTypes.CRIT, x, y, z, level.random, 3.0);
        }
    }

    public static void handleExplosionRocket(S2CMessageRocket message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        float size = Config.COMMON.rocketExplosionRadius.get().floatValue() * 2.0F;
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        soundManager.play(DistancedSound.rocketExplosion(ModSounds.ROCKET_EXPLOSION.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        //Spawn explosion particle
        Particle explosion = spawnParticle(particleManager, ModParticleTypes.EXPLOSION.get(), x, y, z, level.random, 0.0);
        explosion.scale(size);

        //Spawn lingering smoke particles
        for(int i = 0; i < 90; i++)
        {
            spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, level.random, 0.25);
        }

        //Spawn fast moving flame particles
        for(int i = 0; i < 120; i++)
        {
            Particle flame = spawnParticle(particleManager, ParticleTypes.FLAME, x, y, z, level.random, 1.5);
            flame.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.6)) * 0.5));
            flame.scale(3f);
        }
    }

    public static void handleExplosionPipeGrenade(S2CMessagePipeGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        float size = Config.COMMON.pipeGrenadeExplosionRadius.get().floatValue() * 2.0F;
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        soundManager.play(DistancedSound.pipeGrenadeExplosion(ModSounds.PIPE_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        //Spawn explosion particle
        Particle explosion = spawnParticle(particleManager, ModParticleTypes.EXPLOSION.get(), x, y, z, level.random, 0.0);
        explosion.scale(size);

        //Spawn lingering smoke particles
        for(int i = 0; i < 90; i++)
        {
            spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, level.random, 0.25);
        }

        //Spawn fast moving flame particles
        for(int i = 0; i < 120; i++)
        {
            Particle flame = spawnParticle(particleManager, ParticleTypes.FLAME, x, y, z, level.random, 1.5);
            flame.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.6)) * 0.5));
            flame.scale(3f);
        }
    }

    public static void handleExplosionIncendiaryGrenade(S2CMessageIncendiaryGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        float size = Config.COMMON.incendiaryGrenadeExplosionRadius.get().floatValue() * 2.0F;
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        soundManager.play(DistancedSound.incendiaryGrenadeExplosion(ModSounds.INCENDIARY_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        //Spawn explosion particle
        Particle explosion = spawnParticle(particleManager, ModParticleTypes.EXPLOSION.get(), x, y, z, level.random, 0.0);
        explosion.scale(size);

        //Spawn fast moving flame particles
        for(int i = 0; i < 90; i++)
        {
            Particle flame = spawnParticle(particleManager, ParticleTypes.FLAME, x, y, z, level.random, 1.5);
            flame.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.6)) * 0.5));
            flame.scale(3f);
        }
    }

    public static void handleIncendiaryGrenadeUnderwater(S2CMessageIncendiaryGrenadeUnderwater message)
    {
        Minecraft mc = Minecraft.getInstance();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        mc.getSoundManager().play(DistancedSound.incendiaryGrenadeExplosion(ModSounds.EXTINGUISH.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        //Spawn lingering smoke and bubble particles
        for(int i = 0; i < 60; i++)
        {
            spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, level.random, 0.2);
        }
    }

    public static void handleExplosionMolotov(S2CMessageMolotov message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        float size = Config.COMMON.molotovExplosionRadius.get().floatValue() * 2.0F;
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        soundManager.play(DistancedSound.molotovExplosion(ModSounds.MOLOTOV_EXPLOSION.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        //Spawn explosion particle
        Particle explosion = spawnParticle(particleManager, ModParticleTypes.EXPLOSION.get(), x, y, z, level.random, 0.0);
        explosion.scale(size);

        //Spawn fast moving flame particles
        for(int i = 0; i < 90; i++)
        {
            Particle flame = spawnParticle(particleManager, ParticleTypes.FLAME, x, y, z, level.random, 1.5);
            flame.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.6)) * 0.5));
            flame.scale(3f);
        }
    }

    public static void handleMolotovUnderwater (S2CMessageMolotovUnderwater message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        soundManager.play(DistancedSound.molotovExplosion(ModSounds.BOTTLE_BREAK.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        //Spawn lingering smoke and bubble particles
        for(int i = 0; i < 60; i++)
        {
            spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, level.random, 0.2);
        }
    }

    public static void handleExplosionStunGrenade(S2CMessageStunGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        ParticleEngine particleManager = mc.particleEngine;
        Level level = Objects.requireNonNull(mc.level);
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();

        //Play explosion sound
        soundManager.play(DistancedSound.stunGrenadeExplosion(ModSounds.STUN_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float)x,(float)y, (float)z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        /* Spawn lingering smoke particles */
        for(int i = 0; i < 30; i++)
        {
            spawnParticle(particleManager, ParticleTypes.CLOUD, x, y, z, level.random, 0.2);
        }

        /* Spawn fast moving smoke/spark particles */
        for(int i = 0; i < 30; i++)
        {
            Particle smoke = spawnParticle(particleManager, ParticleTypes.SMOKE, x, y, z, level.random, 4.0);
            smoke.setLifetime((int) ((8 / (Math.random() * 0.1 + 0.4)) * 0.5));
            spawnParticle(particleManager, ParticleTypes.CRIT, x, y, z, level.random, 4.0);
        }
    }

    public static void handleExplosionSmokeGrenade(S2CMessageSmokeGrenade message)
    {
        Minecraft mc = Minecraft.getInstance();
        SoundManager soundManager = mc.getSoundManager();
        Level level = Objects.requireNonNull(mc.level);
        double x = message.getX();
        double y = message.getY();
        double z = message.getZ();
        double diameter = Config.COMMON.smokeGrenadeCloudDiameter.get();
        double vel = 0.004;
        int amount = (int) (diameter * 15);

        //Play explosion sound
        soundManager.play(DistancedSound.smokeGrenadeExplosion(ModSounds.SMOKE_GRENADE_EXPLOSION.getId(), SoundSource.BLOCKS, (float) x,(float) y, (float) z, 1, 0.9F + level.random.nextFloat() * 0.1F, level.getRandom()));

        /* Spawn smoke cloud */
        for(int i = 0; i < amount; i++)
        {
            level.addAlwaysVisibleParticle(ModParticleTypes.SMOKE_CLOUD.get(),
                    true,
                    x+((Math.random()-0.5) * diameter),
                    y+(Math.random() * (diameter * 0.5)),
                    z+((Math.random()-0.5) * diameter),
                    (Math.random()-0.5) * vel,
                    Math.random() * (vel * 0.5),
                    (Math.random()-0.5) * vel);
        }
    }

    public static Particle spawnParticle(ParticleEngine manager, ParticleOptions data, double x, double y, double z, RandomSource rand, double velocityMultiplier)
    {
        return manager.createParticle(data, x, y, z, (rand.nextDouble() - 0.5) * velocityMultiplier, (rand.nextDouble() - 0.5) * velocityMultiplier, (rand.nextDouble() - 0.5) * velocityMultiplier);
    }

    public static void handleProjectileHitBlock(S2CMessageProjectileHitBlock message)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if(world != null && mc.player != null)
        {
            long currentTick = world.getGameTime();

            if (currentTick != lastProcessedTick) {
                projectileHitsPerTick.remove(lastProcessedTick);
                lastProcessedTick = currentTick;
            }

            List<Vec3> currentTickHits = projectileHitsPerTick.computeIfAbsent(currentTick, k -> new ArrayList<>());

            BlockState state = world.getBlockState(message.getPos());
            double holeX = message.getX() + 0.005 * message.getFace().getStepX();
            double holeY = message.getY() + 0.005 * message.getFace().getStepY();
            double holeZ = message.getZ() + 0.005 * message.getFace().getStepZ();
            double distance = Math.sqrt(mc.player.distanceToSqr(message.getX(), message.getY(), message.getZ()));
            world.addParticle(new BulletHoleData(message.getFace(), message.getPos()), true, holeX, holeY, holeZ, 0, 0, 0);
            if(distance < Config.CLIENT.impactParticleDistance.get())
            {
                for(int i = 0; i < 4; i++)
                {
                    Vec3i normal = message.getFace().getNormal();
                    Vec3 motion = new Vec3(normal.getX(), normal.getY(), normal.getZ());
                    motion.add(getRandomDir(world.random), getRandomDir(world.random), getRandomDir(world.random));
                    world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), true, message.getX(), message.getY(), message.getZ(), motion.x, motion.y, motion.z);
                }
            }

            boolean hasNearbyHit = false;
            Vec3 currentHit = new Vec3(message.getX(), message.getY(), message.getZ());
            for (Vec3 hit : currentTickHits) {
                if (hit.distanceToSqr(currentHit) <= 4.0) {
                    hasNearbyHit = true;
                    break;
                }
            }

            if (!hasNearbyHit && distance <= Config.CLIENT.impactSoundDistance.get()) {
                world.playLocalSound(message.getX(), message.getY(), message.getZ(), state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 2.0F, false);
            }
            currentTickHits.add(currentHit);
        }
    }

    private static double getRandomDir(RandomSource random)
    {
        return -0.25 + random.nextDouble() * 0.5;
    }

    public static void handleProjectileHitEntity(S2CMessageProjectileHitEntity message)
    {
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if(world == null)
            return;

        GunRenderingHandler.get().playHitMarker(message.isCritical() || message.isHeadshot());

        SoundEvent event = getHitSound(message.isCritical(), message.isHeadshot(), message.isPlayer());
        if(event == null)
            return;

        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F, 1.0F + world.random.nextFloat() * 0.2F));
    }

    @Nullable
    private static SoundEvent getHitSound(boolean critical, boolean headshot, boolean player)
    {
        if(critical)
        {
            if(Config.CLIENT.playSoundWhenCritical.get())
            {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.criticalSound.get()));
                return event != null ? event : SoundEvents.PLAYER_ATTACK_CRIT;
            }
        }
        else if(headshot)
        {
            if(Config.CLIENT.playSoundWhenHeadshot.get())
            {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.headshotSound.get()));
                return event != null ? event : SoundEvents.PLAYER_ATTACK_KNOCKBACK;
            }
        }
        else
        {
        	if(Config.CLIENT.playHitSound.get() && (!Config.CLIENT.hitSoundOnlyAgainstPlayers.get() || player))
            {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(Config.CLIENT.hitSound.get()));
                return event != null ? event : SoundEvents.TRIDENT_HIT;
            }
        }
        return null;
    }


    public static void handleRemoveProjectile(S2CMessageRemoveProjectile message)
    {
        BulletTrailRenderingHandler.get().remove(message.getEntityId());
    }

    public static void handleUpdateGuns(S2CMessageUpdateGuns message)
    {
        NetworkGunManager.updateRegisteredGuns(message);
        CustomGunManager.updateCustomGuns(message);
    }
}

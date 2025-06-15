package com.mrcrayfish.guns.event;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModParticleTypes;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Iterator;
import java.util.Map;

import static com.mrcrayfish.guns.event.GunFireLightEvent.temporaryLights;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEventBus
{
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null)
            {
                for (ServerLevel world : server.getAllLevels())
                {
                    GunFireLightEvent.tickLights(world);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event)
    {
        MinecraftServer server = event.getServer();
        for (ServerLevel world : server.getAllLevels())
        {
            GunFireLightEvent.cleanup(world);
        }
    }
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event)
    {
        if (!event.getLevel().isClientSide())
        {
            ChunkPos chunkPos = event.getChunk().getPos();
            Iterator<Map.Entry<Long, GunFireLightEvent.LightData>> iterator = temporaryLights.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<Long, GunFireLightEvent.LightData> entry = iterator.next();
                BlockPos pos = BlockPos.of(entry.getKey());
                if (chunkPos.equals(new ChunkPos(pos)))
                {
                    GunFireLightEvent.LightData data = entry.getValue();
                    Level level = (Level) event.getLevel();
                    if (data.dimension == level.dimension())
                    {
                        if (level.hasChunkAt(pos))
                        {
                            BlockState currentState = level.getBlockState(pos);
                            if (currentState.is(Blocks.LIGHT))
                            {
                                level.setBlock(pos, data.previousState, 3);
                            }
                        }
                        iterator.remove();
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void postShoot(GunFireEvent.Post event)
    {
        Player player = event.getEntity();
        Level level = event.getEntity().level;
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getOrCreateTag();
        
        if (heldItem.getItem() instanceof GunItem gunItem)
        {
            //Fire light
            Gun gun = gunItem.getModifiedGun(heldItem);
            Vec3 lookVec = player.getLookAngle();
            BlockPos lightPos = player.blockPosition().offset((int)(lookVec.x), 0.75, (int)(lookVec.z));
            GunFireLightEvent.addTemporaryLight(level, lightPos);

            //Casing eject
            if (gun.getGeneral().shouldSpawnCasings())
            {
                if (tag.getInt("AmmoCount") >= 1 || player.getAbilities().instabuild) {
                    ejectCasing(level, player);
                }
            }
        }
    }

    public static void ejectCasing(Level level, LivingEntity livingEntity)
    {
        Player playerEntity = (Player) livingEntity;
        ItemStack heldItem = playerEntity.getMainHandItem();
        Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        GripType gripType = gun.getGeneral().getGripType();

        Vec3 lookVec = playerEntity.getLookAngle();
        double yawRad = Math.toRadians(playerEntity.getYRot());
        Vec3 rightVec = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad));

        double rightOffset;
        double verticalOffset;

        if(gripType.equals(GripType.TWO_HANDED)) {
            rightOffset = 0.25;
            verticalOffset = 0.4;
        }
        else if(gripType.equals(GripType.ONE_HANDED) || gripType.equals(GripType.PISTOL_CUSTOM)) {
            rightOffset = 0.275;
            verticalOffset = 0.3;
        }
        else if(gripType.equals(GripType.MINI_GUN)) {
            rightOffset = 0.5;
            verticalOffset = 0.8;
        }
        else {
            rightOffset = 0.0;
            verticalOffset = 0.0;
        }

        double offsetX = rightVec.x * rightOffset + lookVec.x * 0.5;
        double offsetY = (playerEntity.getEyeHeight() - verticalOffset) + lookVec.y * 0.5;
        double offsetZ = rightVec.z * rightOffset + lookVec.z * 0.5;

        Vec3 particlePos = playerEntity.getPosition(1).add(offsetX, offsetY, offsetZ);

        ResourceLocation light = ModItems.LIGHT_BULLET.getId();
        ResourceLocation medium = ModItems.MEDIUM_BULLET.getId();
        ResourceLocation heavy = ModItems.HEAVY_BULLET.getId();
        ResourceLocation shell = ModItems.BUCKSHOT_SHELL.getId();
        ResourceLocation projectile = gun.getProjectile().getItem();

        SimpleParticleType casingType = ModParticleTypes.CASING.get();

        if (projectile != null) {
            if (projectile.equals(light) || projectile.equals(medium) || projectile.equals(heavy))
                casingType = ModParticleTypes.BRASS_CASING.get();
            else if (projectile.equals(shell))
                casingType = ModParticleTypes.SHELL_CASING.get();
        }

        if (level instanceof ServerLevel serverLevel)
        {
            double upSpeed = 0.05;
            double rightSpeed = 0.1;

            double velocityX = rightVec.x * rightSpeed;
            double velocityY = upSpeed;
            double velocityZ = rightVec.z * rightSpeed;

            serverLevel.sendParticles(casingType,
                    particlePos.x, particlePos.y, particlePos.z,
                    1,
                    velocityX, velocityY, velocityZ,
                    1.0);
        }
    }
}

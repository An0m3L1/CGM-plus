package com.mrcrayfish.guns.event;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModParticleTypes;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

import static com.mrcrayfish.guns.common.GripType.*;
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
    public static void preShoot(GunFireEvent.Pre event)
    {

        Player player = event.getEntity();
        Level level = event.getEntity().level;
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getTag();

        if(heldItem.getItem() instanceof GunItem gunItem)
        {
            Gun gun = gunItem.getModifiedGun(heldItem);

            if (heldItem.isDamageableItem() && tag != null) {
                if (heldItem.getDamageValue() == (heldItem.getMaxDamage() - 1)) {
                    event.setCanceled(true);
                }

                // Jamming
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() * 0.8) && Config.COMMON.gameplay.enableJamming.get()) {
                    if (Math.random() >= 0.975) {
                        // Play sound if jammed
                        event.getEntity().playSound(SoundEvents.ITEM_BREAK, 3.0F, 1.0F);
                        int coolDown = gun.getGeneral().getRate() * 10;
                        if (coolDown > 100) {
                            coolDown = 100;
                        }
                        event.getEntity().getCooldowns().addCooldown(event.getStack().getItem(), (coolDown));
                        event.setCanceled(true);
                    }
                } else if (tag.getInt("AmmoCount") >= 1) {
                    broken(heldItem, level, player);
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
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);

            // Decreasing durability
            if (heldItem.isDamageableItem() && Config.COMMON.gameplay.enableDurability.get()) {
                if (tag.getInt("AmmoCount") >= 1 ){
                    damageGun(heldItem, level, player);
                }
                // Play sound when low durability shooting
                if (heldItem.getDamageValue() >= (heldItem.getMaxDamage() * 0.8)) {
                    level.playSound(player, player.blockPosition(), ModSounds.LOW_DURABILITY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }

            // Fire light (WIP, buggy)
            /*
            Vec3 lookVec = player.getLookAngle();
            BlockPos low = player.blockPosition().offset((int)(lookVec.x), 0.0, (int)(lookVec.z));
            BlockPos high = player.blockPosition().offset((int)(lookVec.x), 1.0, (int)(lookVec.z));

            FireLightEvent.addTemporaryLight(level, low);
            FireLightEvent.addTemporaryLight(level, high);
            */

            // Casing eject (WIP, not clean)
            /*
            if (gun.getGeneral().shouldSpawnCasings() && (tag.getInt("AmmoCount") >= 1 || player.getAbilities().instabuild)) {
                ejectCasing(level, player);
            }
            */
        }
    }

    public static void broken(ItemStack stack, Level level, Player player) {
        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamageValue();
        if (currentDamage >= (maxDamage - 2)) {
            level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 3.0F, 1.0F);
        }
    }

    public static void damageGun(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().instabuild) {
            if (stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int currentDamage = stack.getDamageValue();
                if (currentDamage >= (maxDamage - 1)) {
                    if (currentDamage >= (maxDamage - 2)) {
                        level.playSound(player, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 3.0F, 1.0F);
                    }
                }
                else {
                    stack.hurtAndBreak(1, player, null);
                }
            }
        }
    }

    public static void ejectCasing(Level level, LivingEntity livingEntity)
    {
        Player player = (Player) livingEntity;
        ItemStack heldItem = player.getMainHandItem();
        Gun modifiedGun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
        GripType gripType = modifiedGun.getGeneral().getGripType();

        Vec3 lookVec = player.getLookAngle();
        Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();
        Vec3 forwardVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        //Offsets in blocks
        double horizontalOffset = 0.0;
        double verticalOffset = -0.0;
        double forwardOffset = 0.0;

        if(gripType.equals(TWO_HANDED) || gripType.equals(TWO_HANDED_SHORT)) {
            horizontalOffset = 0.225;
            verticalOffset = -0.3;
            forwardOffset = 0.4;
        }
        else if(gripType.equals(ONE_HANDED) || gripType.equals(PISTOL_CUSTOM)) {
            horizontalOffset = 0.275;
            verticalOffset = -0.2;
            forwardOffset = 0.5;
        }
        else if(gripType.equals(MINI_GUN)) {
            horizontalOffset = 0.5;
            verticalOffset = -0.8;
            forwardOffset = 0.4;
        }

        double offsetX = rightVec.x * horizontalOffset + forwardVec.x * forwardOffset;
        double offsetY = (player.getEyeHeight() + verticalOffset) + lookVec.y * 0.5;
        double offsetZ = rightVec.z * horizontalOffset + forwardVec.z * forwardOffset;

        Vec3 particlePos = player.getPosition(1).add(offsetX, offsetY, offsetZ); //Add the offsets to the player's position

        ResourceLocation light = ModItems.LIGHT_BULLET.getId();
        ResourceLocation medium = ModItems.MEDIUM_BULLET.getId();
        ResourceLocation heavy = ModItems.HEAVY_BULLET.getId();
        ResourceLocation shell = ModItems.BUCKSHOT_SHELL.getId();
        ResourceLocation projectile = modifiedGun.getProjectile().getItem();

        SimpleParticleType casingType = ModParticleTypes.CASING.get();

        if (projectile != null) {
            if (projectile.equals(light) || projectile.equals(medium) || projectile.equals(heavy))
                casingType = ModParticleTypes.BRASS_CASING.get();
            else if (projectile.equals(shell))
                casingType = ModParticleTypes.SHELL_CASING.get();
        }

        if (level instanceof ServerLevel serverLevel)
        {
            serverLevel.sendParticles(casingType,
                    particlePos.x, particlePos.y, particlePos.z, 1,
                    0, 0, 0, 0);
        }
    }
}
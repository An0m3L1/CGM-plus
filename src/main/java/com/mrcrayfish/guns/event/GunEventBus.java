package com.mrcrayfish.guns.event;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.entity.LightSourceEntity;
import com.mrcrayfish.guns.init.ModItems;
import com.mrcrayfish.guns.init.ModParticleTypes;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.mrcrayfish.guns.common.GripType.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunEventBus
{
    @SubscribeEvent
    public static void postShoot(GunFireEvent.Post event)
    {
        Player player = event.getEntity();
        Level level = event.getEntity().level;
        ItemStack heldItem = player.getMainHandItem();
        CompoundTag tag = heldItem.getOrCreateTag();
        int maxDamage = heldItem.getMaxDamage();
        int currentDamage = heldItem.getDamageValue();

        if (heldItem.getItem() instanceof GunItem gunItem)
        {
            // Decreasing durability
            if (heldItem.isDamageableItem() && Config.COMMON.gameplay.enableDurability.get())
            {
                if (tag.getInt("AmmoCount") >= 1 )
                    damageGun(heldItem, level, player);

                // Play sound when shooting a low durability gun
                if (currentDamage >= (maxDamage * 0.8))
                    level.playSound(player, player.blockPosition(), ModSounds.LOW_DURABILITY.get(), SoundSource.PLAYERS, 0.75F, 1.5F);
            }

            // Fire light
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);
            if (!level.isClientSide() && // Checks if world is on server-side
                    Config.COMMON.gameplay.enableDynamicLights.get() && // Checks config
                    GunMod.dynamicLightsLoaded && // Checks loaded dynamic lights mod
                    modifiedGun.getGeneral().shouldEmitLight()) // Checks if this gun should emit light while shooting
            {
                Vec3 lookVec = player.getLookAngle();
                double forwardOffset = 2.0;
                double x = player.getX() + lookVec.x * forwardOffset;
                double y = (player.getEyeY() - 0.35) + lookVec.y * forwardOffset;
                double z = player.getZ() + lookVec.z * forwardOffset;
                int lightLevel = Config.COMMON.gameplay.dynamicLightValue.get();
                LightSourceEntity light = new LightSourceEntity(level, x, y, z, lightLevel);
                level.addFreshEntity(light);
            }

            // Casing eject (WIP, not clean)
            /*
            if (gun.getGeneral().shouldSpawnCasings() && (tag.getInt("AmmoCount") >= 1 || player.getAbilities().instabuild)) {
                ejectCasing(level, player);
            }
            */
        }
    }

    public static void damageGun(ItemStack stack, Level level, Player player)
    {
        if (!player.getAbilities().instabuild && stack.isDamageableItem() && (stack.getDamageValue() <= (stack.getMaxDamage() - 1)))
            stack.hurtAndBreak(1, player, null);
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
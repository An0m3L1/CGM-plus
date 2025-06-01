package com.mrcrayfish.guns.event;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PausedGameEvent {
    private static boolean wasGamePaused = false;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        boolean isPaused = mc.isPaused();
        if (!wasGamePaused && isPaused && mc.player != null)
        {
            if (mc.level != null)
            {
                GunFireLight.cleanup(mc.level);
            }
            ItemStack stack = mc.player.getMainHandItem();
            if (!(stack.getItem() instanceof GunItem))
            {
                wasGamePaused = isPaused;
                return;
            }

            CompoundTag nbtCompound = stack.getOrCreateTag();
            if (!nbtCompound.getBoolean("guns:IsReloading"))
            {
                wasGamePaused = true;
                return;
            }
        }
        wasGamePaused = isPaused;
    }
    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event)
    {
        GunFireLight.cleanup((Level) event.getLevel());
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
    {
        if (event.getPlayer() != null)
        {
            GunFireLight.cleanup(event.getPlayer().level);
        }
    }

    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event)
    {
        if (event.getLevel().isClientSide())
        {
            GunFireLight.cleanup((Level) event.getLevel());
        }
    }
}
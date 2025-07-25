package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class ServerAimTracker
{
    private static final Map<Player, ServerAimTracker> AIM_TRACKER_MAP = new WeakHashMap<>();

    private int aimStartTick;
    
    // This tracker handles the accelerating fire-rate of guns with the Ramp Up enchantment/parameter.

    private ServerAimTracker(Player player)
    {
        this.aimStartTick = 0;
    }

    private int getDeltaTicks(Player player)
    {
        int deltaTicks = player.tickCount - this.aimStartTick;
        return deltaTicks;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START && !event.player.level.isClientSide)
        {
            Player player = event.player;
            if(!AIM_TRACKER_MAP.containsKey(player))
            {
                AIM_TRACKER_MAP.put(player, new ServerAimTracker(player));
            }
            ServerAimTracker tracker = AIM_TRACKER_MAP.get(player);
            
           	if(!ModSyncedDataKeys.AIMING.getValue(player))
           	{
           		tracker.aimStartTick = player.tickCount;
           	}
        }
    }

    public static int getAimingTicks(Player player)
    {
        if(!AIM_TRACKER_MAP.containsKey(player))
        	return 0;
    	ServerAimTracker tracker = AIM_TRACKER_MAP.get(player);
    	return tracker.getDeltaTicks(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerEvent.PlayerLoggedOutEvent event)
    {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null)
        {
            server.execute(() -> AIM_TRACKER_MAP.remove(event.getEntity()));
        }
    }
}

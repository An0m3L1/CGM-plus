package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.init.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: An0m3L1
 */
public class PlayerHandler
{
    private static PlayerHandler instance;

    public static PlayerHandler get()
    {
        if(instance == null)
        {
            instance = new PlayerHandler();
        }
        return instance;
    }

    private PlayerHandler(){}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return;

        ItemStack heldItem = mc.player.getMainHandItem();

        // Sprinting restrictions
        if(heldItem.is(ModTags.Items.HEAVY) ||
                player.isVisuallyCrawling() ||
                player.isCrouching() ||
                (player.getUseItem().getItem() == Items.SHIELD) ||
                ModSyncedDataKeys.RELOADING.getValue(player))
        {
            mc.options.keySprint.setDown(false);
            player.setSprinting(false);
        }

        // Crouching restrictions
        if(player.isVisuallyCrawling())
            mc.options.keyShift.setDown(false);
            player.setShiftKeyDown(false);
    }
}

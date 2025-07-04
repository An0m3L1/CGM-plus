package com.mrcrayfish.guns.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.guns.common.network.ServerPlayHandler;
import com.mrcrayfish.guns.event.GunReloadEvent;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

/**
 * Author: MrCrayfish
 */
public class C2SMessageReload extends PlayMessage<C2SMessageReload>
{
    private boolean reload;

    public C2SMessageReload() {}

    public C2SMessageReload(boolean reload)
    {
        this.reload = reload;
    }

    @Override
    public void encode(C2SMessageReload message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.reload);
    }

    @Override
    public C2SMessageReload decode(FriendlyByteBuf buffer)
    {
        return new C2SMessageReload(buffer.readBoolean());
    }

    @Override
    public void handle(C2SMessageReload message, MessageContext context)
    {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if(player != null && !player.isSpectator())
            {
                ModSyncedDataKeys.RELOADING.setValue(player, message.reload); // This has to be set in order to verify the packet is sent if the event is cancelled
                if(!message.reload)
                {
                    if (ModSyncedDataKeys.SWITCHTIME.getValue(player)<=0)
                        ModSyncedDataKeys.SWITCHTIME.setValue(player, 1);
                    return;
                }
                else
                {
                	ServerPlayHandler.playReloadStartSound(player);
                }

                ItemStack gun = player.getMainHandItem();
                if(MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, gun)))
                {
                	ModSyncedDataKeys.RELOADING.setValue(player, false);
                    return;
                }
                MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, gun));
            }
        });
        context.setHandled(true);
    }
}

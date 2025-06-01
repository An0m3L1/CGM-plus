package com.mrcrayfish.guns.event;

import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
            Gun gun = gunItem.getModifiedGun(heldItem);
            Vec3 lookVec = player.getLookAngle();
            //BlockPos lightPos = player.blockPosition().offset(0, 0.5, 0);
            BlockPos lightPos = player.blockPosition().offset((int)(lookVec.x), 0.75, (int)(lookVec.z));
            GunFireLightEvent.addTemporaryLight(level, lightPos);
         }
    }
}

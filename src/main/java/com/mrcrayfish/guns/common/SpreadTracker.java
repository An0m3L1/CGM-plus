package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class SpreadTracker
{
    private static final Map<Player, SpreadTracker> TRACKER_MAP = new WeakHashMap<>();
    private final Map<GunItem, Pair<MutableLong, MutableInt>> SPREAD_TRACKER_MAP = new HashMap<>();

    private boolean isPenaltyActive(Player player) {
        return Config.COMMON.doSpreadPenalties.get() && (player.isSprinting() || !player.isOnGround());
    }

    private int getMinCount(int maxCount, Player player) {
        if (isPenaltyActive(player)) {
            return Math.round(maxCount * 0.5F);
        }
        return 0;
    }

    public void update(Player player, GunItem item)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.computeIfAbsent(item, gun -> Pair.of(new MutableLong(-1), new MutableInt()));
        MutableLong lastFire = entry.getLeft();
        MutableInt spreadCount = entry.getRight();

        if (lastFire.getValue() != -1)
        {
            long deltaTime = System.currentTimeMillis() - lastFire.getValue();
            int currentCount = spreadCount.getValue();
            int maxCount = Config.COMMON.maxCount.get();
            int minCount = getMinCount(maxCount, player);

            if (deltaTime < Config.COMMON.spreadThreshold.get())
            {
                if (currentCount < maxCount)
                {
                    int addCount = 1;

                    if (Config.COMMON.doSpreadPenalties.get())
                    {
                        if (!ModSyncedDataKeys.AIMING.getValue(player)) addCount++;
                        if (player.isSprinting() || !player.isOnGround()) addCount++;
                    }

                    int newCount = Math.min(currentCount + addCount, maxCount);
                    spreadCount.setValue(Math.max(newCount, minCount));
                }
                else if (currentCount < minCount)
                {
                    spreadCount.setValue(minCount);
                }
            }
            else
            {
                spreadCount.setValue(minCount);
            }
        }
        lastFire.setValue(System.currentTimeMillis());
    }

    public float getNextSpread(Player player, GunItem item, float aim)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.get(item);
        if (entry != null)
        {
            int currentCount = entry.getRight().getValue();
            int maxCount = Config.COMMON.maxCount.get();
            int minCount = getMinCount(maxCount, player);

            currentCount = Math.max(currentCount, minCount);
            float nextSpread = (Config.COMMON.doSpreadPenalties.get() ? 1F + aim : 1F);
            float nextCount = Math.min(currentCount + nextSpread, maxCount);
            nextCount = Math.max(nextCount, minCount);

            return nextCount / (float) maxCount;
        }
        return 0F;
    }

    public float getSpread(Player player, GunItem item)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.get(item);
        if (entry != null)
        {
            int currentCount = entry.getRight().getValue();
            int maxCount = Config.COMMON.maxCount.get();
            int minCount = getMinCount(maxCount, player);

            currentCount = Math.max(currentCount, minCount);
            return (float) currentCount / (float) maxCount;
        }
        return 0F;
    }

    public static SpreadTracker get(Player player)
    {
        return TRACKER_MAP.computeIfAbsent(player, player1 -> new SpreadTracker());
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
    {
        MinecraftServer server = event.getEntity().getServer();
        if (server != null)
        {
            server.execute(() -> TRACKER_MAP.remove(event.getEntity()));
        }
    }
}
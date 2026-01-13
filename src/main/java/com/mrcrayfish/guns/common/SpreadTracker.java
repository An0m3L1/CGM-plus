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

    public void update(Player player, GunItem item)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.computeIfAbsent(item, gun -> Pair.of(new MutableLong(-1), new MutableInt()));
        MutableLong lastFire = entry.getLeft();
        MutableInt spreadCount = entry.getRight();

        if(lastFire.getValue() != -1) {
            long deltaTime = System.currentTimeMillis() - lastFire.getValue();
            int currentCount = spreadCount.getValue();
            int maxCount = Config.COMMON.maxCount.get();

            /* Spread is at least 50% while sprinting/midair */
            int penaltyMinCount = Math.round(maxCount * 0.5F);
            boolean penaltyActive = Config.COMMON.doSpreadStartInaccuracy.get() && (player.isSprinting() || !player.isOnGround());
            int minCount = penaltyActive ? penaltyMinCount : 0;

            if (deltaTime < Config.COMMON.spreadThreshold.get()) {
                if (currentCount < maxCount) {
                    /* Increase spread after each shot */
                    int addCount = 1;

                    /* Increase spread more if:
                     * 1. Player is not ADS
                     * 2. Player is sprinting/airborne */
                    if (Config.COMMON.doSpreadPenalties.get()) {
                        if(!ModSyncedDataKeys.AIMING.getValue(player)) addCount++;
                        if(player.isSprinting() || !player.isOnGround()) addCount++;
                    }

                    int newCount = Math.min(currentCount + addCount, maxCount);
                    /* Check if spread is no lower than minCount */
                    spreadCount.setValue(Math.max(newCount, minCount));
                } else if (currentCount < minCount) {
                    /* If current spread is lower than minimum, raise it */
                    spreadCount.setValue(minCount);
                }
            } else {
                /* Reset to minimum count */
                spreadCount.setValue(minCount);
            }
        }
        lastFire.setValue(System.currentTimeMillis());
    }

    public float getNextSpread(Player player, GunItem item, float aim)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.get(item);
        if(entry != null) {
            int currentCount = entry.getRight().getValue();
            int maxCount = Config.COMMON.maxCount.get();

            int penaltyMinCount = Math.round(maxCount * 0.5F);
            boolean penaltyActive = Config.COMMON.doSpreadStartInaccuracy.get() && (player.isSprinting() || !player.isOnGround());
            int minCount = penaltyActive ? penaltyMinCount : 0;

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
        if(entry != null) {
            int currentCount = entry.getRight().getValue();
            int maxCount = Config.COMMON.maxCount.get();

            int penaltyMinCount = Math.round(maxCount * 0.5F);
            boolean penaltyActive = Config.COMMON.doSpreadStartInaccuracy.get() && (player.isSprinting() || !player.isOnGround());
            int minCount = penaltyActive ? penaltyMinCount : 0;

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
        if(server != null) {
            server.execute(() -> TRACKER_MAP.remove(event.getEntity()));
        }
    }
}
package com.an0m3l1.guns.common;

import com.an0m3l1.guns.GunConfig;
import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.init.ModSyncedDataKeys;
import com.an0m3l1.guns.item.GunItem;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.S2CMessageSound;
import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
@Mod.EventBusSubscriber(modid = GunMod.MOD_ID)
public class SwitchGunTracker
{
	private static final Map<Player, SwitchGunTracker> SWITCHGUN_TRACKER_MAP = new WeakHashMap<>();
	
	private final int slot;
	private ItemStack stack;
	private final Item item;
	private Gun gun;
	private boolean playSelectSound;
	
	private SwitchGunTracker(Player player)
	{
		this.slot = player.getInventory().selected;
		this.stack = player.getInventory().getSelected();
		this.item = stack.getItem();
		this.gun = ((GunItem) item).getModifiedGun(stack);
		this.playSelectSound = true;
	}
	
	/**
	 * Tests if the current item the player is holding is the same as the one being reloaded
	 *
	 * @param player
	 * 		the player to check
	 *
	 * @return True if it's the same weapon and slot
	 */
	private boolean isSameWeapon(Player player)
	{
		return !this.stack.isEmpty() && player.getInventory().selected == this.slot && player.getInventory().getSelected().getItem() == this.item;
	}
	
	private int getInventoryAmmo(Player player, Gun gun)
	{
		return Gun.getReserveAmmoCount(player, gun.getProjectile().getItem());
	}
	
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		if(event.phase == TickEvent.Phase.START && !event.player.level.isClientSide)
		{
			Player player = event.player;
			boolean doGunSwitch = false;
			boolean doGunSelect = false;
			if(!SWITCHGUN_TRACKER_MAP.containsKey(player))
			{
				if(!(player.getInventory().getSelected().getItem() instanceof GunItem))
				{
					ModSyncedDataKeys.SWITCHTIME.setValue(player, 1);
					return;
				}
				SWITCHGUN_TRACKER_MAP.put(player, new SwitchGunTracker(player));
				doGunSelect = true;
			}
			SwitchGunTracker tracker = SWITCHGUN_TRACKER_MAP.get(player);
			
			//Reload and weapon switch cooldown logic
			if(player.getInventory().getSelected().getItem() instanceof GunItem)
			{
				if(tracker.isSameWeapon(player))
				{
					int switch_cooldown = ModSyncedDataKeys.SWITCHTIME.getValue(player);
					if(switch_cooldown > 0)
					{
						switch_cooldown--;
					}
					if(switch_cooldown != ModSyncedDataKeys.SWITCHTIME.getValue(player))
					{
						ModSyncedDataKeys.SWITCHTIME.setValue(player, switch_cooldown);
					}
				}
				else
				{
					doGunSwitch = true;
				}
			}
			else
			{
				doGunSwitch = true;
			}
			
			if(doGunSelect && tracker.playSelectSound)
			{
				tracker.playSelectSound = false;
				final ItemStack finalStack = player.getInventory().getSelected();
				ModSyncedDataKeys.SWITCHTIME.setValue(player, tracker.gun.getGeneral().getDrawTime());
				ResourceLocation selectSound = tracker.gun.getSounds().getWeaponSelect();
				final Player finalPlayer = player;
				if(tracker.gun.getSounds().getWeaponSelectDelay() >= 0)
				{
					DelayedTask.runAfter(tracker.gun.getSounds().getWeaponSelectDelay(), () ->
					{
						if(finalStack == finalPlayer.getInventory().getSelected())
						{
							playSelectSound(finalPlayer, selectSound);
						}
					});
				}
			}
			if(doGunSwitch)
			{
				if(!(player.getInventory().getSelected().getItem() instanceof GunItem) || (Item.getId(player.getInventory().getSelected().getItem()) != Item.getId(tracker.stack.getItem()) || player.getInventory().selected != tracker.slot))
				{
					ModSyncedDataKeys.SWITCHTIME.setValue(player, 5);
					SWITCHGUN_TRACKER_MAP.remove(player);
				}
				else
				{
					tracker.stack = player.getInventory().getSelected();
					tracker.gun = ((GunItem) tracker.stack.getItem()).getModifiedGun(tracker.stack);
				}
			}
		}
	}
	
	private static void playSelectSound(Player player, ResourceLocation sound)
	{
		if(sound != null && player.isAlive())
		{
			double soundX = player.getX();
			double soundY = player.getY() + 1.0;
			double soundZ = player.getZ();
			double radius = GunConfig.SERVER.switchSoundDistance.get();
			S2CMessageSound messageSound = new S2CMessageSound(sound, SoundSource.PLAYERS, (float) soundX, (float) soundY, (float) soundZ, 1.0F, 1.0F, player.getId(), false, true);
			PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, soundX, soundY, soundZ, radius), messageSound);
		}
	}
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerEvent.PlayerLoggedOutEvent event)
	{
		MinecraftServer server = event.getEntity().getServer();
		if(server != null)
		{
			server.execute(() -> SWITCHGUN_TRACKER_MAP.remove(event.getEntity()));
		}
	}
}

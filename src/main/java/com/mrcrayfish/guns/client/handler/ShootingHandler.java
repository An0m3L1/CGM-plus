package com.mrcrayfish.guns.client.handler;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.KeyBinds;
import com.mrcrayfish.guns.common.GripType;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.compat.PlayerReviveHelper;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageFireSwitch;
import com.mrcrayfish.guns.network.message.C2SMessageReload;
import com.mrcrayfish.guns.network.message.C2SMessageShoot;
import com.mrcrayfish.guns.network.message.C2SMessageShooting;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class ShootingHandler
{
    private static ShootingHandler instance;

    public static ShootingHandler get()
    {
        if(instance == null)
        {
            instance = new ShootingHandler();
        }
        return instance;
    }

    private boolean shooting;
    private int lastShotTick=-1;
    private int weaponSwitchTick=-1;
    private boolean doEmptyClick;

    private int slot = -1;
    private Item lastItem;

    private ShootingHandler() {}

    private boolean isNotInGame()
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.getOverlay() != null)
            return true;
        if(mc.screen != null)
            return true;
        if(!mc.mouseHandler.isMouseGrabbed())
            return true;
        return !mc.isWindowActive();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMouseClick(InputEvent.InteractionKeyMappingTriggered event)
    {
        if(event.isCanceled())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player == null)
            return;

        if(PlayerReviveHelper.isBleeding(player))
            return;

        if(Config.CLIENT.flipControls.get() ? event.isUseItem() : event.isAttack())
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem gunItem)
            {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
        }
        else 
        {
        	if(Config.CLIENT.flipControls.get() ? event.isAttack() : event.isUseItem())
	        {
	            ItemStack heldItem = player.getMainHandItem();
	            if(heldItem.getItem() instanceof GunItem gunItem)
	            {
	                if(event.getHand() == InteractionHand.OFF_HAND)
	                {
	                    // Allow shields to be used if weapon is one-handed
	                    if(player.getOffhandItem().getItem() == Items.SHIELD)
	                    {
	                        Gun modifiedGun = gunItem.getModifiedGun(heldItem);
	                        if(modifiedGun.getGeneral().getGripType() == GripType.ONE_HANDED_PISTOL || modifiedGun.getGeneral().getGripType() == GripType.TWO_HANDED_PISTOL)
	                        {
	                            return;
	                        }
	                    }
	                    event.setCanceled(true);
	                    event.setSwingHand(false);
	                    return;
	                }
	                if(Config.CLIENT.flipControls.get() || AimingHandler.get().isZooming() && AimingHandler.get().isLookingAtInteractableBlock())
	                {
	                    event.setCanceled(true);
	                    event.setSwingHand(false);
	                }
	            }
	        }
        }
    }

	@SubscribeEvent
    public void onHandleShooting(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        if(this.isNotInGame())
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player != null)
        {
            ItemStack heldItem = player.getMainHandItem();
            if(heldItem.getItem() instanceof GunItem && !isEmpty(player, heldItem) && !PlayerReviveHelper.isBleeding(player))
            {
                //Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
                boolean shooting = (KeyBinds.getShootMapping().isDown() && !ModSyncedDataKeys.ONBURSTCOOLDOWN.getValue(player)) || (ModSyncedDataKeys.BURSTCOUNT.getValue(player)>0 && Gun.hasBurstFire(heldItem));
                if(GunMod.controllableLoaded)
                {
                    shooting |= ControllerHandler.isShooting();
                }
                if(shooting)
                {
                    if(!this.shooting)
                    {
                        this.shooting = true;
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(true));
                    }
                }
                else if(this.shooting)
                {
                    this.shooting = false;
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
                }
            }
            else if(this.shooting)
            {
                this.shooting = false;
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageShooting(false));
            }
        }
        else
        {
            this.shooting = false;
        }
    }

    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if(player != null)
        {
            ItemStack heldItem = player.getMainHandItem();
            // Weapon switch detection
            if (!isSameWeapon(player))
            {
                lastItem = heldItem.getItem();
                if (!isSameSlot(player))
                    ModSyncedDataKeys.SWITCHTIME.setValue(player, 1);
            	ModSyncedDataKeys.BURSTCOUNT.setValue(player, 0);
                ModSyncedDataKeys.AIMING.setValue(player, false);
                weaponSwitchTick = player.tickCount;
                if(heldItem.getItem() instanceof GunItem)
                {
                    GunRenderingHandler.get().updateReserveAmmo(player);
                }
                ReloadHandler.get().weaponSwitched();
            }

            if (ModSyncedDataKeys.RELOADING.getValue(player) == true)
                weaponSwitchTick = -1;

            // Update item and slot variables
            lastItem = player.getInventory().getSelected().getItem();
            slot = player.getInventory().selected;

            if(isNotInGame())
                return;

            if(PlayerReviveHelper.isBleeding(player))
                return;
            
            if(heldItem.getItem() instanceof GunItem)
            {
            	//Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
            	if(KeyBinds.getShootMapping().isDown() || (ModSyncedDataKeys.BURSTCOUNT.getValue(player)>0 && Gun.hasBurstFire(heldItem)))
                {
                    this.fire(player, heldItem);
                }
            	else
                {
                    doEmptyClick = true;
                }
            }

            // Handling fire mode switch logic here for convenience.
            if(KeyBinds.KEY_FIRE_MODE.isDown())
            {
            	if(heldItem.getItem() instanceof GunItem gunItem)
                {
                	Gun modifiedGun = gunItem.getModifiedGun(heldItem);
                	if (modifiedGun.getFireModes().usesFireModes() && (!ModSyncedDataKeys.SHOOTING.getValue(player) && !ModSyncedDataKeys.RELOADING.getValue(player) && ModSyncedDataKeys.BURSTCOUNT.getValue(player)<=0))
                	{
                    	CompoundTag tag = heldItem.getOrCreateTag();
                        //Gun.FireModes fireModes = modifiedGun.getFireModes();
                        boolean changedFireMode = false;
                        int newFireMode = 0;
                        
                        if (Gun.canDoAutoFire(heldItem) &&
                        (Gun.getFireMode(heldItem)==0 ||
                        !Gun.canDoSemiFire(heldItem) && Gun.getFireMode(heldItem)==2))
                        {
                        	changedFireMode = true;
                        	newFireMode = 1;
                		}
                        else
                        if (Gun.canDoBurstFire(heldItem) &&
                        (Gun.getFireMode(heldItem)==1 ||
                       	!Gun.canDoAutoFire(heldItem) && Gun.getFireMode(heldItem)==0))
                        {
                        	changedFireMode = true;
                        	newFireMode = 2;
                		}
                        else
                        if (Gun.canDoSemiFire(heldItem) &&
                        (Gun.getFireMode(heldItem)==2 ||
                       	!Gun.canDoBurstFire(heldItem) && Gun.getFireMode(heldItem)==1))
                        {
                        	changedFireMode = true;
                        }
                        
                        if (changedFireMode)
                        {
                        	PacketHandler.getPlayChannel().sendToServer(new C2SMessageFireSwitch(newFireMode));
                        	//tag.putInt("FireMode", newFireMode);
                        	Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(Objects.requireNonNull(gunItem.getModifiedGun(heldItem).getSounds().getFireSwitch()), SoundSource.PLAYERS, 0.8F, 1.0F, Objects.requireNonNull(Minecraft.getInstance().level).getRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true));
                        }
                	}
                }
                KeyBinds.KEY_FIRE_MODE.setDown(false);
            }
        }
    }

    private boolean canFire(Player player, ItemStack heldItem)
    {
    	if(!(heldItem.getItem() instanceof GunItem))
            return false;
        
        if(player.isSpectator())
            return false;
        
        if(ModSyncedDataKeys.RELOADING.getValue(player)) //*NEW* Disallow firing while reloading, and cancel reload.
        {
            if (!Gun.usesMagReloads(heldItem) && ReloadHandler.get().getReloadProgress(Minecraft.getInstance().getPartialTick()) >= 1F)
        	{
        		ReloadHandler.get().setReloading(false, true);
        		PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false));
        	}
        	return false;
        }

        if(ReloadHandler.get().getReloadProgress(Minecraft.getInstance().getPartialTick()) > 0F)
        {
            return false;
        }

        if(ModSyncedDataKeys.SWITCHTIME.getValue(player) > 0) //*NEW* Disallow firing during the weapon switch time.
        {
        	return false;
        }
        
        if(ModSyncedDataKeys.ONBURSTCOOLDOWN.getValue(player)) //*NEW* Disallow firing during the burst cooldown period.
        {
        	//GunItem gunItem = (GunItem) heldItem.getItem();
        	if (Gun.hasBurstFire(heldItem) && ModSyncedDataKeys.BURSTCOUNT.getValue(player)<=0)
            {
                return false;
            }
        }

        return player.getUseItem().getItem() != Items.SHIELD;
    }

    private boolean isEmpty(Player player, ItemStack heldItem)
    {
    	if(!(heldItem.getItem() instanceof GunItem))
            return false;
        
        if(player.isSpectator())
            return false;

        return (!Gun.hasAmmo(heldItem) || Gun.cantShoot(heldItem)) && !player.isCreative();
    }
    
    private boolean canUseTrigger(Player player, ItemStack heldItem)
    {
    	if(!(heldItem.getItem() instanceof GunItem))
            return false;
        
        if(player.isSpectator())
            return false;
        
        if(ModSyncedDataKeys.RELOADING.getValue(player))
        {
        	return false;
        }
        
        if(ModSyncedDataKeys.SWITCHTIME.getValue(player) > 0)
        {
        	return false;
        }
        
        if(ModSyncedDataKeys.ONBURSTCOOLDOWN.getValue(player))
        {
        	if (Gun.hasBurstFire(heldItem) && ModSyncedDataKeys.BURSTCOUNT.getValue(player)<=0)
            {
                return false;
            }
        }

        return player.getUseItem().getItem() != Items.SHIELD;
    }

    public void fire(Player player, ItemStack heldItem)
    {
        if(!(heldItem.getItem() instanceof GunItem))
            return;

        if(isEmpty(player, heldItem) || heldItem.getDamageValue() >= (heldItem.getMaxDamage() - 1))
        {

            ItemCooldowns tracker = player.getCooldowns();
            if(!tracker.isOnCooldown(heldItem.getItem()))
            {
            	if (doEmptyClick && heldItem.getItem() instanceof GunItem gunItem && canUseTrigger(player, heldItem))
	        	{
		            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(Objects.requireNonNull(gunItem.getModifiedGun(heldItem).getSounds().getEmptyClick()), SoundSource.PLAYERS, 0.8F, 1.0F, Objects.requireNonNull(Minecraft.getInstance().level).getRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true));
		        	doEmptyClick = false;
                    boolean doAutoFire = Gun.isAuto(heldItem) || (Gun.hasBurstFire(heldItem) && Gun.hasAutoBurst(heldItem));
                    if(!doAutoFire)
                    {
                        KeyBinds.getShootMapping().setDown(false);
                    }
	        	}
        	}
        	if (ModSyncedDataKeys.BURSTCOUNT.getValue(player)>0)
            {
                ModSyncedDataKeys.BURSTCOUNT.setValue(player, 0);
            }
        	return;
        }

        if(!canFire(player, heldItem))
            return;
        
        ItemCooldowns tracker = player.getCooldowns();
        if(!tracker.isOnCooldown(heldItem.getItem()))
        {
            GunItem gunItem = (GunItem) heldItem.getItem();
            Gun modifiedGun = gunItem.getModifiedGun(heldItem);

            if(MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)))
                return;

            int rate = GunCompositeStatHelper.getCompositeRate(heldItem,modifiedGun,player);
            tracker.addCooldown(heldItem.getItem(), rate);
            ModSyncedDataKeys.RAMPUPSHOT.setValue(player, ModSyncedDataKeys.RAMPUPSHOT.getValue(player)+1);
            
            int gunBurstCount = Gun.getBurstCount(heldItem);
            if (Gun.hasBurstFire(heldItem))
            {
                // Burst has not begun yet:
            	if (ModSyncedDataKeys.BURSTCOUNT.getValue(player)<=0)
            	ModSyncedDataKeys.BURSTCOUNT.setValue(player, gunBurstCount-1);
            	else
            	// When there are shots remaining in burst:
            	if (ModSyncedDataKeys.BURSTCOUNT.getValue(player)>0)
                {
                    ModSyncedDataKeys.BURSTCOUNT.setValue(player, ModSyncedDataKeys.BURSTCOUNT.getValue(player)-1);
                }
            }
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageShoot(player));
            boolean doAutoFire = Gun.isAuto(heldItem) || (Gun.hasBurstFire(heldItem) && Gun.hasAutoBurst(heldItem));
            if(!doAutoFire)
            {
                KeyBinds.getShootMapping().setDown(false);
            }
            int lastShotTick = player.tickCount;
            weaponSwitchTick = -1;
            MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));
        }
    }
    
    private boolean isSameWeapon(Player player)
    {
        if (slot==-1)
        	return true;
        boolean sameItem = (player.getInventory().getSelected().getItem() == lastItem);

        return (isSameSlot(player) && sameItem);
        //return (player.getInventory().selected == slot);
    }

    private boolean isSameSlot(Player player)
    {
        if (slot==-1)
            return true;

        return (player.getInventory().selected == slot);
    }

    public int getWeaponSwitchTick()
    {
        return weaponSwitchTick;
    }

    public void clearWeaponSwitchTick()
    {
        this.weaponSwitchTick = -1;
    }
}

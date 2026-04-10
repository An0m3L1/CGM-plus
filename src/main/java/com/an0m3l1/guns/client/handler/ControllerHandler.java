package com.an0m3l1.guns.client.handler;

import com.an0m3l1.guns.Config;
import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.client.GunButtonBindings;
import com.an0m3l1.guns.client.screen.WorkbenchScreen;
import com.an0m3l1.guns.common.Gun;
import com.an0m3l1.guns.init.ModSyncedDataKeys;
import com.an0m3l1.guns.item.GunItem;
import com.an0m3l1.guns.item.attachment.impl.create.Scope;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.C2SMessageAttachments;
import com.an0m3l1.guns.network.message.C2SMessageUnload;
import com.an0m3l1.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.event.ControllerEvent;
import com.mrcrayfish.controllable.event.GatherActionsEvent;
import com.mrcrayfish.controllable.event.GatherNavigationPointsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class ControllerHandler
{
	private int reloadCounter = -1;
	
	@SubscribeEvent
	public void onButtonInput(ControllerEvent.ButtonInput event)
	{
		Player player = Minecraft.getInstance().player;
		Level world = Minecraft.getInstance().level;
		if(player != null && world != null && Minecraft.getInstance().screen == null)
		{
			ItemStack heldItem = player.getMainHandItem();
			int button = event.getButton();
			if(button == GunButtonBindings.SHOOT.getButton())
			{
				if(heldItem.getItem() instanceof GunItem)
				{
					event.setCanceled(true);
					if(event.getState())
					{
						ShootingHandler.get().fire(player, heldItem);
					}
				}
			}
			else if(button == GunButtonBindings.AIM.getButton())
			{
				if(heldItem.getItem() instanceof GunItem)
				{
					event.setCanceled(true);
				}
			}
			else if(button == GunButtonBindings.STEADY_AIM.getButton())
			{
				if(heldItem.getItem() instanceof GunItem)
				{
					event.setCanceled(true);
				}
			}
			else if(button == GunButtonBindings.RELOAD.getButton())
			{
				if(heldItem.getItem() instanceof GunItem)
				{
					event.setCanceled(true);
					if(event.getState())
					{
						this.reloadCounter = 0;
					}
				}
			}
			else if(button == GunButtonBindings.OPEN_ATTACHMENTS.getButton())
			{
				if(heldItem.getItem() instanceof GunItem && Minecraft.getInstance().screen == null)
				{
					event.setCanceled(true);
					if(event.getState())
					{
						PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onControllerTurn(ControllerEvent.Turn event)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
		{
			ItemStack heldItem = player.getMainHandItem();
			if(heldItem.getItem() instanceof GunItem && AimingHandler.get().isAiming())
			{
				double adsSensitivity = Config.CLIENT.aimDownSightSensitivity.get();
				event.setYawSpeed(10.0F * (float) adsSensitivity);
				event.setPitchSpeed(7.5F * (float) adsSensitivity);
				
				Scope scope = Gun.getScope(heldItem);
				if(scope != null && scope.isStable() && Controllable.isButtonPressed(GunButtonBindings.STEADY_AIM.getButton()))
				{
					event.setYawSpeed(event.getYawSpeed() / 2.0F);
					event.setPitchSpeed(event.getPitchSpeed() / 2.0F);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void updateAvailableActions(GatherActionsEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen != null)
		{
			return;
		}
		
		Player player = Minecraft.getInstance().player;
		if(player != null)
		{
			ItemStack heldItem = player.getMainHandItem();
			if(heldItem.getItem() instanceof GunItem gunItem)
			{
				event.getActions().put(GunButtonBindings.AIM, new Action(Component.translatable(GunMod.MOD_ID + ".action.aim"), Action.Side.RIGHT));
				event.getActions().put(GunButtonBindings.SHOOT, new Action(Component.translatable(GunMod.MOD_ID + ".action.shoot"), Action.Side.RIGHT));
				
				Gun modifiedGun = gunItem.getModifiedGun(heldItem);
				CompoundTag tag = heldItem.getTag();
				if(tag != null && tag.getInt("AmmoCount") < GunCompositeStatHelper.getAmmoCapacity(heldItem, modifiedGun))
				{
					event.getActions().put(GunButtonBindings.RELOAD, new Action(Component.translatable(GunMod.MOD_ID + ".action.reload"), Action.Side.LEFT));
				}
				
				Scope scope = Gun.getScope(heldItem);
				if(scope != null && scope.isStable() && AimingHandler.get().isAiming())
				{
					event.getActions().put(GunButtonBindings.STEADY_AIM, new Action(Component.translatable(GunMod.MOD_ID + ".action.steady_aim"), Action.Side.RIGHT));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRender(TickEvent.RenderTickEvent event)
	{
		Controller controller = Controllable.getController();
		if(controller == null)
		{
			return;
		}
		
		if(event.phase == TickEvent.Phase.END)
		{
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if(player == null)
		{
			return;
		}
		
		if(Controllable.isButtonPressed(GunButtonBindings.SHOOT.getButton()) && Minecraft.getInstance().screen == null)
		{
			ItemStack heldItem = player.getMainHandItem();
			if(heldItem.getItem() instanceof GunItem)
			{
				//Gun gun = ((GunItem) heldItem.getItem()).getModifiedGun(heldItem);
				if(Gun.isAuto(heldItem))
				{
					ShootingHandler.get().fire(player, heldItem);
				}
			}
		}
		
		if(mc.screen == null && this.reloadCounter != -1)
		{
			if(Controllable.isButtonPressed(GunButtonBindings.RELOAD.getButton()))
			{
				this.reloadCounter++;
			}
		}
		
		if(this.reloadCounter > 40)
		{
			ReloadHandler.get().setReloading(false, false);
			PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
			this.reloadCounter = -1;
		}
		else if(this.reloadCounter > 0 && !Controllable.isButtonPressed(GunButtonBindings.RELOAD.getButton()))
		{
			ReloadHandler.get().setReloading(!ModSyncedDataKeys.RELOADING.getValue(player), true);
			this.reloadCounter = -1;
		}
	}
	
	public static boolean isAiming()
	{
		Controller controller = Controllable.getController();
		return controller != null && Controllable.isButtonPressed(GunButtonBindings.AIM.getButton());
	}
	
	public static boolean isShooting()
	{
		Controller controller = Controllable.getController();
		return controller != null && Controllable.isButtonPressed(GunButtonBindings.SHOOT.getButton());
	}
	
	@SubscribeEvent
	public void onGatherNavigationPoints(GatherNavigationPointsEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		if(mc.screen instanceof WorkbenchScreen workbench)
		{
			int startX = workbench.getGuiLeft();
			int startY = workbench.getGuiTop();
			
			for(int i = 0; i < workbench.getTabs().size(); i++)
			{
				int tabX = startX + 28 * i + (28 / 2);
				int tabY = startY - (28 / 2);
				event.addPoint(new BasicNavigationPoint(tabX, tabY));
			}
			
			for(int i = 0; i < 6; i++)
			{
				int itemX = startX + 172 + (80 / 2);
				int itemY = startY + i * 19 + 63 + (19 / 2);
				event.addPoint(new BasicNavigationPoint(itemX, itemY));
			}
		}
	}
}

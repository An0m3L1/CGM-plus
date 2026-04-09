package com.mrcrayfish.guns.client;

import com.mrcrayfish.controllable.client.BindingRegistry;
import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.guns.GunMod;

/**
 * Author: MrCrayfish
 */
public class GunButtonBindings
{
	public static final ButtonBinding SHOOT = new ButtonBinding(Buttons.RIGHT_TRIGGER, GunMod.MOD_ID + ".button.shoot", "button.categories." + GunMod.MOD_ID, GunConflictContext.IN_GAME_HOLDING_WEAPON);
	public static final ButtonBinding AIM = new ButtonBinding(Buttons.LEFT_TRIGGER, GunMod.MOD_ID + ".button.aim", "button.categories." + GunMod.MOD_ID, GunConflictContext.IN_GAME_HOLDING_WEAPON);
	public static final ButtonBinding RELOAD = new ButtonBinding(Buttons.X, GunMod.MOD_ID + ".button.reload", "button.categories." + GunMod.MOD_ID, GunConflictContext.IN_GAME_HOLDING_WEAPON);
	public static final ButtonBinding OPEN_ATTACHMENTS = new ButtonBinding(Buttons.B, GunMod.MOD_ID + ".button.attachments", "button.categories." + GunMod.MOD_ID, GunConflictContext.IN_GAME_HOLDING_WEAPON);
	public static final ButtonBinding STEADY_AIM = new ButtonBinding(Buttons.RIGHT_THUMB_STICK, GunMod.MOD_ID + ".button.steadyAim", "button.categories." + GunMod.MOD_ID, GunConflictContext.IN_GAME_HOLDING_WEAPON);
	
	public static void register()
	{
		BindingRegistry.getInstance().register(SHOOT);
		BindingRegistry.getInstance().register(AIM);
		BindingRegistry.getInstance().register(RELOAD);
		BindingRegistry.getInstance().register(OPEN_ATTACHMENTS);
		BindingRegistry.getInstance().register(STEADY_AIM);
	}
}

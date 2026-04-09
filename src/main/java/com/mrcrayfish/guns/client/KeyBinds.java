package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.GunMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public class KeyBinds
{
	public static final KeyMapping KEY_RELOAD = new KeyMapping("key." + GunMod.MOD_ID + ".reload", GLFW.GLFW_KEY_R, "key.categories." + GunMod.MOD_ID);
	public static final KeyMapping KEY_UNLOAD = new KeyMapping("key." + GunMod.MOD_ID + ".unload", GLFW.GLFW_KEY_U, "key.categories." + GunMod.MOD_ID);
	public static final KeyMapping KEY_ATTACHMENTS = new KeyMapping("key." + GunMod.MOD_ID + ".attachments", GLFW.GLFW_KEY_G, "key.categories." + GunMod.MOD_ID);
	public static final KeyMapping KEY_FIRE_MODE = new KeyMapping("key." + GunMod.MOD_ID + ".fire_mode", GLFW.GLFW_KEY_V, "key.categories." + GunMod.MOD_ID);
	
	public static void registerKeyMappings(RegisterKeyMappingsEvent event)
	{
		event.register(KEY_RELOAD);
		event.register(KEY_UNLOAD);
		event.register(KEY_ATTACHMENTS);
		event.register(KEY_FIRE_MODE);
	}
	
	public static KeyMapping getAimMapping()
	{
		Minecraft mc = Minecraft.getInstance();
		return mc.options.keyUse;
	}
	
	public static KeyMapping getShootMapping()
	{
		Minecraft mc = Minecraft.getInstance();
		return mc.options.keyAttack;
	}
}

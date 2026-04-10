package com.an0m3l1.guns.client;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.client.handler.*;
import com.an0m3l1.guns.client.render.gun.ModelOverrides;
import com.an0m3l1.guns.client.render.gun.model.*;
import com.an0m3l1.guns.client.screen.AttachmentScreen;
import com.an0m3l1.guns.client.screen.WorkbenchScreen;
import com.an0m3l1.guns.client.util.PropertyHelper;
import com.an0m3l1.guns.common.Gun;
import com.an0m3l1.guns.init.ModBlocks;
import com.an0m3l1.guns.init.ModContainers;
import com.an0m3l1.guns.init.ModItems;
import com.an0m3l1.guns.item.GunItem;
import com.an0m3l1.guns.item.IColored;
import com.an0m3l1.guns.item.attachment.impl.IAttachment;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.C2SMessageAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.Tag;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = GunMod.MOD_ID, value = Dist.CLIENT)
public class ClientHandler
{
	private static Field mouseOptionsField;
	
	public static void setup()
	{
		MinecraftForge.EVENT_BUS.register(AimingHandler.get());
		MinecraftForge.EVENT_BUS.register(BulletTrailRenderingHandler.get());
		MinecraftForge.EVENT_BUS.register(CrosshairHandler.get());
		MinecraftForge.EVENT_BUS.register(GunRenderingHandler.get());
		MinecraftForge.EVENT_BUS.register(RecoilHandler.get());
		MinecraftForge.EVENT_BUS.register(ReloadHandler.get());
		MinecraftForge.EVENT_BUS.register(ShootingHandler.get());
		MinecraftForge.EVENT_BUS.register(SoundHandler.get());
		MinecraftForge.EVENT_BUS.register(PlayerHandler.get());
		MinecraftForge.EVENT_BUS.register(new PlayerModelHandler());
		
		/* Only register controller events if Controllable is loaded otherwise it will crash */
		if(GunMod.controllableLoaded)
		{
			MinecraftForge.EVENT_BUS.register(new ControllerHandler());
			GunButtonBindings.register();
		}
		
		setupRenderLayers();
		registerColors();
		registerModelOverrides();
		registerScreenFactories();
	}
	
	private static void setupRenderLayers()
	{
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.GUN_WORKBENCH.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ModBlocks.HEMP_CROP_BLOCK.get(), RenderType.cutout());
	}
	
	private static void registerColors()
	{
		ItemColor color = (stack, index) ->
		{
			if(!IColored.isDyeable(stack))
			{
				return -1;
			}
			if(index == 0 && stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("Color", Tag.TAG_INT))
			{
				return stack.getTag().getInt("Color");
			}
			if(index == 0 && stack.getItem() instanceof GunItem)
			{
				ItemStack renderingWeapon = GunRenderingHandler.get().getRenderingWeapon();
				if(renderingWeapon != null)
				{
					Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
					return gun.getGeneral().getDefaultColor();
				}
			}
			if(index == 0 && stack.getItem() instanceof IAttachment)
			{
				ItemStack renderingWeapon = GunRenderingHandler.get().getRenderingWeapon();
				if(renderingWeapon != null)
				{
					return Minecraft.getInstance().getItemColors().getColor(renderingWeapon, index);
				}
			}
			if(index == 2) // Reticle colour
			{
				return PropertyHelper.getReticleColor(stack);
			}
			return -1;
		};
		ForgeRegistries.ITEMS.forEach(item ->
		{
			if(item instanceof IColored)
			{
				Minecraft.getInstance().getItemColors().register(color, item);
			}
		});
	}
	
	private static void registerModelOverrides()
	{
		/* Weapons */
		ModelOverrides.register(ModItems.ASSAULT_RIFLE.get(), new AssaultRifleModel());
		ModelOverrides.register(ModItems.TACTICAL_RIFLE.get(), new TacticalRifleModel());
		ModelOverrides.register(ModItems.COMBAT_RIFLE.get(), new CombatRifleModel());
		ModelOverrides.register(ModItems.ROCKET_LAUNCHER.get(), new SimpleModel(SpecialModels.ROCKET_LAUNCHER::getModel));
		ModelOverrides.register(ModItems.GRENADE_LAUNCHER.get(), new GrenadeLauncherModel());
		ModelOverrides.register(ModItems.HEAVY_SNIPER_RIFLE.get(), new HeavySniperRifleModel());
		ModelOverrides.register(ModItems.AUTOMATIC_PISTOL.get(), new AutomaticPistolModel());
		ModelOverrides.register(ModItems.MINI_GUN.get(), new MiniGunModel());
		ModelOverrides.register(ModItems.TACTICAL_PISTOL.get(), new TacticalPistolModel());
		ModelOverrides.register(ModItems.PISTOL.get(), new PistolModel());
		ModelOverrides.register(ModItems.SNIPER_RIFLE.get(), new SniperRifleModel());
		ModelOverrides.register(ModItems.SEMI_AUTO_SHOTGUN.get(), new SemiAutoShotgunModel());
	}
	
	private static void registerScreenFactories()
	{
		MenuScreens.register(ModContainers.WORKBENCH.get(), WorkbenchScreen::new);
		MenuScreens.register(ModContainers.ATTACHMENTS.get(), AttachmentScreen::new);
	}
	
	@SubscribeEvent
	public static void onScreenInit(ScreenEvent.Init.Post event)
	{
		if(event.getScreen() instanceof MouseSettingsScreen screen)
		{
			if(mouseOptionsField == null)
			{
				mouseOptionsField = ObfuscationReflectionHelper.findField(MouseSettingsScreen.class, "f_96218_");
				mouseOptionsField.setAccessible(true);
			}
			try
			{
				OptionsList list = (OptionsList) mouseOptionsField.get(screen);
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public static void onKeyPressed(InputEvent.Key event)
	{
		Minecraft mc = Minecraft.getInstance();
		if(mc.player != null && mc.screen == null && event.getAction() == GLFW.GLFW_PRESS)
		{
			if(KeyBinds.KEY_ATTACHMENTS.isDown())
			{
				PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
			}
		}
	}
	
	public static void onRegisterReloadListener(RegisterClientReloadListenersEvent event)
	{
		event.registerReloadListener((ResourceManagerReloadListener) manager -> PropertyHelper.resetCache());
	}
}

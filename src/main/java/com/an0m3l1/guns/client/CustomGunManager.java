package com.an0m3l1.guns.client;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.common.CustomGun;
import com.an0m3l1.guns.common.CustomGunLoader;
import com.an0m3l1.guns.init.ModItems;
import com.an0m3l1.guns.network.message.S2CMessageUpdateGuns;
import com.mrcrayfish.framework.api.data.login.ILoginData;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = GunMod.MOD_ID, value = Dist.CLIENT)
public class CustomGunManager
{
	private static Map<ResourceLocation, CustomGun> customGunMap;
	
	public static void updateCustomGuns(S2CMessageUpdateGuns message)
	{
		updateCustomGuns(message.getCustomGuns());
	}
	
	private static void updateCustomGuns(Map<ResourceLocation, CustomGun> customGunMap)
	{
		CustomGunManager.customGunMap = customGunMap;
	}
	
	public static void fill(NonNullList<ItemStack> items)
	{
		if(customGunMap != null)
		{
			customGunMap.forEach((id, gun) ->
			{
				ItemStack stack = new ItemStack(ModItems.TACTICAL_PISTOL.get());
				stack.setHoverName(Component.translatable("item." + id.getNamespace() + "." + id.getPath() + ".name"));
				CompoundTag tag = stack.getOrCreateTag();
				tag.put("Model", gun.getModel().save(new CompoundTag()));
				tag.put("Gun", gun.getGun().serializeNBT());
				tag.putBoolean("Custom", true);
				tag.putInt("AmmoCount", gun.getGun().getGeneral().getMaxAmmo());
				tag.putInt("Energy", 0);
				items.add(stack);
			});
		}
	}
	
	@SubscribeEvent
	public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event)
	{
		customGunMap = null;
	}
	
	public static class LoginData implements ILoginData
	{
		@Override
		public void writeData(FriendlyByteBuf buffer)
		{
			Validate.notNull(CustomGunLoader.get());
			CustomGunLoader.get().writeCustomGuns(buffer);
		}
		
		@Override
		public Optional<String> readData(FriendlyByteBuf buffer)
		{
			Map<ResourceLocation, CustomGun> customGuns = CustomGunLoader.readCustomGuns(buffer);
			CustomGunManager.updateCustomGuns(customGuns);
			return Optional.empty();
		}
	}
}

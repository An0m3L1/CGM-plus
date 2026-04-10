package com.an0m3l1.guns.init;

import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.blockentity.WorkbenchBlockEntity;
import com.an0m3l1.guns.common.container.AttachmentContainer;
import com.an0m3l1.guns.common.container.WorkbenchContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class ModContainers
{
	public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, GunMod.MOD_ID);
	
	public static final RegistryObject<MenuType<WorkbenchContainer>> WORKBENCH = register("workbench", (IContainerFactory<WorkbenchContainer>) (windowId, playerInventory, data) ->
	{
		WorkbenchBlockEntity workstation = (WorkbenchBlockEntity) playerInventory.player.level.getBlockEntity(data.readBlockPos());
		return new WorkbenchContainer(windowId, playerInventory, Objects.requireNonNull(workstation));
	});
	
	public static final RegistryObject<MenuType<AttachmentContainer>> ATTACHMENTS = register("attachments", AttachmentContainer::new);
	
	private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String id, MenuType.MenuSupplier<T> factory)
	{
		return REGISTER.register(id, () -> new MenuType<>(factory));
	}
}

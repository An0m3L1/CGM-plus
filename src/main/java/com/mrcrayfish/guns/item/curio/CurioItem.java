package com.mrcrayfish.guns.item.curio;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CurioItem extends AccessoryItem implements ICurioItem
{
    public boolean isEquippedBy(@Nullable LivingEntity entity) {
        return entity != null && CuriosApi.getCuriosHelper().findEquippedCurio(this, entity).isPresent();
    }

    protected <T extends Event, S extends LivingEntity> void addListener(EventPriority priority, Class<T> eventClass, BiConsumer<T, S> listener, Function<T, S> wearerSupplier) {
        MinecraftForge.EVENT_BUS.addListener(priority, true, eventClass, (event) -> {
            S wearer = (S)((LivingEntity)wearerSupplier.apply(event));
            if (this.isEquippedBy(wearer)) {
                listener.accept(event, wearer);
            }

        });
    }

    protected <T extends Event, S extends LivingEntity> void addListener(Class<T> eventClass, BiConsumer<T, S> listener, Function<T, S> wearerSupplier) {
        this.addListener(EventPriority.NORMAL, eventClass, listener, wearerSupplier);
    }

    protected <T extends LivingEvent> void addListener(EventPriority priority, Class<T> eventClass, BiConsumer<T, LivingEntity> listener) {
        this.addListener(priority, eventClass, listener, LivingEvent::getEntity);
    }

    protected <T extends LivingEvent> void addListener(Class<T> eventClass, BiConsumer<T, LivingEntity> listener) {
        this.addListener(EventPriority.NORMAL, eventClass, listener);
    }

    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}

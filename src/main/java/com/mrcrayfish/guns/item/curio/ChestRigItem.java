package com.mrcrayfish.guns.item.curio;

import com.mrcrayfish.guns.init.ModTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.stream.Stream;

public class ChestRigItem extends CurioItem
{
    public static final String TAG_ITEMS = "Items";

    public ChestRigItem() {}

    /*
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        // Check if chest rig has a stack of one and we right-click it
        if (stack.getCount() == 1 && action == ClickAction.SECONDARY) {
            // Get item in item slot we're hovering
            ItemStack itemStackInSlot = slot.getItem();
            // Check if item slot is empty
            if (itemStackInSlot.isEmpty()) {
                // Play remove sound
                this.playRemoveOneSound(player);
                // Remove one item stack
                removeOne(stack).ifPresent(removedStack -> add(stack, slot.safeInsert(removedStack)));
                // Check if item in slot is an instance of ammo
            } else if (itemStackInSlot.is(ModTags.Items.AMMO)) {
                // Calculate max amount to insert
                int maxInsertCount = getMaxItemCount(stack) - getTotalItemCount(stack);
                // Calculate actual amount to insert
                int itemsToInsert = Math.min(itemStackInSlot.getCount(), maxInsertCount);
                // Add inserted items
                int insertedItems = add(stack, slot.safeTake(itemStackInSlot.getCount(), itemsToInsert, player));
                // Check if we actually inserted anything
                if (insertedItems > 0) {
                    // Play insert sound
                    this.playInsertSound(player);
                }
            }
            return true;
        } else {
            return false;
        }
    }
    */
    private static Optional<ItemStack> removeOne(ItemStack stack) {
        CompoundTag compoundTag = stack.getOrCreateTag();
        if (!compoundTag.contains(TAG_ITEMS)) {
            return Optional.empty();
        } else {
            ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
            if (listTag.isEmpty()) {
                return Optional.empty();
            } else {
                CompoundTag itemTag = listTag.getCompound(0);
                ItemStack itemStack = ItemStack.of(itemTag);
                listTag.remove(0);
                if (listTag.isEmpty()) {
                    stack.removeTagKey(TAG_ITEMS);
                }
                return Optional.of(itemStack);
            }
        }
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level.getRandom().nextFloat() * 0.4F);
    }

    protected void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level.getRandom().nextFloat() * 0.4F);
    }

    protected int getBaseMaxItemCount() {
        return 512;
    }

    protected TagKey<Item> getAmmoTag() {
        return ModTags.Items.AMMO;
    }

    public static Stream<ItemStack> getContents(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag == null) {
            return Stream.empty();
        }
        ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
        return listTag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
    }
}

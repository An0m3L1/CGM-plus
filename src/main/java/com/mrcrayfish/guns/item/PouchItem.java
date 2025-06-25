package com.mrcrayfish.guns.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.common.capability.CurioItemCapability;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class PouchItem extends BundleItem
{
    public static final String TAG_ITEMS = "Items";
    public int maxCount;
    public TagKey<Item> itemType;

    public PouchItem(Properties properties, int maxStacks, TagKey<Item> itemType)
    {
		super(properties.stacksTo(1));
        this.maxCount = maxStacks * 64;
        this.itemType = itemType;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (stack.getCount() == 1 && action == ClickAction.SECONDARY) {
            ItemStack itemStackInSlot = slot.getItem();
            if (itemStackInSlot.isEmpty()) {
                this.playRemoveOneSound(player);
                removeOne(stack).ifPresent(removedStack -> add(stack, slot.safeInsert(removedStack)));
            } else if (itemStackInSlot.is(itemType)) {
                int maxInsertCount = maxCount - getContentWeight(stack);
                int itemsToInsert = Math.min(itemStackInSlot.getCount(), maxInsertCount);
                int insertedItems = add(stack, slot.safeTake(itemStackInSlot.getCount(), itemsToInsert, player));
                if (insertedItems > 0) {
                    this.playInsertSound(player);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess) {
        if (stack.getCount() != 1) {
            return false;
        } else if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (otherStack.isEmpty()) {
                removeOne(stack).ifPresent(removedStack -> {
                    this.playRemoveOneSound(player);
                    slotAccess.set(removedStack);
                });
            } else if (otherStack.is(itemType)) {
                int maxInsertCount = maxCount - getContentWeight(stack);
                int itemsToInsert = Math.min(otherStack.getCount(), maxInsertCount);
                ItemStack newOtherStack = otherStack.copy();
                newOtherStack.setCount(itemsToInsert);
                int insertedItems = add(stack, newOtherStack);
                if (insertedItems > 0) {
                    this.playInsertSound(player);
                    otherStack.shrink(insertedItems);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        return Math.min(1 + 12 * getContentWeight(stack) / maxCount, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Objects.requireNonNull(ChatFormatting.YELLOW.getColor());
    }

    public int add(ItemStack pouchStack, ItemStack insertedStack) {
        if (!insertedStack.isEmpty() && insertedStack.is(itemType)) {
            CompoundTag compoundTag = pouchStack.getOrCreateTag();
            if (!compoundTag.contains(TAG_ITEMS)) {
                compoundTag.put(TAG_ITEMS, new ListTag());
            }

            int maxItemCount = maxCount;
            int itemsToInsert = Math.min(insertedStack.getCount(), maxItemCount - getContentWeight(pouchStack));

            if (itemsToInsert == 0) {
                return 0;
            }

            ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag itemTag = listTag.getCompound(i);
                ItemStack existingStack = ItemStack.of(itemTag);
                if (ItemStack.isSameItemSameTags(existingStack, insertedStack)) {
                    int remainingSpace = Math.min(existingStack.getMaxStackSize() - existingStack.getCount(), itemsToInsert);
                    existingStack.grow(remainingSpace);
                    itemsToInsert -= remainingSpace;
                    existingStack.save(itemTag);
                    listTag.set(i, itemTag);
                    if (itemsToInsert <= 0) {
                        //break;
                    }
                }
            }

            while (itemsToInsert > 0) {
                int countToInsert = Math.min(insertedStack.getMaxStackSize(), itemsToInsert);
                ItemStack newItemStack = insertedStack.copy();
                newItemStack.setCount(countToInsert);
                CompoundTag newItemTag = new CompoundTag();
                newItemStack.save(newItemTag);
                listTag.add(newItemTag);
                itemsToInsert -= countToInsert;
            }

            compoundTag.put(TAG_ITEMS, listTag);
            return insertedStack.getCount() - itemsToInsert;
        } else {
            return 0;
        }
    }

    public static int getContentWeight(ItemStack stack) {
        return getContents(stack).mapToInt((ItemStack) -> getWeight(ItemStack) * ItemStack.getCount()).sum();
    }

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

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("item.minecraft.bundle.fullness", getContentWeight(stack), maxCount).withStyle(ChatFormatting.GRAY));
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level.getRandom().nextFloat() * 0.4F);
    }

    protected void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level.getRandom().nextFloat() * 0.4F);
    }

    public static Stream<ItemStack> getContents(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        if (compoundTag == null) {
            return Stream.empty();
        }
        ListTag listTag = compoundTag.getList(TAG_ITEMS, 10);
        return listTag.stream().map(CompoundTag.class::cast).map(ItemStack::of);
    }

    private static int getWeight(ItemStack stack) {
        return 64 / stack.getMaxStackSize();
    }

    public static ICapabilityProvider createPouchProvider(final ItemStack stack) {
        return CurioItemCapability.createProvider(new ICurio() {
            public ItemStack getStack() {
                return stack;
            }

            public boolean canEquipFromUse(SlotContext context) {
                return false;
            }

            public boolean canSync(SlotContext context) {
                return true;
            }

            public boolean canUnequip(SlotContext context) {
                return true;
            }

            @Nonnull
            public ICurio.DropRule getDropRule(SlotContext context, DamageSource source, int lootingLevel, boolean recentlyHit)
            {
                return DropRule.DEFAULT;
            }
        });
    }
}

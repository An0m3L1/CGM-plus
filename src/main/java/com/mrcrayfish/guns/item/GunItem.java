package com.mrcrayfish.guns.item;

import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.GunItemStackRenderer;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.NetworkGunManager;
import com.mrcrayfish.guns.debug.Debug;
import com.mrcrayfish.guns.init.ModBlocks;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class GunItem extends Item implements IColored, IMeta
{
    private final WeakHashMap<CompoundTag, Gun> modifiedGunCache = new WeakHashMap<>();

    private Gun gun = new Gun();

    public GunItem(Item.Properties properties)
    {
        super(properties.tab(GunMod.GUNS));
    }

    public void setGun(NetworkGunManager.Supplier supplier)
    {
        this.gun = supplier.getGun();
    }

    public Gun getGun()
    {
        return this.gun;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt){
        return new GunEnergyStorage(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)
    {
        Gun modifiedGun = this.getModifiedGun(stack);
        String additionalDamageText = "";
        CompoundTag tagCompound = stack.getTag();

        /* Weapon stats */
        boolean isBroken = stack.getDamageValue() == (stack.getMaxDamage() - 1); // Guns never go below 1 durability
        float damage = GunModifierHelper.getModifiedProjectileDamage(stack, modifiedGun.getProjectile().getDamage());
        float headshotDamage = GunCompositeStatHelper.getHeadshotDamage(stack);
        int projectileAmount = modifiedGun.getGeneral().getProjectileAmount();
        Item ammoType = ForgeRegistries.ITEMS.getValue(modifiedGun.getProjectile().getItem());
        float armorPiercePercent = modifiedGun.getProjectile().getArmorBypass() * 100.0F;
        float protectionPiercePercent = modifiedGun.getProjectile().getProtectionBypass() * 100.0F;
        int maxPierceCount = modifiedGun.getProjectile().getMaxPierceCount();
        double reloadTimeSeconds;
        double adsTimeSeconds = 0.25 - (GunCompositeStatHelper.getCompositeAimDownSightSpeed(stack) * 0.25 - 0.25); // 0.25s by default
        double drawTimeSeconds = (double) modifiedGun.getGeneral().getDrawTime() / 20; // 0.5s by default
        float fireRateRPM = Math.round(20 / ((float)GunCompositeStatHelper.getCompositeBaseRate(stack, modifiedGun)) * 60);
        float recoilDegrees = (modifiedGun.getGeneral().getRecoilAngle() * (1.0F - GunModifierHelper.getRecoilModifier(stack)));
        float adsRecoilDegrees = recoilDegrees * (1.0F - (modifiedGun.getGeneral().getRecoilAdsReduction()));
        float spreadDegrees = GunCompositeStatHelper.getCompositeSpread(stack, modifiedGun);
        float minSpreadDegrees = GunCompositeStatHelper.getCompositeMinSpread(stack, modifiedGun);
        boolean isAlwaysSpread = (minSpreadDegrees<=0 && modifiedGun.getGeneral().getAlwaysSpread());
        minSpreadDegrees = (GunCompositeStatHelper.getCompositeMinSpread(stack, modifiedGun)<=0 ? (isAlwaysSpread ? spreadDegrees : 0) : GunCompositeStatHelper.getCompositeMinSpread(stack, modifiedGun));
        float adsSpreadDegrees = spreadDegrees * (1-(modifiedGun.getGeneral().getSpreadAdsReduction()));
        float adsMinSpreadDegrees = minSpreadDegrees * (1-(modifiedGun.getGeneral().getSpreadAdsReduction()));
        boolean useSniperSpread = modifiedGun.getGeneral().getUseSniperSpread();

        /* Broken check */
        if(isBroken)
            tooltip.add(Component.translatable("info.cgm.broken").withStyle(ChatFormatting.DARK_RED));

        if (Screen.hasControlDown()) {
        	tooltip.add(Component.translatable("info.cgm.stats").withStyle(ChatFormatting.GOLD));
            /* Additional Damage */ {
                if(tagCompound != null) {
                    if(tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC))
                    {
                        float additionalDamage = tagCompound.getFloat("AdditionalDamage");
                        additionalDamage += GunModifierHelper.getAdditionalDamage(stack);

                        if(additionalDamage > 0) {
                            additionalDamageText = ChatFormatting.GREEN + " +" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                        }
                        else if(additionalDamage < 0) {
                            additionalDamageText = ChatFormatting.RED + " " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                        }
                    }
                }
            }
            /* Damage */ {
                if (projectileAmount >= 2) {
                    tooltip.add(Component.translatable("info.cgm.damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + ChatFormatting.GRAY + " (" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage / projectileAmount) + "*" + projectileAmount + ")").withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(Component.translatable("info.cgm.damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText).withStyle(ChatFormatting.GRAY));
                }
            }
            /* Headshot damage */ {
                if(headshotDamage != damage) {
                    if (projectileAmount >= 2) {
                        tooltip.add(Component.translatable("info.cgm.headshot_damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(headshotDamage) + ChatFormatting.GRAY + " (" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(headshotDamage / projectileAmount) + "*" + projectileAmount + ")").withStyle(ChatFormatting.GRAY));
                    } else {
                        tooltip.add(Component.translatable("info.cgm.headshot_damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(headshotDamage) + additionalDamageText).withStyle(ChatFormatting.GRAY));
                    }
                }
            }
            /* Fire Rate */ {
                tooltip.add(Component.translatable("info.cgm.rate", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(fireRateRPM)).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.translatable("info.cgm.line").withStyle(ChatFormatting.DARK_GRAY));
            /* Ammo Type */ {
                if(ammoType != null && !Gun.usesEnergy(stack)) {
                    tooltip.add(Component.translatable("info.cgm.ammo_type", Component.translatable(ammoType.getDescriptionId()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
                }
            }
            /* Ammo Capacity */ {
                if(tagCompound != null) {
                    if(Gun.hasInfiniteAmmo(stack)) {
                        if(!Gun.usesEnergy(stack))
                            tooltip.add(Component.translatable("info.cgm.ammo", ChatFormatting.LIGHT_PURPLE + "∞" + "/" + "∞").withStyle(ChatFormatting.GRAY));
                    }
                    else {
                        int ammoCount = tagCompound.getInt("AmmoCount");
                        if(ammoCount == 0) {
                            tooltip.add(Component.translatable("info.cgm.ammo", ChatFormatting.RED.toString() + ammoCount + "/" + GunCompositeStatHelper.getAmmoCapacity(stack, modifiedGun)).withStyle(ChatFormatting.GRAY));
                        }
                        else {
                            tooltip.add(Component.translatable("info.cgm.ammo", ChatFormatting.WHITE.toString() + ammoCount + "/" + GunCompositeStatHelper.getAmmoCapacity(stack, modifiedGun)).withStyle(ChatFormatting.GRAY));
                        }
                    }
                    if(Gun.usesEnergy(stack)) {
                        int energy = tagCompound.getInt("Energy");
                        tooltip.add(Component.translatable("info.cgm.energy", ChatFormatting.WHITE.toString() + energy + "/" + modifiedGun.getGeneral().getEnergyCapacity()).withStyle(ChatFormatting.AQUA));
                    }
                }
            }
            /* Armor and protection piercing */ {
            tooltip.add(Component.translatable("info.cgm.armor_pierce", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(armorPiercePercent) + "%").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("info.cgm.protection_pierce", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(protectionPiercePercent) + "%").withStyle(ChatFormatting.GRAY));
        }
            /* Pierce count */ {
            if (maxPierceCount > 0) {
                tooltip.add(Component.translatable("info.cgm.pierce_count", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxPierceCount)).withStyle(ChatFormatting.GRAY));
            }
            if (maxPierceCount == -1) {
                tooltip.add(Component.translatable("info.cgm.pierce_count", ChatFormatting.LIGHT_PURPLE + "∞").withStyle(ChatFormatting.GRAY));
            }
        }
            tooltip.add(Component.translatable("info.cgm.line").withStyle(ChatFormatting.DARK_GRAY));
            /* Reload Time */ {
            if(modifiedGun.getGeneral().usesMagReload()) {
                reloadTimeSeconds = (float) GunCompositeStatHelper.getMagReloadSpeed(stack, false) / 20;
            }
            else {
                reloadTimeSeconds = (float) GunCompositeStatHelper.getReloadInterval(stack, false) / 20;
            }
            tooltip.add(Component.translatable("info.cgm.reload_rate", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(reloadTimeSeconds)).withStyle(ChatFormatting.GRAY));
        }
            /* ADS Time */ {
                if(modifiedGun.getModules().getZoom() != null) {
                    tooltip.add(Component.translatable("info.cgm.ads_time", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsTimeSeconds)).withStyle(ChatFormatting.GRAY));
                }
            }
            /* Draw time */ {
                tooltip.add(Component.translatable("info.cgm.draw_time", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(drawTimeSeconds)).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.translatable("info.cgm.line").withStyle(ChatFormatting.DARK_GRAY));
            /* Recoil */ {
                tooltip.add(Component.translatable("info.cgm.recoil").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(recoilDegrees) + "°").withStyle(ChatFormatting.WHITE)));
            }
            /* ADS Recoil */ {
                if (adsRecoilDegrees!=recoilDegrees && modifiedGun.getModules().getZoom() != null) {
                    tooltip.add(Component.translatable("info.cgm.ads_recoil").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsRecoilDegrees) + "°").withStyle(ChatFormatting.WHITE)));
                }
            }
            /* Spread */ {
                if ((minSpreadDegrees!=spreadDegrees) && ((minSpreadDegrees>0) || (!isAlwaysSpread))) {
                    tooltip.add(Component.translatable("info.cgm.spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(minSpreadDegrees) + "°-" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(spreadDegrees) + "°").withStyle(ChatFormatting.WHITE)));
                }
                else {
                    tooltip.add(Component.translatable("info.cgm.spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(spreadDegrees) + "°").withStyle(ChatFormatting.WHITE)));
                }
            }
        	/* ADS Spread */ {
                boolean checkSpread;
                if(useSniperSpread) {
                    adsSpreadDegrees = spreadDegrees;
                    checkSpread = true;
                }
                else {
                    checkSpread = adsSpreadDegrees != spreadDegrees;
                }

                if(checkSpread && modifiedGun.getModules().getZoom() != null) {
                    if ((adsMinSpreadDegrees!=adsSpreadDegrees) && ((adsMinSpreadDegrees>0) || (!isAlwaysSpread))) {
                        tooltip.add(Component.translatable("info.cgm.ads_spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsMinSpreadDegrees) + "°-" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsSpreadDegrees) + "°").withStyle(ChatFormatting.WHITE)));
                    }
                    else {
                        tooltip.add(Component.translatable("info.cgm.ads_spread").withStyle(ChatFormatting.GRAY).append(Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(adsSpreadDegrees) + "°").withStyle(ChatFormatting.WHITE)));
                    }
                }
            }
        }
        else {
            /* Additional Damage */ {
            if(tagCompound != null) {
                if(tagCompound.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC))
                {
                    float additionalDamage = tagCompound.getFloat("AdditionalDamage");
                    additionalDamage += GunModifierHelper.getAdditionalDamage(stack);

                    if(additionalDamage > 0) {
                        additionalDamageText = ChatFormatting.GREEN + " +" + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                    }
                    else if(additionalDamage < 0) {
                        additionalDamageText = ChatFormatting.RED + " " + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(additionalDamage);
                    }
                }
            }
        }
            /* Damage */ {
                tooltip.add(Component.translatable("info.cgm.damage", ChatFormatting.WHITE + ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage) + additionalDamageText).withStyle(ChatFormatting.GRAY));
            }
            /* Ammo Type */ {
            if(ammoType != null && !Gun.usesEnergy(stack)) {
                tooltip.add(Component.translatable("info.cgm.ammo_type", Component.translatable(ammoType.getDescriptionId()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
            }
        }
            /* Ammo Capacity */ {
            if(tagCompound != null) {
                if(Gun.hasInfiniteAmmo(stack)) {
                    if(!Gun.usesEnergy(stack))
                        tooltip.add(Component.translatable("info.cgm.ammo", ChatFormatting.LIGHT_PURPLE + "∞" + "/" + "∞").withStyle(ChatFormatting.GRAY));
                }
                else {
                    int ammoCount = tagCompound.getInt("AmmoCount");
                    if(ammoCount == 0) {
                        tooltip.add(Component.translatable("info.cgm.ammo", ChatFormatting.RED.toString() + ammoCount + "/" + GunCompositeStatHelper.getAmmoCapacity(stack, modifiedGun)).withStyle(ChatFormatting.GRAY));
                    }
                    else {
                        tooltip.add(Component.translatable("info.cgm.ammo", ChatFormatting.WHITE.toString() + ammoCount + "/" + GunCompositeStatHelper.getAmmoCapacity(stack, modifiedGun)).withStyle(ChatFormatting.GRAY));
                    }
                }
                if(Gun.usesEnergy(stack)) {
                    int energy = tagCompound.getInt("Energy");
                    tooltip.add(Component.translatable("info.cgm.energy", ChatFormatting.WHITE.toString() + energy + "/" + modifiedGun.getGeneral().getEnergyCapacity()).withStyle(ChatFormatting.AQUA));
                }
            }
        }
        	tooltip.add(Component.translatable("info.cgm.stats_help").withStyle(ChatFormatting.GOLD));
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity)
    {
        return true;
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab group, @NotNull NonNullList<ItemStack> stacks)
    {
        if(this.allowedIn(group))
        {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putInt("AmmoCount", this.gun.getGeneral().getMaxAmmo());
            stack.getOrCreateTag().putInt("Energy", 0);
            stacks.add(stack);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack)
    {
        if (stack.getDamageValue() >= (stack.getMaxDamage() * 0.8)) {
            return Objects.requireNonNull(ChatFormatting.RED.getColor());
        }
        float stackMaxDamage = this.getMaxDamage(stack);
        float f = Math.max(0.0F, (stackMaxDamage - (float)stack.getDamageValue()) / stackMaxDamage);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    public Gun getModifiedGun(ItemStack stack)
    {
        CompoundTag tagCompound = stack.getTag();
        if(tagCompound != null && tagCompound.contains("Gun", Tag.TAG_COMPOUND))
        {
            return this.modifiedGunCache.computeIfAbsent(tagCompound, item ->
            {
                if(tagCompound.getBoolean("Custom"))
                {
                    return Gun.create(tagCompound.getCompound("Gun"));
                }
                else
                {
                    Gun gunCopy = this.gun.copy();
                    gunCopy.deserializeNBT(tagCompound.getCompound("Gun"));
                    return gunCopy;
                }
            });
        }
        if(GunMod.isDebugging())
        {
            return Debug.getGun(this);
        }
        return this.gun;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions()
        {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer()
            {
                return new GunItemStackRenderer();
            }
        });
    }

    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair)
    {
        return repair.is(ModBlocks.GUN_REPAIR_KIT.get().asItem());
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack)
    {
        return false;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack)
    {
        return false;
    }

    // Everything below is related to energy storage and transfer mechanics via Forge's EnergyStorage capability.

    public static void setCurrentEnergy(ItemStack stack, int Amount)
    {
    	stack.getOrCreateTag().putInt("Energy", Amount);
    }

    public static int getCurrentEnergy(ItemStack stack)
    {
    	return stack.getOrCreateTag().getInt("Energy");
    }

    public static int getEnergyCapacity(ItemStack stack)
    {
    	GunItem gunItem = (GunItem) stack.getItem();
    	Gun modifiedGun = gunItem.getModifiedGun(stack);
    	return modifiedGun.getGeneral().getEnergyCapacity();
    }

    public static int getTransferCap(ItemStack stack)
    {
    	GunItem gunItem = (GunItem) stack.getItem();
    	Gun modifiedGun = gunItem.getModifiedGun(stack);
    	return modifiedGun.getGeneral().getEnergyCapacity()/80;
    }
    
    public static class GunEnergyStorage implements IEnergyStorage, ICapabilityProvider {
        private final LazyOptional<IEnergyStorage> user = LazyOptional.of(() -> this);
        private final ItemStack stack;
        
        public GunEnergyStorage(ItemStack stack){
            this.stack = stack;
        }
    
        public void setCurrentEnergy(int amount){
            GunItem.setCurrentEnergy(this.stack, amount);
        }
    
        @Override
        public int getEnergyStored(){
            return GunItem.getCurrentEnergy(this.stack);
        }
    
        @Override
        public int getMaxEnergyStored(){
            return GunItem.getEnergyCapacity(this.stack);
        }
    
        @Override
        public boolean canExtract(){
            return this.getEnergyStored() > 0;
        }
    
        @Override
        public boolean canReceive(){
            return this.getEnergyStored() < this.getMaxEnergyStored();
        }
    
        @Override
        public int receiveEnergy(int receiveEnergy, boolean simulate){
            if (!canReceive()){
                return 0;
            }
        
            int stored = this.getEnergyStored();
            int energyReceived = Math.min(this.getMaxEnergyStored() - stored, Math.min(GunItem.getTransferCap(this.stack), receiveEnergy));
            if (!simulate){
                this.setCurrentEnergy(stored + energyReceived);
            }
            return energyReceived;
        }
    
        @Override
        public int extractEnergy(int extractEnergy, boolean simulate){
            if (!canExtract()){
                return 0;
            }
        
            int stored = this.getEnergyStored();
            int energyExtracted = Math.min(stored, Math.min(GunItem.getTransferCap(this.stack), extractEnergy));
            if (!simulate){
                this.setCurrentEnergy(stored - energyExtracted);
            }
            return energyExtracted;
        }
        
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
            return CapabilityEnergy.ENERGY.orEmpty(cap, user);
        }
    }
}

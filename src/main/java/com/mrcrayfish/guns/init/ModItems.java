package com.mrcrayfish.guns.init;

import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.common.Attachments;
import com.mrcrayfish.guns.common.GunModifiers;
import com.mrcrayfish.guns.item.AmmoItem;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.PouchItem;
import com.mrcrayfish.guns.item.UnobtainableItem;
import com.mrcrayfish.guns.item.attachment.*;
import com.mrcrayfish.guns.item.attachment.impl.create.Barrel;
import com.mrcrayfish.guns.item.attachment.impl.create.Magazine;
import com.mrcrayfish.guns.item.attachment.impl.create.Stock;
import com.mrcrayfish.guns.item.attachment.impl.create.UnderBarrel;
import com.mrcrayfish.guns.item.grenade.*;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);

    /* When specifying durability, calculate it like this:
    * (count * mag) + 1
    * count = amount of magazines the gun can fire
    * mag = magazine capacity */

    /* Assault Rifles */
    public static final RegistryObject<GunItem> ASSAULT_RIFLE = REGISTER.register("assault_rifle", () -> new GunItem(new Item.Properties()
            .durability((32 * 30) + 1) // 32 mags, 960 shots
            ));
    public static final RegistryObject<GunItem> TACTICAL_RIFLE = REGISTER.register("tactical_rifle", () -> new GunItem(new Item.Properties()
            .durability((32 * 30) + 1) // 32 mags, 960 shots
            ));
    public static final RegistryObject<GunItem> COMBAT_RIFLE = REGISTER.register("combat_rifle", () -> new GunItem(new Item.Properties()
            .durability((32 * 30) + 1) // 32 mags, 960 shots
            ));

    /* Sniper Rifles */
    public static final RegistryObject<GunItem> SNIPER_RIFLE = REGISTER.register("sniper_rifle",
            () -> new GunItem(new Item.Properties()
                    .durability((48 * 10) + 1) // 48 mags, 480 shots
            ));
    public static final RegistryObject<GunItem> HEAVY_SNIPER_RIFLE = REGISTER.register("heavy_sniper_rifle", () -> new GunItem(new Item.Properties()
            .durability((48 * 5) + 1) // 48 mags, 240 shots
            ));

    /* Shotguns */
    public static final RegistryObject<GunItem> SEMI_AUTO_SHOTGUN = REGISTER.register("semi_auto_shotgun", () -> new GunItem(new Item.Properties()
            .durability((48 * 7) + 1) // 48 mags, 336 shots
            ));

    /* Machine Guns */
    public static final RegistryObject<GunItem> MINI_GUN = REGISTER.register("mini_gun", () -> new GunItem(new Item.Properties()
            .durability((8 * 200) + 1) // 8 mags, 1600 shots
            ));

    /* Submachine Guns */

    /* Pistols */
    public static final RegistryObject<GunItem> TACTICAL_PISTOL = REGISTER.register("tactical_pistol", () -> new GunItem(new Item.Properties()
            .durability((48 * 12) + 1) // 48 mags, 576 shots
            ));
    public static final RegistryObject<GunItem> AUTOMATIC_PISTOL = REGISTER.register("automatic_pistol", () -> new GunItem(new Item.Properties()
            .durability((24 * 32) + 1) // 24 mags, 768 shots
            ));

    /* Explosives */
    public static final RegistryObject<GunItem> GRENADE_LAUNCHER = REGISTER.register("grenade_launcher", () -> new GunItem(new Item.Properties()
            .durability((24 * 6) + 1) // 24 mags, 144 shots
            ));
    public static final RegistryObject<GunItem> ROCKET_LAUNCHER = REGISTER.register("rocket_launcher", () -> new GunItem(new Item.Properties()
            .durability((96 * 1) + 1) // 96 shots
            ));

    /* Ammo */
    public static final RegistryObject<Item> LIGHT_BULLET = REGISTER.register("light_bullet", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_BULLET = REGISTER.register("medium_bullet", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_BULLET = REGISTER.register("heavy_bullet", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> BUCKSHOT_SHELL = REGISTER.register("buckshot_shell", () -> new AmmoItem(new Item.Properties()));
    public static final RegistryObject<Item> ROCKET = REGISTER.register("rocket", () -> new AmmoItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> PIPE_GRENADE = REGISTER.register("pipe_grenade", () -> new AmmoItem(new Item.Properties().stacksTo(16)));

    /* Grenades */
    public static final RegistryObject<Item> GRENADE = REGISTER.register("grenade", () -> new GrenadeItem(new Item.Properties(),
            20 * 3));
    public static final RegistryObject<Item> STUN_GRENADE = REGISTER.register("stun_grenade", () -> new StunGrenadeItem(new Item.Properties(),
            20 * 2));
    public static final RegistryObject<Item> SMOKE_GRENADE = REGISTER.register("smoke_grenade", () -> new SmokeGrenadeItem(new Item.Properties(),
            20 * 5));
    public static final RegistryObject<Item> INCENDIARY_GRENADE = REGISTER.register("incendiary_grenade", () -> new IncendiaryGrenadeItem(new Item.Properties(),
            20 * 3));
    public static final RegistryObject<Item> MOLOTOV = REGISTER.register("molotov", () -> new MolotovItem(new Item.Properties(),
            20 * 10));

    public static final RegistryObject<Item> GRENADE_NO_PIN = REGISTER.register("grenade_no_pin", () -> new UnobtainableItem(new Item.Properties()));
    public static final RegistryObject<Item> STUN_GRENADE_NO_PIN = REGISTER.register("stun_grenade_no_pin", () -> new UnobtainableItem(new Item.Properties()));
    public static final RegistryObject<Item> SMOKE_GRENADE_NO_PIN = REGISTER.register("smoke_grenade_no_pin", () -> new UnobtainableItem(new Item.Properties()));
    public static final RegistryObject<Item> INCENDIARY_GRENADE_NO_PIN = REGISTER.register("incendiary_grenade_no_pin", () -> new UnobtainableItem(new Item.Properties()));

    /* Scopes */
    public static final RegistryObject<Item> RED_DOT_SIGHT = REGISTER.register("red_dot_sight", () -> new ScopeItem(Attachments.RED_DOT_SIGHT, new Item.Properties()));
    public static final RegistryObject<Item> X2_SCOPE = REGISTER.register("x2_scope", () -> new ScopeItem(Attachments.X2_SCOPE, new Item.Properties()));
    public static final RegistryObject<Item> X4_SCOPE = REGISTER.register("x4_scope", () -> new ScopeItem(Attachments.X4_SCOPE, new Item.Properties()));
    public static final RegistryObject<Item> X6_SCOPE = REGISTER.register("x6_scope", () -> new ScopeItem(Attachments.X6_SCOPE, new Item.Properties()));

    /* Barrels */
    public static final RegistryObject<Item> SILENCER = REGISTER.register("silencer", () -> new BarrelItem(Barrel.create(10f, GunModifiers.SILENCER), new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_SILENCER = REGISTER.register("heavy_silencer", () -> new BarrelItem(Barrel.create(12f, GunModifiers.HEAVY_SILENCER), new Item.Properties()));

    /* Stocks */
    public static final RegistryObject<Item> LIGHT_STOCK = REGISTER.register("light_stock", () -> new StockItem(Stock.create(GunModifiers.LIGHT_STOCK), new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_STOCK = REGISTER.register("medium_stock", () -> new StockItem(Stock.create(GunModifiers.MEDIUM_STOCK), new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_STOCK = REGISTER.register("heavy_stock", () -> new StockItem(Stock.create(GunModifiers.HEAVY_STOCK), new Item.Properties()));

    /* Grips */
    public static final RegistryObject<Item> HORIZONTAL_GRIP = REGISTER.register("horizontal_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.HORIZONTAL_GRIP), new Item.Properties()));
    public static final RegistryObject<Item> VERTICAL_GRIP = REGISTER.register("vertical_grip", () -> new UnderBarrelItem(UnderBarrel.create(GunModifiers.VERTICAL_GRIP), new Item.Properties()));

    /* Tactical */

    /* Magazines */
    public static final RegistryObject<Item> LIGHT_MAG = REGISTER.register("light_magazine", () -> new MagazineItem(Magazine.create(GunModifiers.LIGHT_MAG), new Item.Properties()));
    public static final RegistryObject<Item> EXTENDED_MAG = REGISTER.register("extended_magazine", () -> new MagazineItem(Magazine.create(GunModifiers.EXTENDED_MAG), new Item.Properties()));

    /* Misc */
    public static final RegistryObject<Item> GUN_REPAIR_KIT = REGISTER.register("gun_repair_kit",() -> new Item(new Item.Properties().stacksTo(16).tab(GunMod.GUNS)));
    public static final RegistryObject<Item> TACTICAL_BELT = REGISTER.register("tactical_belt",() -> new PouchItem(new Item.Properties().tab(GunMod.GUNS),
            4,
            ModTags.Items.AMMO));

    /* Materials */
    public static final RegistryObject<Item> STURDY_MECHANISM = REGISTER.register("sturdy_mechanism",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> INCOMPLETE_STURDY_MECHANISM = REGISTER.register("incomplete_sturdy_mechanism",() -> new SequencedAssemblyItem(new Item.Properties()));

    public static final RegistryObject<Item> LIGHT_BULLET_MODEL = REGISTER.register("light_bullet_model", () -> new UnobtainableItem(new Item.Properties()));
    public static final RegistryObject<Item> MEDIUM_BULLET_MODEL = REGISTER.register("medium_bullet_model", () -> new UnobtainableItem(new Item.Properties()));
    public static final RegistryObject<Item> HEAVY_BULLET_MODEL = REGISTER.register("heavy_bullet_model", () -> new UnobtainableItem(new Item.Properties()));
    public static final RegistryObject<Item> BUCKSHOT_MODEL = REGISTER.register("buckshot_model", () -> new UnobtainableItem(new Item.Properties()));

    public static final RegistryObject<Item> BRASS_CASING = REGISTER.register("brass_casing",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> SHELL_CASING = REGISTER.register("shell_casing",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> BULLET = REGISTER.register("bullet",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> BUCKSHOT = REGISTER.register("buckshot",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> GUNPOWDER_PINCH = REGISTER.register("gunpowder_pinch",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));

    public static final RegistryObject<Item> CAST_IRON_INGOT = REGISTER.register("cast_iron_ingot",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> CAST_IRON_NUGGET = REGISTER.register("cast_iron_nugget",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> CAST_IRON_SHEET = REGISTER.register("cast_iron_sheet",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> CAST_IRON_ROD = REGISTER.register("cast_iron_rod",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));

    public static final RegistryObject<Item> STEEL_INGOT = REGISTER.register("steel_ingot",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> STEEL_NUGGET = REGISTER.register("steel_nugget",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> STEEL_SHEET = REGISTER.register("steel_sheet",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> STEEL_ROD = REGISTER.register("steel_rod",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));

    public static final RegistryObject<Item> IRON_ROD = REGISTER.register("iron_rod",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> GOLDEN_ROD = REGISTER.register("golden_rod",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> COPPER_ROD = REGISTER.register("copper_rod",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> BRASS_ROD = REGISTER.register("brass_rod",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));

    public static final RegistryObject<Item> HEMP_SEEDS = REGISTER.register("hemp_seeds",() -> new ItemNameBlockItem(ModBlocks.HEMP_CROP_BLOCK.get(), new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> HEMP = REGISTER.register("hemp",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> CLOTH = REGISTER.register("cloth",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> RUBBER = REGISTER.register("rubber",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> RUBBER_SHEET = REGISTER.register("rubber_sheet",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> PLASTIC = REGISTER.register("plastic",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
    public static final RegistryObject<Item> PLASTIC_SHEET = REGISTER.register("plastic_sheet",() -> new Item(new Item.Properties().tab(GunMod.MATERIALS)));
}
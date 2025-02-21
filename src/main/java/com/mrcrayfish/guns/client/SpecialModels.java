package com.mrcrayfish.guns.client;

import com.mrcrayfish.guns.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum SpecialModels
{
    ASSAULT_RIFLE("z_gun/assault_rifle"),
    BAZOOKA("z_gun/rocket_launcher"),
    HEAVY_RIFLE("z_gun/heavy_sniper_rifle"),
    MACHINE_PISTOL("z_gun/automatic_pistol"),
    PISTOL("z_gun/tactical_pistol"),
    RIFLE("z_gun/sniper_rifle"),
    SHOTGUN("z_gun/semi_auto_shotgun"),
    FLAME("flame"),

    MINI_GUN_BASE("mini_gun/mini_gun_base"),
    MINI_GUN_BARRELS("mini_gun/mini_gun_barrels"),

    GRENADE_LAUNCHER_BASE("grenade_launcher/grenade_launcher_base"),
    GRENADE_LAUNCHER_CYLINDER("grenade_launcher/grenade_launcher_cylinder"),

    ASSAULT_RIFLE_BASE("assault_rifle/base"),
    ASSAULT_RIFLE_NO_STOCK("assault_rifle/no_stock"),
    ASSAULT_RIFLE_MAG("assault_rifle/mag"),
    ASSAULT_RIFLE_SIGHTS("assault_rifle/sights"),
    ASSAULT_RIFLE_NO_SIGHTS("assault_rifle/no_sights"),
    ASSAULT_RIFLE_LIGHT_MAG("assault_rifle/light_mag"),
    ASSAULT_RIFLE_EXT_MAG("assault_rifle/ext_mag"),

    COMBAT_RIFLE_BASE("combat_rifle/base"),
    COMBAT_RIFLE_SIGHTS("combat_rifle/sights"),
    COMBAT_RIFLE_NO_SIGHTS("combat_rifle/no_sights"),

    HEAVY_RIFLE_BASE("heavy_sniper_rifle/base"),

    PISTOL_BASE("tactical_pistol/base"),
    PISTOL_SLIDE("tactical_pistol/slide"),
    PISTOL_MAG("tactical_pistol/mag"),
    PISTOL_EXT_MAG("tactical_pistol/ext_mag"),

    MACHINE_PISTOL_BASE("automatic_pistol/base"),
    MACHINE_PISTOL_SLIDE("automatic_pistol/slide"),
    MACHINE_PISTOL_MAG("automatic_pistol/mag"),
    MACHINE_PISTOL_SIGHTS("automatic_pistol/sights"),
    MACHINE_PISTOL_NO_SIGHTS("automatic_pistol/no_sights"),
    MACHINE_PISTOL_LIGHT_MAG("automatic_pistol/light_mag"),
    MACHINE_PISTOL_EXT_MAG("automatic_pistol/ext_mag"),

    SHOTGUN_BASE("semi_auto_shotgun/base"),
    SHOTGUN_HANDLE("semi_auto_shotgun/handle"),
    SHOTGUN_NO_HANDLE("semi_auto_shotgun/no_handle"),
    SHOTGUN_SLIDE("semi_auto_shotgun/slide");

    /**
     * The location of an item model in the [MOD_ID]/models/special/[NAME] folder
     */
    private final ResourceLocation modelLocation;

    /**
     * Cached model
     */
    private BakedModel cachedModel;

    /**
     * Sets the model's location
     *
     * @param modelName name of the model file
     */
    SpecialModels(String modelName)
    {
        this.modelLocation = new ResourceLocation(Reference.MOD_ID, "special/" + modelName);
    }

    /**
     * Gets the model
     *
     * @return isolated model
     */
    public BakedModel getModel()
    {
        if(this.cachedModel == null)
        {
            this.cachedModel = Minecraft.getInstance().getModelManager().getModel(this.modelLocation);
        }
        return this.cachedModel;
    }

    /**
     * Registers the special models into the Forge Model Bakery. This is only called once on the
     * load of the game.
     */
    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event)
    {
        for(SpecialModels model : values())
        {
            event.register(model.modelLocation);
        }
    }

    /**
     * Clears the cached BakedModel since it's been rebuilt. This is needed since the models may
     * have changed when a resource pack was applied, or if resources are reloaded.
     */
    @SubscribeEvent
    public static void onBake(ModelEvent.BakingCompleted event)
    {
        for(SpecialModels model : values())
        {
            model.cachedModel = null;
        }
    }
}

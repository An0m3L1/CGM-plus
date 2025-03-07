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
    ROCKET_LAUNCHER("z_gun/rocket_launcher"),
    SNIPER_RIFLE("z_gun/sniper_rifle"),
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

    HEAVY_SNIPER_RIFLE_BASE("heavy_sniper_rifle/base"),
    HEAVY_SNIPER_RIFLE_SIGHTS("heavy_sniper_rifle/sights"),
    HEAVY_SNIPER_RIFLE_BOLT("heavy_sniper_rifle/bolt"),
    HEAVY_SNIPER_RIFLE_CHAMBER("heavy_sniper_rifle/chamber"),
    HEAVY_SNIPER_RIFLE_MAG("heavy_sniper_rifle/mag"),

    TACTICAL_PISTOL_BASE("tactical_pistol/base"),
    TACTICAL_PISTOL_SLIDE("tactical_pistol/slide"),
    TACTICAL_PISTOL_MAG("tactical_pistol/mag"),
    TACTICAL_PISTOL_EXT_MAG("tactical_pistol/ext_mag"),

    AUTOMATIC_PISTOL_BASE("automatic_pistol/base"),
    AUTOMATIC_PISTOL_SLIDE("automatic_pistol/slide"),
    AUTOMATIC_PISTOL_MAG("automatic_pistol/mag"),
    AUTOMATIC_PISTOL_SIGHTS("automatic_pistol/sights"),
    AUTOMATIC_PISTOL_NO_SIGHTS("automatic_pistol/no_sights"),
    AUTOMATIC_PISTOL_LIGHT_MAG("automatic_pistol/light_mag"),
    AUTOMATIC_PISTOL_EXT_MAG("automatic_pistol/ext_mag"),

    SEMI_AUTO_SHOTGUN_BASE("semi_auto_shotgun/base"),
    SEMI_AUTO_SHOTGUN_HANDLE("semi_auto_shotgun/handle"),
    SEMI_AUTO_SHOTGUN_NO_HANDLE("semi_auto_shotgun/no_handle"),
    SEMI_AUTO_SHOTGUN_SLIDE("semi_auto_shotgun/slide");

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

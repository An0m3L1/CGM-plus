package com.mrcrayfish.guns.common.network;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.blockentity.WorkbenchBlockEntity;
import com.mrcrayfish.guns.common.*;
import com.mrcrayfish.guns.common.Gun.ReloadSoundsBase;
import com.mrcrayfish.guns.common.container.AttachmentContainer;
import com.mrcrayfish.guns.common.container.WorkbenchContainer;
import com.mrcrayfish.guns.crafting.WorkbenchRecipe;
import com.mrcrayfish.guns.crafting.WorkbenchRecipes;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.event.GunFireEvent;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.interfaces.IProjectileFactory;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.IColored;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.C2SMessageFireSwitch;
import com.mrcrayfish.guns.network.message.C2SMessageShoot;
import com.mrcrayfish.guns.network.message.S2CMessageBulletTrail;
import com.mrcrayfish.guns.network.message.S2CMessageGunSound;
import com.mrcrayfish.guns.particles.TrailData;
import com.mrcrayfish.guns.util.GunCompositeStatHelper;
import com.mrcrayfish.guns.util.GunModifierHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class ServerPlayHandler
{
    private static final Predicate<LivingEntity> HOSTILE_ENTITIES = entity -> entity.getSoundSource() == SoundSource.HOSTILE && !(entity instanceof NeutralMob) && !Config.COMMON.exemptEntities.get().contains(EntityType.getKey(entity.getType()).toString());

    /**
     * Fires the weapon the player is currently holding.
     * This is only intended for use on the logical server.
     *
     * @param player the player for whose weapon to fire
     */
    public static void handleShoot(C2SMessageShoot message, ServerPlayer player)
    {
        if(player.isSpectator())
            return;

        if(player.getUseItem().getItem() == Items.SHIELD)
            return;

        Level world = player.level;
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.getItem() instanceof GunItem item && (Gun.hasAmmo(heldItem) || player.isCreative()))
        {
            Gun modifiedGun = item.getModifiedGun(heldItem);
            if(modifiedGun != null)
            {
                if(MinecraftForge.EVENT_BUS.post(new GunFireEvent.Pre(player, heldItem)))
                    return;

                /* Updates the yaw and pitch with the clients current yaw and pitch */
                player.setYRot(Mth.wrapDegrees(message.getRotationYaw()));
                player.setXRot(Mth.clamp(message.getRotationPitch(), -90F, 90F));
                
                if(ModSyncedDataKeys.SWITCHTIME.getValue(player) > 0) // Disallow firing during the weapon switch/reload time - server-side fallback.
                {
                	return;
                }

                ShootTracker tracker = ShootTracker.getShootTracker(player);
                if(tracker.hasCooldown(item) && tracker.getRemaining(item) > Config.SERVER.cooldownThreshold.get())
                {
                    GunMod.LOGGER.warn("{}({}) tried to fire before cooldown finished! Is the server lagging? Remaining milliseconds: {}", player.getName().getContents(), player.getUUID(), tracker.getRemaining(item));
                    return;
                }
                tracker.putCooldown(player, heldItem, item, modifiedGun);

                if(ModSyncedDataKeys.RELOADING.getValue(player))
                {
                    ModSyncedDataKeys.RELOADING.setValue(player, false);
                }

                if((!modifiedGun.getGeneral().isAlwaysSpread() && modifiedGun.getGeneral().getSpread() > 0.0F) || modifiedGun.getGeneral().getRestingSpread() > 0F)
                {
                    SpreadTracker.get(player).update(player, item);
                }

                int count = modifiedGun.getGeneral().getProjectileAmount();
                Gun.Projectile projectileProps = modifiedGun.getProjectile();
                ResourceLocation projectileItem = (projectileProps.getProjectileItem());
                ProjectileEntity[] spawnedProjectiles = new ProjectileEntity[count];
                for(int i = 0; i < count; i++)
                {
                    IProjectileFactory factory = ProjectileManager.getInstance().getFactory(projectileItem);
                    IProjectileFactory override = (projectileProps.getProjectileOverride() != null ? ProjectileManager.getInstance().getOverride(projectileProps.getProjectileOverride()) : null);
                    if (override != null)
                    {
                        factory = override;
                    }
                    
                    ProjectileEntity projectileEntity = factory.create(world, player, heldItem, item, modifiedGun);
                    projectileEntity.setWeapon(heldItem);
                    projectileEntity.setAdditionalDamage(Gun.getAdditionalDamage(heldItem));
                    world.addFreshEntity(projectileEntity);
                    spawnedProjectiles[i] = projectileEntity;
                    projectileEntity.tick();
                }
                if(!projectileProps.isVisible())
                {
                    double spawnX = player.getX();
                    double spawnY = player.getY() + 1.0;
                    double spawnZ = player.getZ();
                    double radius = Config.COMMON.projectileTrackingRange.get();
                    ParticleOptions data = new TrailData(heldItem.isEnchanted());
                    S2CMessageBulletTrail messageBulletTrail = new S2CMessageBulletTrail(spawnedProjectiles, projectileProps, player.getId(), data);
                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, spawnX, spawnY, spawnZ, radius), messageBulletTrail);
                }

                MinecraftForge.EVENT_BUS.post(new GunFireEvent.Post(player, heldItem));

                if(Config.COMMON.scareMobs.get())
                {
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotSoundDistance.get());
                    double x = player.getX();
                    double y = player.getY() + 0.5;
                    double z = player.getZ();
                    AABB box = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
                    radius *= radius;
                    double dx, dy, dz;
                    for(LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, box, HOSTILE_ENTITIES))
                    {
                        dx = x - entity.getX();
                        dy = y - entity.getY();
                        dz = z - entity.getZ();
                        if(dx * dx + dy * dy + dz * dz <= radius)
                        {
                            entity.setLastHurtByMob(Config.COMMON.angerHostileMobs.get() ? player : entity);
                        }
                    }
                }

                ResourceLocation fireSound = getFireSound(heldItem, modifiedGun);
                if(fireSound != null)
                {
                    double posX = player.getX();
                    double posY = player.getY() + player.getEyeHeight();
                    double posZ = player.getZ();
                    float volume = GunModifierHelper.getFireSoundVolume(heldItem);
                    float pitch = 0.9F + world.random.nextFloat() * 0.2F;
                    double radius = GunModifierHelper.getModifiedFireSoundRadius(heldItem, Config.SERVER.gunShotSoundDistance.get());
                    boolean muzzle = modifiedGun.getDisplay().getFlash() != null;
                    S2CMessageGunSound messageSound = new S2CMessageGunSound(fireSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, volume, pitch, player.getId(), muzzle, false);
                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, posX, posY, posZ, radius), messageSound);
                }

                ResourceLocation cycleSound = getGunSound(modifiedGun, "cycle", false, false);
                if(cycleSound != null || modifiedGun.getSounds().getCycleDelay() >= 0)
                {
            		final ResourceLocation finalSound = (cycleSound != null? cycleSound : getGunSound(modifiedGun, "cock", false, false));
            		
            		double modifiedCycleDelay = modifiedGun.getSounds().getCycleDelay();
            		modifiedCycleDelay = modifiedCycleDelay * (GunCompositeStatHelper.getCompositeRate(heldItem, modifiedGun, player) / Math.max(modifiedGun.getGeneral().getRate(),1.0));
            		final int trueCycleDelay = (int) Math.round(modifiedCycleDelay);
            		if (trueCycleDelay>0)
                    {
                    	final Player finalPlayer = player;
                    	DelayedTask.runAfter(trueCycleDelay, () ->
                    	{
                    		if (finalPlayer.isAlive())
	                    	{
		                    	double posX = finalPlayer.getX();
		                        double posY = finalPlayer.getY() + finalPlayer.getEyeHeight();
		                        double posZ = finalPlayer.getZ();
			                  	double radius = Config.SERVER.reloadSoundDistance.get();
			                    S2CMessageGunSound messageSound = new S2CMessageGunSound(finalSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, 1.0F, 1.0F, player.getId(), false, true);
			                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, posX, posY, posZ, radius), messageSound);
		                    }
                    	});
                    }
                	else
                	{
                		double posX = player.getX();
                        double posY = player.getY() + player.getEyeHeight();
                        double posZ = player.getZ();
	                  	double radius = Config.SERVER.reloadSoundDistance.get();
	                    S2CMessageGunSound messageSound = new S2CMessageGunSound(finalSound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, 1.0F, 1.0F, player.getId(), false, true);
	                    PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, posX, posY, posZ, radius), messageSound);
                	}
                }

                if(!player.isCreative())
                {
                    CompoundTag tag = heldItem.getOrCreateTag();
                    if(!Gun.hasInfiniteAmmo(heldItem))
                    {
                        tag.putInt("AmmoCount", Math.max(0, tag.getInt("AmmoCount") - 1));
                    }
                    if (Gun.usesEnergy(heldItem))
                    {
                    	tag.putInt("Energy", Math.max(0, tag.getInt("Energy") - modifiedGun.getGeneral().getEnergyPerShot()));
                    }
                }

                player.awardStat(Stats.ITEM_USED.get(item));
            }
        }
    }
    
    /**
     * Handles server-side fire mode switch logic.
     * This is only intended for use on the logical server.
     */
    public static void handleFireSwitch(C2SMessageFireSwitch message, ServerPlayer player)
    {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(heldItem.getItem() instanceof GunItem item)
        {
    		//GunMod.LOGGER.info("Switching fire mode to " + message.getFireMode());
        	CompoundTag tag = heldItem.getOrCreateTag();
        	tag.putInt("FireMode", message.getFireMode());
        }
    }

    private static ResourceLocation getFireSound(ItemStack stack, Gun modifiedGun)
    {
        ResourceLocation fireSound = null;
        if(GunModifierHelper.isSilencedFire(stack))
        {
            fireSound = modifiedGun.getSounds().getSilencedFireEx();
        }
        else if(stack.isEnchanted())
        {
            fireSound = modifiedGun.getSounds().getEnchantedFireEx();
        }
        if(fireSound != null)
        {
            return fireSound;
        }
        return modifiedGun.getSounds().getFireEx();
    }
    
    /**
     * Plays the reload start sound of the gun.
     * This is only intended for use on the logical server.
     *
     * @param player the player who is performing the reload
     */
    public static void playReloadStartSound(ServerPlayer player)
    {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
    	if(heldItem.getItem() instanceof GunItem item)
        {
            Gun modifiedGun = item.getModifiedGun(heldItem);
            if(modifiedGun != null)
            {
            	String soundType = "reloadStart";
            	boolean magReload = Gun.usesMagReloads(heldItem);
            	boolean reloadFromEmpty = !Gun.hasAmmo(heldItem);
            	ReloadSoundsBase soundObj = Gun.findReloadSoundObj(modifiedGun, soundType, magReload, reloadFromEmpty);
            	
            	final ResourceLocation finalSound = getGunReloadSound(modifiedGun, soundObj, soundType, magReload, reloadFromEmpty);
            	
            	if (Gun.getReloadSoundTimings(modifiedGun, soundObj, soundType, magReload, reloadFromEmpty)>0)
                {
                    DelayedTask.runAfter((int) Gun.getReloadSoundTimings(modifiedGun, soundObj, soundType, magReload, reloadFromEmpty), () ->
                            playReloadStartSound(player, finalSound));
                }
            	else
                {
                    playReloadStartSound(player, finalSound);
                }
            }
        }
    }
    
    private static ResourceLocation getGunSound(Gun modifiedGun, String soundType, boolean doMagReload, boolean reloadFromEmpty)
    {
    	ResourceLocation sound = null;
    	
    	if(soundType.equals("cycle"))
    		sound = modifiedGun.getSounds().getCycle();
    	else
    	if(soundType.equals("cock"))
    		sound = modifiedGun.getSounds().getCock();

        return sound;
    }
    
    private static ResourceLocation getGunReloadSound(Gun modifiedGun, ReloadSoundsBase soundObj, String soundType, boolean doMagReload, boolean reloadFromEmpty)
    {
    	ResourceLocation sound;
    	sound = Gun.getReloadSound(modifiedGun, soundObj, soundType, doMagReload, reloadFromEmpty);

        return sound;
    }
    
    public static void playReloadStartSound(Player player, ResourceLocation sound)
    {
    	if (sound == null)
    		return;
    	
    	double posX = player.getX();
		double posY = player.getY() + 1.0;
		double posZ = player.getZ();
		double radius = Config.SERVER.reloadSoundDistance.get();
		S2CMessageGunSound messageSound = new S2CMessageGunSound(sound, SoundSource.PLAYERS, (float) posX, (float) posY, (float) posZ, 1.0F, 1.0F, player.getId(), false, true);
		PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(player.level, posX, posY, posZ, radius), messageSound);
    }

    /**
     * Crafts the specified item at the workstation the player is currently using.
     * This is only intended for use on the logical server.
     *
     * @param player the player who is crafting
     * @param id     the id of an item which is registered as a valid workstation recipe
     * @param pos    the block position of the workstation the player is using
     */
    public static void handleCraft(ServerPlayer player, ResourceLocation id, BlockPos pos)
    {
        Level world = player.level;

        if(player.containerMenu instanceof WorkbenchContainer workbench)
        {
            if(workbench.getPos().equals(pos))
            {
                WorkbenchRecipe recipe = WorkbenchRecipes.getRecipeById(world, id);
                if(recipe == null || !recipe.hasMaterials(player))
                    return;

                recipe.consumeMaterials(player);

                WorkbenchBlockEntity workbenchBlockEntity = workbench.getWorkbench();

                /* Gets the color based on the dye */
                ItemStack stack = recipe.getItem();
                ItemStack dyeStack = workbenchBlockEntity.getInventory().get(0);
                if(dyeStack.getItem() instanceof DyeItem dyeItem)
                {
                    int color = dyeItem.getDyeColor().getTextColor();

                    if(IColored.isDyeable(stack))
                    {
                        IColored colored = (IColored) stack.getItem();
                        colored.setColor(stack, color);
                        workbenchBlockEntity.getInventory().set(0, ItemStack.EMPTY);
                    }
                }

                Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1.125, pos.getZ() + 0.5, stack);
            }
        }
    }

    public static void handleUnload(ServerPlayer player, boolean partial)
    {
        ItemStack stack = player.getMainHandItem();
        if(stack.getItem() instanceof GunItem gunItem)
        {
            CompoundTag tag = stack.getTag();
            if(tag != null && tag.contains("AmmoCount", Tag.TAG_INT) && !tag.getBoolean("IgnoreAmmo"))
            {
            	int ammoStored = tag.getInt("AmmoCount");
            	if (!Gun.hasUnlimitedReloads(stack))
            	{
	            	int count = Math.max(ammoStored - (partial ? GunCompositeStatHelper.getAmmoCapacity(stack) : 0),0);
	                tag.putInt("AmmoCount", partial ? Math.min(GunCompositeStatHelper.getAmmoCapacity(stack), ammoStored) : 0);
	                
	                Gun modifiedGun = gunItem.getModifiedGun(stack);
	                ResourceLocation id = modifiedGun.getProjectile().getItem();
	
	                Item item = ForgeRegistries.ITEMS.getValue(id);
	                if(item == null)
	                {
	                    return;
	                }
	                
	                if (!player.isCreative() || !partial)
	                {
		                int maxStackSize = item.getMaxStackSize();
		                int stacks = count / maxStackSize;
		                for(int i = 0; i < stacks; i++)
		                {
		                    spawnAmmo(player, new ItemStack(item, maxStackSize));
		                }
		
		                int remaining = count % maxStackSize;
		                if(remaining > 0)
		                {
		                    spawnAmmo(player, new ItemStack(item, remaining));
		                }
            		}
	            }
	            else
	            {
	            	if (partial)
                    {
                        tag.putInt("AmmoCount", Math.min(GunCompositeStatHelper.getAmmoCapacity(stack), ammoStored));
                    }
	            }
        	}
        }
    }

    private static void spawnAmmo(ServerPlayer player, ItemStack stack)
    {
        player.getInventory().add(stack);
        if(stack.getCount() > 0)
        {
            player.level.addFreshEntity(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), stack.copy()));
        }
    }

    public static void handleAttachments(ServerPlayer player)
    {
        ItemStack heldItem = player.getMainHandItem();
        if(heldItem.getItem() instanceof GunItem)
        {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((windowId, playerInventory, player1) -> new AttachmentContainer(windowId, playerInventory, heldItem), Component.translatable("container.cgm.attachments")));
        }
    }
}

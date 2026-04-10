package com.an0m3l1.guns.entity.grenade;

import com.an0m3l1.guns.Config;
import com.an0m3l1.guns.entity.ThrowableItemEntity;
import com.an0m3l1.guns.init.ModEntities;
import com.an0m3l1.guns.init.ModItems;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.S2CMessageImpactGrenade;
import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.an0m3l1.guns.entity.ProjectileEntity.createExplosion;

/**
 * Author: MrCrayfish
 */
public class ThrowableImpactGrenadeEntity extends ThrowableGrenadeEntity
{
	protected final float radius = Config.SERVER.impactGrenadeExplosionRadius.get().floatValue();
	protected final boolean griefing = Config.SERVER.impactGrenadeExplosionGriefing.get();
	
	public ThrowableImpactGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn)
	{
		super(entityType, worldIn);
	}
	
	public ThrowableImpactGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity entity)
	{
		super(entityType, world, entity);
		this.setItem(new ItemStack(ModItems.IMPACT_GRENADE_NO_PIN.get()));
		this.setShouldBounce(false);
	}
	
	public ThrowableImpactGrenadeEntity(Level world, LivingEntity entity, int timeLeft)
	{
		super(ModEntities.THROWABLE_IMPACT_GRENADE.get(), world, entity);
		this.setItem(new ItemStack(ModItems.IMPACT_GRENADE_NO_PIN.get()));
		this.setMaxLife(200);
		this.setShouldBounce(false);
	}
	
	@Override
	public void onDeath()
	{
		createExplosion(this, radius, griefing);
		double y = this.getY() + this.getType().getDimensions().height * 0.5;
		if(this.level.isClientSide)
		{
			return;
		}
		this.createLight(explosionLightValue, explosionLightLife);
		PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageImpactGrenade(this.getX(), y, this.getZ()));
	}
}

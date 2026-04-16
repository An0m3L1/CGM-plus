package com.an0m3l1.guns.entity.grenade;

import com.an0m3l1.guns.GunConfig;
import com.an0m3l1.guns.entity.LightSourceEntity;
import com.an0m3l1.guns.entity.ThrowableItemEntity;
import com.an0m3l1.guns.init.ModEntities;
import com.an0m3l1.guns.init.ModItems;
import com.an0m3l1.guns.init.ModSounds;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.S2CMessageGrenade;
import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static com.an0m3l1.guns.entity.ProjectileEntity.createExplosion;

/**
 * Author: MrCrayfish
 */
public class ThrowableGrenadeEntity extends ThrowableItemEntity
{
	public float rotation;
	public float prevRotation;
	protected final float radius = GunConfig.SERVER.grenadeExplosionRadius.get().floatValue();
	protected final boolean griefing = GunConfig.SERVER.grenadeExplosionGriefing.get();
	public final int explosionLightValue = 12;
	public final int explosionLightLife = 6;
	
	public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn)
	{
		super(entityType, worldIn);
		bounceSound = ModSounds.GRENADE_BOUNCE.get();
		useCustomBounceSound = true;
	}
	
	public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity entity)
	{
		super(entityType, world, entity);
		this.setShouldBounce(true);
		this.setGravityVelocity(0.05F);
		this.setItem(new ItemStack(ModItems.GRENADE_NO_PIN.get()));
		this.setMaxLife(20 * 3);
		bounceSound = ModSounds.GRENADE_BOUNCE.get();
		useCustomBounceSound = true;
	}
	
	public ThrowableGrenadeEntity(Level world, LivingEntity entity, int timeLeft)
	{
		super(ModEntities.THROWABLE_GRENADE.get(), world, entity);
		this.setShouldBounce(true);
		this.setGravityVelocity(0.05F);
		this.setItem(new ItemStack(ModItems.GRENADE_NO_PIN.get()));
		this.setMaxLife(timeLeft);
		bounceSound = ModSounds.GRENADE_BOUNCE.get();
		useCustomBounceSound = true;
	}
	
	@Override
	protected void defineSynchedData()
	{
	}
	
	@Override
	public void tick()
	{
		super.tick();
		this.prevRotation = this.rotation;
		double speed = this.getDeltaMovement().length();
		if(speed > 0.1)
		{
			this.rotation += (float) (speed * 50);
		}
		if(this.level.isClientSide)
		{
			this.level.addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
		}
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
		this.createLight(this.explosionLightValue, this.explosionLightLife);
		PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageGrenade(this.getX(), y, this.getZ()));
	}
	
	public void createLight(int lightValue, int lightLife)
	{
		LightSourceEntity lightSource = new LightSourceEntity(level, this.getX(), this.getY(), this.getZ(), lightValue, lightLife);
		level.addFreshEntity(lightSource);
	}
	
	public static void igniteEntities(Level level, Vec3 center, float radius, int fireDuration)
	{
		int minX = Mth.floor(center.x - radius);
		int maxX = Mth.floor(center.x + radius);
		int minY = Mth.floor(center.y - radius);
		int maxY = Mth.floor(center.y + radius);
		int minZ = Mth.floor(center.z - radius);
		int maxZ = Mth.floor(center.z + radius);
		
		double radiusSq = radius * radius;
		
		for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(minX, minY, minZ, maxX, maxY, maxZ)))
		{
			if(entity.ignoreExplosion())
			{
				continue;
			}
			
			Vec3 entityPos = entity.getBoundingBox().getCenter();
			double distanceSq = center.distanceToSqr(entityPos);
			
			if(distanceSq > radiusSq)
			{
				continue;
			}
			
			ClipContext context = new ClipContext(center, entityPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
			
			if(level.clip(context).getType() == HitResult.Type.MISS)
			{
				entity.setSecondsOnFire(fireDuration);
			}
		}
	}
}

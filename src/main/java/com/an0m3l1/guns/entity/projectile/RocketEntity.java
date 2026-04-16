package com.an0m3l1.guns.entity.projectile;

import com.an0m3l1.guns.GunConfig;
import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.common.Gun;
import com.an0m3l1.guns.entity.LightSourceEntity;
import com.an0m3l1.guns.entity.ProjectileEntity;
import com.an0m3l1.guns.init.ModTags;
import com.an0m3l1.guns.item.GunItem;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.S2CMessageRocket;
import com.mrcrayfish.framework.api.network.LevelLocation;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class RocketEntity extends ProjectileEntity
{
	public RocketEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn)
	{
		super(entityType, worldIn);
		if(GunMod.dynamicLightsLoaded && GunConfig.COMMON.enableDynamicLights.get())
		{
			DynamicLightHandlers.registerDynamicLightHandler(entityType, entity -> 7);
		}
	}
	
	public RocketEntity(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
	{
		super(entityType, worldIn, shooter, weapon, item, modifiedGun);
	}
	
	@Override
	protected void onProjectileTick()
	{
		if(this.level.isClientSide)
		{
			for(int i = 5; i > 0; i--)
			{
				this.level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
			}
			if(this.level.random.nextInt(2) == 0)
			{
				this.level.addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
				this.level.addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
			}
		}
	}
	
	@Override
	protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot)
	{
		explode();
	}
	
	@Override
	protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
	{
		/* If the projectile hit a fragile block, check if projectile griefing is enabled globally, for the projectile itself and explosion griefing is disabled.
		 * We check for explosion griefing so the projectile doesn't just disappear */
		if(this.getProjectile() != null && state.is(ModTags.Blocks.HARDNESS_NONE))
		{
			if(!GunConfig.COMMON.universalExplosionGriefing.get() && GunConfig.COMMON.projectileGriefing.get() && this.getProjectile().isGriefing())
			{
				this.level.destroyBlock(pos, GunConfig.COMMON.projectileGriefingBlockDrops.get());
			}
			else
			{
				explode();
			}
		}
		else
		{
			explode();
		}
	}
	
	@Override
	public void onExpired()
	{
		explode();
	}
	
	private void explode()
	{
		float radius = 5.0F;
		boolean griefing = true;
		
		if(this.getProjectile() != null)
		{
			radius = this.getProjectile().getExplosionRadius();
			griefing = this.getProjectile().isGriefing();
		}
		
		createExplosion(this, radius, griefing);
		if(this.level.isClientSide)
		{
			return;
		}
		
		LightSourceEntity light = new LightSourceEntity(level, this.getX(), this.getY(), this.getZ(), explosionLightValue, explosionLightLife);
		level.addFreshEntity(light);
		PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256), new S2CMessageRocket(this.getX(), this.getY(), this.getZ(), radius));
	}
}
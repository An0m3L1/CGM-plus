package com.an0m3l1.guns.entity.grenade;

import com.an0m3l1.guns.GunConfig;
import com.an0m3l1.guns.GunConfig.EffectCriteria;
import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.init.ModEffects;
import com.an0m3l1.guns.init.ModEntities;
import com.an0m3l1.guns.init.ModItems;
import com.an0m3l1.guns.init.ModSounds;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.S2CMessageStunGrenade;
import com.mrcrayfish.framework.api.network.LevelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ThrowableStunGrenadeEntity extends ThrowableGrenadeEntity
{
	public ThrowableStunGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world)
	{
		super(entityType, world);
		bounceSound = ModSounds.STUN_BOUNCE.get();
		useCustomBounceSound = true;
	}
	
	public ThrowableStunGrenadeEntity(EntityType<? extends ThrowableGrenadeEntity> entityType, Level world, LivingEntity player)
	{
		super(entityType, world, player);
		this.setItem(new ItemStack(ModItems.STUN_GRENADE_NO_PIN.get()));
		bounceSound = ModSounds.STUN_BOUNCE.get();
		useCustomBounceSound = true;
	}
	
	public ThrowableStunGrenadeEntity(Level world, LivingEntity player, int maxCookTime)
	{
		super(ModEntities.THROWABLE_STUN_GRENADE.get(), world, player);
		this.setItem(new ItemStack(ModItems.STUN_GRENADE_NO_PIN.get()));
		this.setMaxLife(maxCookTime);
		bounceSound = ModSounds.STUN_BOUNCE.get();
		useCustomBounceSound = true;
	}
	
	@SubscribeEvent
	public static void blindMobs(LivingSetAttackTargetEvent event)
	{
		if(GunConfig.COMMON.blindMobs.get() && event.getTarget() != null && event.getEntity() instanceof Mob && event.getEntity().hasEffect(ModEffects.BLINDED.get()))
		{
			((Mob) event.getEntity()).setTarget(null);
		}
	}
	
	@Override
	public void onDeath()
	{
		double y = this.getY() + this.getType().getDimensions().height * 0.5;
		if(this.level.isClientSide)
		{
			return;
		}
		this.createLight(explosionLightValue, explosionLightLife);
		PacketHandler.getPlayChannel().sendToNearbyPlayers(() -> LevelLocation.create(this.level, this.getX(), y, this.getZ(), 256), new S2CMessageStunGrenade(this.getX(), y, this.getZ()));
		
		// Calculate bounds of area where potentially effected players may be
		double diameter = Math.max(GunConfig.SERVER.stunCriteria.radius.get(), GunConfig.SERVER.blindCriteria.radius.get()) * 2 + 1;
		int minX = Mth.floor(this.getX() - diameter);
		int maxX = Mth.floor(this.getX() + diameter);
		int minY = Mth.floor(y - diameter);
		int maxY = Mth.floor(y + diameter);
		int minZ = Mth.floor(this.getZ() - diameter);
		int maxZ = Mth.floor(this.getZ() + diameter);
		
		// Affect all non-spectating players in range of the blast
		Vec3 grenade = new Vec3(this.getX(), y, this.getZ());
		Vec3 eyes, directionGrenade;
		double distance;
		for(LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class, new AABB(minX, minY, minZ, maxX, maxY, maxZ)))
		{
			if(entity.ignoreExplosion())
			{
				continue;
			}
			
			eyes = entity.getEyePosition(1.0F);
			directionGrenade = grenade.subtract(eyes);
			distance = directionGrenade.length();
			
			// Calculate angle between eye-gaze line and eye-grenade line
			double angle = Math.toDegrees(Math.acos(entity.getViewVector(1.0F).dot(directionGrenade.normalize())));
			
			// Apply effects as determined by their criteria
			if(this.calculateAndApplyEffect(ModEffects.STUNNED.get(), GunConfig.SERVER.stunCriteria, entity, grenade, eyes, distance, angle) && GunConfig.COMMON.panicMobs.get())
			{
				entity.setLastHurtByMob(entity);
			}
			if(this.calculateAndApplyEffect(ModEffects.BLINDED.get(), GunConfig.SERVER.blindCriteria, entity, grenade, eyes, distance, angle) && GunConfig.COMMON.blindMobs.get() && entity instanceof Mob)
			{
				((Mob) entity).setTarget(null);
			}
		}
	}
	
	private boolean calculateAndApplyEffect(MobEffect effect, EffectCriteria criteria, LivingEntity entity, Vec3 grenade, Vec3 eyes, double distance, double angle)
	{
		double angleMax = criteria.angleEffect.get() * 0.5;
		boolean debug = GunConfig.COMMON.showDebugMessages.get();
		
		if(debug)
		{
			String entityName = entity.getName().getString();
			String effectName = effect.getDisplayName().getString();
			GunMod.LOGGER.debug("[StunGrenade] Checking effect {} for {}", effectName, entityName);
			GunMod.LOGGER.debug("[StunGrenade]   Distance: {} (radius: {})", String.format("%.2f", distance), criteria.radius.get());
			GunMod.LOGGER.debug("[StunGrenade]   Angle: {} (max angle: {})", String.format("%.2f", angle), angleMax);
		}
		
		if(distance <= criteria.radius.get() && angleMax > 0 && angle <= angleMax)
		{
			if(debug)
			{
				GunMod.LOGGER.debug("[StunGrenade]   Entity passed distance and angle checks.");
			}
			
			if(effect == ModEffects.BLINDED.get())
			{
				HitResult hit = rayTraceOpaqueBlocks(this.level, eyes, grenade, false, false, false);
				if(hit != null)
				{
					if(debug)
					{
						GunMod.LOGGER.debug("[StunGrenade]   Ray blocked by: {} at {}", hit.getType(), hit.getLocation());
						GunMod.LOGGER.debug("[StunGrenade]   Effect NOT applied due to obstruction.");
					}
					return false;
				}
				else
				{
					if(debug)
					{
						GunMod.LOGGER.debug("[StunGrenade]   Line of sight is clear.");
					}
				}
			}
			
			int duration = (int) Math.round(criteria.durationMax.get() * 20 - (criteria.durationMax.get() * 20 - criteria.durationMin.get() * 20) * (distance / criteria.radius.get()));
			if(debug)
			{
				GunMod.LOGGER.debug("[StunGrenade]   Base duration (distance-attenuated): {} ticks ({} sec)", duration, String.format("%.2f", duration / 20.0));
			}
			
			double angleFactor = 1.0 - (angle * (1.0 - criteria.angleAttenuationMax.get())) / angleMax;
			duration = (int) Math.round(duration * angleFactor);
			if(debug)
			{
				GunMod.LOGGER.debug("[StunGrenade]   Duration after angle correction: {} ticks ({} sec)", duration, String.format("%.2f", duration / 20.0));
			}
			
			entity.addEffect(new MobEffectInstance(effect, duration, 0, false, false));
			if(debug)
			{
				GunMod.LOGGER.debug("[StunGrenade]   Effect {} successfully applied to {}", effect.getDisplayName().getString(), entity.getName().getString());
			}
			
			boolean isMob = !(entity instanceof Player);
			if(debug)
			{
				GunMod.LOGGER.debug("[StunGrenade]   Return value (for extra actions): {}", isMob);
			}
			return isMob;
		}
		else
		{
			if(debug)
			{
				if(distance > criteria.radius.get())
				{
					GunMod.LOGGER.debug("[StunGrenade]   Failure reason: distance exceeds radius ({} > {})", distance, criteria.radius.get());
				}
				else if(angleMax <= 0)
				{
					GunMod.LOGGER.debug("[StunGrenade]   Failure reason: max angle is zero");
				}
				else if(angle > angleMax)
				{
					GunMod.LOGGER.debug("[StunGrenade]   Failure reason: angle exceeds max ({} > {})", angle, angleMax);
				}
				GunMod.LOGGER.debug("[StunGrenade]   Effect NOT applied.");
			}
			return false;
		}
	}
	
	@Nullable
	public HitResult rayTraceOpaqueBlocks(Level world, Vec3 start, Vec3 end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
	{
		if(!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z))
		{
			if(!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z))
			{
				int endX = Mth.floor(end.x);
				int endY = Mth.floor(end.y);
				int endZ = Mth.floor(end.z);
				int startX = Mth.floor(start.x);
				int startY = Mth.floor(start.y);
				int startZ = Mth.floor(start.z);
				BlockPos pos = new BlockPos(startX, startY, startZ);
				BlockState stateInside = world.getBlockState(pos);
				
				// Added light opacity check
				if(stateInside.getLightBlock(world, pos) != 0 && (!ignoreBlockWithoutBoundingBox || stateInside.getCollisionShape(world, pos) != Shapes.empty()))
				{
					return world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
				}
				
				HitResult raytraceresult2 = null;
				int limit = 200;
				while(limit-- >= 0)
				{
					if(Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z))
					{
						return null;
					}
					
					if(startX == endX && startY == endY && startZ == endZ)
					{
						return null;
					}
					
					boolean completedX = true;
					boolean completedY = true;
					boolean completedZ = true;
					double d0 = 999;
					double d1 = 999;
					double d2 = 999;
					
					if(endX > startX)
					{
						d0 = startX + 1;
					}
					else if(endX < startX)
					{
						d0 = startX;
					}
					else
					{
						completedX = false;
					}
					
					if(endY > startY)
					{
						d1 = startY + 1;
					}
					else if(endY < startY)
					{
						d1 = startY;
					}
					else
					{
						completedY = false;
					}
					
					if(endZ > startZ)
					{
						d2 = startZ + 1;
					}
					else if(endZ < startZ)
					{
						d2 = startZ;
					}
					else
					{
						completedZ = false;
					}
					
					double d3 = 999;
					double d4 = 999;
					double d5 = 999;
					double d6 = end.x - start.x;
					double d7 = end.y - start.y;
					double d8 = end.z - start.z;
					
					if(completedX)
					{
						d3 = (d0 - start.x) / d6;
					}
					
					if(completedY)
					{
						d4 = (d1 - start.y) / d7;
					}
					
					if(completedZ)
					{
						d5 = (d2 - start.z) / d8;
					}
					
					if(d3 == -0)
					{
						d3 = -1.0E-4D;
					}
					
					if(d4 == -0)
					{
						d4 = -1.0E-4D;
					}
					
					if(d5 == -0)
					{
						d5 = -1.0E-4D;
					}
					
					Direction direction;
					
					if(d3 < d4 && d3 < d5)
					{
						direction = endX > startX ? Direction.WEST : Direction.EAST;
						start = new Vec3(d0, start.y + d7 * d3, start.z + d8 * d3);
					}
					else if(d4 < d5)
					{
						direction = endY > startY ? Direction.DOWN : Direction.UP;
						start = new Vec3(start.x + d6 * d4, d1, start.z + d8 * d4);
					}
					else
					{
						direction = endZ > startZ ? Direction.NORTH : Direction.SOUTH;
						start = new Vec3(start.x + d6 * d5, start.y + d7 * d5, d2);
					}
					
					startX = Mth.floor(start.x) - (direction == Direction.EAST ? 1 : 0);
					startY = Mth.floor(start.y) - (direction == Direction.UP ? 1 : 0);
					startZ = Mth.floor(start.z) - (direction == Direction.SOUTH ? 1 : 0);
					pos = new BlockPos(startX, startY, startZ);
					BlockState state = world.getBlockState(pos);
					
					// Added light opacity check
					if(state.getLightBlock(world, pos) != 0 && (!ignoreBlockWithoutBoundingBox || state.getMaterial() == Material.PORTAL || state.getCollisionShape(world, pos) != Shapes.empty()))
					{
						return world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
					}
				}
				return null;
			}
			return null;
		}
		return null;
	}
}
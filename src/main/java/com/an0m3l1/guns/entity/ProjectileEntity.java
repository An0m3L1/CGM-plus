package com.an0m3l1.guns.entity;

import com.an0m3l1.guns.Config;
import com.an0m3l1.guns.GunMod;
import com.an0m3l1.guns.common.BoundingBoxManager;
import com.an0m3l1.guns.common.Gun;
import com.an0m3l1.guns.common.Gun.Projectile;
import com.an0m3l1.guns.common.ServerAimTracker;
import com.an0m3l1.guns.common.SpreadTracker;
import com.an0m3l1.guns.entity.grenade.ThrowableGrenadeEntity;
import com.an0m3l1.guns.entity.grenade.ThrowableImpactGrenadeEntity;
import com.an0m3l1.guns.entity.projectile.BulletEntity;
import com.an0m3l1.guns.event.GunProjectileHitEvent;
import com.an0m3l1.guns.init.ModSounds;
import com.an0m3l1.guns.init.ModSyncedDataKeys;
import com.an0m3l1.guns.init.ModTags;
import com.an0m3l1.guns.interfaces.IDamageable;
import com.an0m3l1.guns.interfaces.IHeadshotBox;
import com.an0m3l1.guns.item.GunItem;
import com.an0m3l1.guns.network.PacketHandler;
import com.an0m3l1.guns.network.message.S2CMessageBlood;
import com.an0m3l1.guns.network.message.S2CMessageProjectileHitBlock;
import com.an0m3l1.guns.network.message.S2CMessageProjectileHitEntity;
import com.an0m3l1.guns.network.message.S2CMessageRemoveProjectile;
import com.an0m3l1.guns.util.ExtendedEntityRayTraceResult;
import com.an0m3l1.guns.util.GunCompositeStatHelper;
import com.an0m3l1.guns.util.GunModifierHelper;
import com.an0m3l1.guns.world.ProjectileExplosion;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.mrcrayfish.framework.api.network.LevelLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.an0m3l1.guns.init.ModTags.Entities.IMMUNE;
import static com.an0m3l1.guns.init.ModTags.Entities.RESISTANT;

public class ProjectileEntity extends Entity implements IEntityAdditionalSpawnData
{
	private static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();
	private static final Predicate<BlockState> IGNORE_NONE = state -> false;
	private static final Method updateRedstoneOutputMethod = ObfuscationReflectionHelper.findMethod(TargetBlock.class, "m_57391_", LevelAccessor.class, BlockState.class, BlockHitResult.class, Entity.class);
	
	protected int shooterId;
	protected LivingEntity shooter;
	protected Gun modifiedGun;
	protected Gun.General general;
	protected Gun.Projectile projectile;
	private ItemStack weapon = ItemStack.EMPTY;
	private ItemStack item = ItemStack.EMPTY;
	protected float additionalDamage = 0.0F;
	protected int pierceCounter = 0;
	protected int maxPierceCount;
	protected float pierceDamageFraction = 1.0F;
	protected EntityDimensions entitySize;
	protected double modifiedGravity;
	protected int life;
	protected int soundTime = 0;
	protected boolean deadProjectile = false;
	protected float pitch = 0.9F + level.random.nextFloat() * 0.2F;
	protected boolean infinitePiercing = false;
	public float rotation;
	public float prevRotation;
	public float waterDamagePenalty;
	public final int explosionLightValue = 12;
	public final int explosionLightLife = 6;
	protected int ticksToSkip = 0;
	protected final Set<UUID> hitEntities = new HashSet<>();
	
	public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn)
	{
		super(entityType, worldIn);
	}
	
	public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
	{
		this(entityType, worldIn);
		this.shooterId = shooter.getId();
		this.shooter = shooter;
		this.modifiedGun = modifiedGun;
		this.general = modifiedGun.getGeneral();
		this.projectile = modifiedGun.getProjectile();
		this.maxPierceCount = this.projectile.getMaxPierceCount();
		this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
		this.modifiedGravity = modifiedGun.getProjectile().isGravity() ? GunModifierHelper.getModifiedProjectileGravity(weapon, -0.04 * modifiedGun.getProjectile().getGravity()) : 0.0;
		this.life = GunModifierHelper.getModifiedProjectileLife(weapon, this.projectile.getLife());
		this.waterDamagePenalty = 1.0F - projectile.getWaterDamagePenalty();
		this.infinitePiercing = this.maxPierceCount == -1;
		
		// Get speed and set motion
		Vec3 dir = this.getDirection(shooter, weapon, item, modifiedGun);
		double speed = GunModifierHelper.getModifiedProjectileSpeed(weapon, this.projectile.getSpeed());
		this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
		this.updateHeading();
		
		// Spawn the projectile halfway between the previous and current position
		double posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
		double posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
		double posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
		this.setPos(posX, posY, posZ);
		
		// Render projectile model
		Item ammo = ForgeRegistries.ITEMS.getValue(this.projectile.getItem());
		if(ammo != null)
		{
			int customModelData = -1;
			if(weapon.getTag() != null)
			{
				if(weapon.getTag().contains("Model", Tag.TAG_COMPOUND))
				{
					ItemStack model = ItemStack.of(weapon.getTag().getCompound("Model"));
					if(model.getTag() != null && model.getTag().contains("CustomModelData"))
					{
						customModelData = model.getTag().getInt("CustomModelData");
					}
				}
			}
			ItemStack ammoStack = new ItemStack(ammo);
			if(customModelData != -1)
			{
				ammoStack.getOrCreateTag().putInt("CustomModelData", customModelData);
			}
			this.item = ammoStack;
		}
	}
	
	@Override
	protected void defineSynchedData()
	{
	}
	
	@Override
	public @NotNull EntityDimensions getDimensions(@NotNull Pose pose)
	{
		return this.entitySize;
	}
	
	private Vec3 getDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
	{
		float gunSpread = GunCompositeStatHelper.getCompositeSpread(weapon, modifiedGun);
		float minSpread = GunCompositeStatHelper.getCompositeMinSpread(weapon, modifiedGun);
		
		// For 0 spread we skip all the calculations and just shoot where the shooter looks
		if(gunSpread == 0F)
		{
			return this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
		}
		
		if(shooter instanceof Player)
		{
			float initialGunSpread = Mth.lerp(SpreadTracker.get((Player) shooter).getSpread((Player) shooter, item), minSpread, gunSpread);
			
			if(!this.general.getAlwaysSpread() || minSpread > 0)
			{
				gunSpread = initialGunSpread;
			}
			
			if(ModSyncedDataKeys.AIMING.getValue((Player) shooter))
			{
				float aimPosition = (float) Mth.clamp(ServerAimTracker.getAimingTicks((Player) shooter) / (5 / GunCompositeStatHelper.getCompositeAimDownSightSpeed(weapon)), 0, 1);
				
				if(modifiedGun.getGeneral().getUseSniperSpread())
				{
					// For sniper spread reduce only the minSpread value
					float aimingMinSpread = minSpread * (1.0F - this.general.getSpreadAdsReduction());
					float aimingInitialGunSpread = Mth.lerp(SpreadTracker.get((Player) shooter).getSpread((Player) shooter, item), aimingMinSpread, gunSpread);
					gunSpread = Mth.lerp(aimPosition, initialGunSpread, aimingInitialGunSpread);
				}
				else
				{
					float aimingGunSpread = gunSpread * (1.0F - this.general.getSpreadAdsReduction());
					gunSpread = Mth.lerp(aimPosition, initialGunSpread, aimingGunSpread);
				}
			}
		}
		
		// New spread vector code provided by Poly-1810 and used with permission.
		// This fix was figured out by unze2unze4 and implemented by Poly into their CGM Refined fork.
		// Big thanks to both of them for this fix!
		gunSpread = Math.min(gunSpread, 170F) * 0.5F * Mth.DEG_TO_RAD;
		
		Vec3 vecForwards = this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
		Vec3 vecUpwards = this.getVectorFromRotation(shooter.getXRot() + 90F, shooter.getYRot());
		Vec3 vecSideways = vecForwards.cross(vecUpwards);
		
		float theta = random.nextFloat() * 2F * (float) Math.PI;
		float r = Mth.sqrt(random.nextFloat()) * (float) Math.tan(gunSpread);
		
		float a1 = Mth.cos(theta) * r;
		float a2 = Mth.sin(theta) * r;
		
		return vecForwards.add(vecSideways.scale(a1)).add(vecUpwards.scale(a2)).normalize();
	}
	
	public void setWeapon(ItemStack weapon)
	{
		this.weapon = weapon.copy();
	}
	
	public ItemStack getWeapon()
	{
		return this.weapon;
	}
	
	public void setItem(ItemStack item)
	{
		this.item = item;
	}
	
	public ItemStack getItem()
	{
		return this.item;
	}
	
	public void setAdditionalDamage(float additionalDamage)
	{
		this.additionalDamage = additionalDamage;
	}
	
	public double getModifiedGravity()
	{
		return this.modifiedGravity;
	}
	
	@Override
	public void tick()
	{
		super.tick();
		this.updateHeading();
		this.onProjectileTick();
		
		// Spin projectile around itself
		this.prevRotation = this.rotation;
		double speed = this.getDeltaMovement().length();
		if(speed > 0.1)
		{
			this.rotation += (float) (speed * 50);
		}
		
		// Spawn bubbles in water
		Vec3 vec3 = this.getDeltaMovement();
		if(this.isInWater())
		{
			for(int j = 0; j < 4; ++j)
			{
				this.level.addParticle(ParticleTypes.BUBBLE, this.getX() - vec3.x * 0.1D, this.getY() - vec3.y * 0.1D, this.getZ() - vec3.z * 0.1D, vec3.x, vec3.y, vec3.z);
			}
		}
		
		// Perform all logic on server
		if(!this.level.isClientSide())
		{
			Vec3 startVec = this.position();
			Vec3 endVec = startVec.add(this.getDeltaMovement());
			HitResult result = rayTraceBlocks(this.level, new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_NONE);
			
			// Projectile flyby sound
			boolean isBullet = this instanceof BulletEntity;
			if(isBullet)
			{
				AABB range = new AABB(startVec.x - 5, startVec.y - 5, startVec.z - 5, startVec.x + 5, startVec.y + 5, startVec.z + 5);
				List<Player> players = this.level.getEntitiesOfClass(Player.class, range);
				
				// Don't play flyby sound for the projectile shooter
				if(this.shooter instanceof Player)
				{
					players.removeIf(player -> player.equals(this.shooter));
				}
				
				float volume = 0.5F + this.level.getRandom().nextFloat() * 0.4F;
				boolean isMultishot = general.getProjectileAmount() > 1;
				if(!players.isEmpty() && this.tickCount > 3 && soundTime < this.tickCount - 3)
				{
					// Divide volume by projectile amount
					if(isMultishot)
					{
						volume = volume / general.getProjectileAmount();
					}
					this.level.playSound(null, startVec.x, startVec.y, startVec.z, ModSounds.FLYBY.get(), SoundSource.NEUTRAL, volume, 0.8F + this.level.getRandom().nextFloat() * 0.4F);
					this.soundTime = this.tickCount;
				}
			}
			
			if(result.getType() != HitResult.Type.MISS)
			{
				endVec = result.getLocation();
			}
			
			List<EntityResult> hitEntities = null;
			boolean skipMovement = false;
			
			// Destroy projectile after projectile has run out of pierces
			if(this.maxPierceCount == 0)
			{
				EntityResult entityResult = this.findEntityOnPath(startVec, endVec);
				if(entityResult != null)
				{
					hitEntities = Collections.singletonList(entityResult);
				}
			}
			// Find all entities that will be affected by the projectile
			else
			{
				hitEntities = this.findEntitiesOnPath(startVec, endVec);
			}
			
			// Check if we found any entities on the way of projectile
			if(hitEntities != null && !hitEntities.isEmpty())
			{
				for(EntityResult entityResult : hitEntities)
				{
					result = new ExtendedEntityRayTraceResult(entityResult);
					if(((EntityHitResult) result).getEntity() instanceof Player player)
					{
						// If we hit ourselves or a player that is immune to our attacks do nothing
						if(this.shooter instanceof Player && !((Player) this.shooter).canHarmPlayer(player))
						{
							result = null;
						}
					}
					if(result != null)
					{
						// If we hit anything, freeze projectile movement for 1 tick
						if(this.onHit(result, startVec, endVec))
						{
							skipMovement = true;
							break;
						}
					}
				}
			}
			// If we hit anything, freeze projectile movement for 1 tick
			else
			{
				if(this.onHit(result, startVec, endVec))
				{
					skipMovement = true;
				}
			}
			
			// If the projectile didn't hit anything, move it
			if(!skipMovement)
			{
				this.moveProjectile();
			}
			// If the projectile did hit something, add 1 tick to its lifetime to account for skipped ticks
			else
			{
				this.ticksToSkip++;
			}
		}
		// Move the projectile on client
		else
		{
			this.moveProjectile();
		}
		
		// Apply gravity to projectile
		if(this.projectile.isGravity())
		{
			this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
		}
		
		// Take into account skipped ticks and destroy projectile after it expires
		int effectiveTickCount = this.tickCount - this.ticksToSkip;
		if(effectiveTickCount >= this.life)
		{
			if(this.isAlive())
			{
				this.onExpired();
			}
			this.remove(RemovalReason.KILLED);
		}
	}
	
	private void moveProjectile()
	{
		double nextX = getX() + getDeltaMovement().x();
		double nextY = getY() + getDeltaMovement().y();
		double nextZ = getZ() + getDeltaMovement().z();
		setPos(nextX, nextY, nextZ);
	}
	
	/**
	 * A simple method to perform logic on each tick of the projectile. This method is appropriate
	 * for spawning particles. Override {@link #tick()} to make changes to physics
	 */
	protected void onProjectileTick()
	{
	}
	
	/**
	 * Called when the projectile has run out of its life. In other words, the projectile managed
	 * to not hit any blocks and instead aged. The grenade uses this to explode in the air.
	 */
	protected void onExpired()
	{
	}
	
	@Nullable
	protected EntityResult findEntityOnPath(Vec3 startVec, Vec3 endVec)
	{
		Vec3 hitVec = null;
		Entity hitEntity = null;
		boolean headshot = false;
		List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0 + this.projectile.getSize()), PROJECTILE_TARGETS);
		double closestDistance = Double.MAX_VALUE;
		for(Entity entity : entities)
		{
			boolean isDead = (entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying());
			boolean isImmune = Config.COMMON.enableImmuneEntities.get() && entity.getType().is(IMMUNE);
			if(!entity.equals(this.shooter) && !isImmune && !this.hitEntities.contains(entity.getUUID()))
			{
				EntityResult result = this.getHitResult(entity, startVec, endVec);
				if(result == null || isDead)
				{
					continue;
				}
				Vec3 hitPos = result.getHitPos();
				double distanceToHit = startVec.distanceTo(hitPos);
				if(distanceToHit < closestDistance)
				{
					hitVec = hitPos;
					hitEntity = entity;
					closestDistance = distanceToHit;
					headshot = result.isHeadshot();
				}
			}
		}
		return hitEntity != null ? new EntityResult(hitEntity, startVec, hitVec, headshot) : null;
	}
	
	@Nullable
	protected List<EntityResult> findEntitiesOnPath(Vec3 startVec, Vec3 endVec)
	{
		List<EntityResult> hitEntities = new ArrayList<>();
		List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0 + this.projectile.getSize()), PROJECTILE_TARGETS);
		for(Entity entity : entities)
		{
			boolean isDead = (entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying());
			boolean isImmune = Config.COMMON.enableImmuneEntities.get() && entity.getType().is(IMMUNE);
			if(this.hitEntities.contains(entity.getUUID()))
			{
				continue;
			}
			if(!entity.equals(this.shooter) && !isImmune)
			{
				EntityResult result = this.getHitResult(entity, startVec, endVec);
				// Ignore dead entities so they don't eat up our piercing
				if(result == null || isDead)
				{
					continue;
				}
				hitEntities.add(result);
			}
		}
		hitEntities.sort(new HitComparator());
		return hitEntities;
	}
	
	@Nullable
	@SuppressWarnings("unchecked")
	private EntityResult getHitResult(Entity entity, Vec3 startVec, Vec3 endVec)
	{
		double expandHeight = entity instanceof Player && !entity.isCrouching() ? 0.0625 : 0.0;
		AABB boundingBox = entity.getBoundingBox();
		if(Config.COMMON.improvedHitboxes.get() && entity instanceof ServerPlayer && this.shooter != null)
		{
			int ping = (int) Math.floor((((ServerPlayer) this.shooter).latency / 1000.0) * 20.0 + 0.5);
			boundingBox = BoundingBoxManager.getBoundingBox((Player) entity, ping);
		}
		boundingBox = boundingBox.expandTowards(0, expandHeight, 0);
		
		Vec3 hitPos = boundingBox.clip(startVec, endVec).orElse(null);
		Vec3 grownHitPos = boundingBox.inflate(Config.COMMON.growBoundingBoxAmount.get(), 0, Config.COMMON.growBoundingBoxAmount.get()).clip(startVec, endVec).orElse(null);
		if(hitPos == null && grownHitPos != null)
		{
			HitResult raytraceresult = rayTraceBlocks(this.level, new ClipContext(startVec, grownHitPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_NONE);
			if(raytraceresult.getType() == HitResult.Type.BLOCK)
			{
				return null;
			}
			hitPos = grownHitPos;
		}
		
		// Check for headshot
		boolean headshot = false;
		if(Config.COMMON.enableHeadShots.get() && entity instanceof LivingEntity)
		{
			IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());
			if(headshotBox != null)
			{
				AABB box = headshotBox.getHeadshotBox((LivingEntity) entity);
				if(box != null)
				{
					box = box.move(boundingBox.getCenter().x, boundingBox.minY, boundingBox.getCenter().z);
					Optional<Vec3> headshotHitPos = box.clip(startVec, endVec);
					if(headshotHitPos.isEmpty())
					{
						box = box.inflate(Config.COMMON.growBoundingBoxAmount.get(), 0, Config.COMMON.growBoundingBoxAmount.get());
						headshotHitPos = box.clip(startVec, endVec);
					}
					if(headshotHitPos.isPresent() && (hitPos == null || headshotHitPos.get().distanceTo(hitPos) < 0.5))
					{
						hitPos = headshotHitPos.get();
						headshot = true;
					}
				}
			}
		}
		
		if(hitPos == null)
		{
			return null;
		}
		
		return new EntityResult(entity, startVec, hitPos, headshot);
	}
	
	/**
	 * Called when the projectile hits something.
	 *
	 * @param result
	 * 		The hit result
	 * @param startVec
	 * 		The start position of the ray trace
	 * @param endVec
	 * 		The end position of the ray trace (position may be updated to hit location)
	 *
	 * @return true if the movement in this tick should be skipped (e.g., after destroying a fragile block)
	 */
	private boolean onHit(HitResult result, Vec3 startVec, Vec3 endVec)
	{
		if(MinecraftForge.EVENT_BUS.post(new GunProjectileHitEvent(result, this)))
		{
			return false;
		}
		
		if(result instanceof BlockHitResult blockHitResult)
		{
			if(blockHitResult.getType() == HitResult.Type.MISS)
			{
				return false;
			}
			
			Vec3 hitVec = result.getLocation();
			BlockPos pos = blockHitResult.getBlockPos();
			BlockState state = this.level.getBlockState(pos);
			Block block = state.getBlock();
			
			if(block instanceof IDamageable)
			{
				((IDamageable) block).onBlockDamaged(this.level, state, pos, this, this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
			}
			
			this.onHitBlock(state, pos, blockHitResult.getDirection(), hitVec.x, hitVec.y, hitVec.z);
			this.level.gameEvent(GameEvent.PROJECTILE_LAND, pos, GameEvent.Context.of(this));
			
			// Check if the projectile should grief
			boolean projectileIsGriefing = Config.COMMON.projectileGriefing.get() && this.modifiedGun.getProjectile().isGriefing();
			
			// Handle target blocks
			if(block instanceof TargetBlock targetBlock)
			{
				int power = updateTargetBlock(targetBlock, this.level, state, blockHitResult, this);
				if(this.shooter instanceof ServerPlayer serverPlayer)
				{
					serverPlayer.awardStat(Stats.TARGET_HIT);
					CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, this, blockHitResult.getLocation(), power);
				}
			}
			
			// Ring bell blocks
			if(block instanceof BellBlock bell)
			{
				bell.attemptToRing(this.level, pos, blockHitResult.getDirection());
			}
			
			// Ignite TNT blocks and kill the projectile
			if(block instanceof TntBlock tnt && projectileIsGriefing)
			{
				tnt.onCaughtFire(state, this.level, pos, blockHitResult.getDirection(), this.shooter);
				this.level.removeBlock(pos, false);
				this.remove(RemovalReason.KILLED);
				this.deadProjectile = true;
			}
			
			// Check if projectile can destroy hit block
			boolean blockCanBeDestroyed = false;
			if(this.modifiedGun != null)
			{
				if(projectileIsGriefing && state.is(ModTags.Blocks.DESTRUCTIBLE))
				{
					blockCanBeDestroyed = true;
				}
			}
			
			// Handle undestructible blocks
			if(!blockCanBeDestroyed)
			{
				// Check if hit block isn't grass, crop, etc. and kill the projectile
				if(!state.getMaterial().isReplaceable())
				{
					this.remove(RemovalReason.KILLED);
					this.deadProjectile = true;
				}
				return false;
			}
			// Handle destructible blocks
			else
			{
				// Destroy fragile blocks for free
				if(state.is(ModTags.Blocks.HARDNESS_NONE))
				{
					this.level.destroyBlock(pos, Config.COMMON.projectileGriefingBlockDrops.get());
					BlockDamageManager.removeDamage(this.level, pos);
					this.setPos(hitVec.x, hitVec.y, hitVec.z);
					return true;
				}
				
				// Calculate block hardness
				float blockRawHardness = 0.0F;
				boolean blockIsTree = GunMod.dynamicTreesLoaded && TreeHelper.isTreePart(state);
				if(blockIsTree)
				{
					blockRawHardness = state.getDestroySpeed(this.level, pos) * 0.25F;
				}
				else
				{
					if(state.is(ModTags.Blocks.HARDNESS_LOW))
					{
						blockRawHardness = Config.COMMON.hardnessLowValue.get();
					}
					else if(state.is(ModTags.Blocks.HARDNESS_MEDIUM))
					{
						blockRawHardness = Config.COMMON.hardnessMediumValue.get();
					}
					else if(state.is(ModTags.Blocks.HARDNESS_HIGH))
					{
						blockRawHardness = Config.COMMON.hardnessHighValue.get();
					}
				}
				
				int blockHardness = Math.max(1, (int) Math.ceil(blockRawHardness));
				int blockCurrentDamage = BlockDamageManager.getDamage(this.level, pos);
				int blockCurrentHardness = blockHardness - blockCurrentDamage;
				int projectilePierceRemaining = this.infinitePiercing ? Integer.MAX_VALUE : (this.maxPierceCount - this.pierceCounter);
				
				// If remaining pierces are insufficient to destroy a block, damage it and kill the projectile
				if(projectilePierceRemaining < blockCurrentHardness)
				{
					if(projectilePierceRemaining <= 0)
					{
						this.remove(RemovalReason.KILLED);
						this.deadProjectile = true;
						return false;
					}
					int blockNewDamage = blockCurrentDamage + projectilePierceRemaining;
					int blockBreakingStage = (int) (10 * blockNewDamage / (float) blockHardness);
					BlockDamageManager.setDamage(this.level, pos, blockNewDamage, blockHardness, blockBreakingStage);
					this.remove(RemovalReason.KILLED);
					this.deadProjectile = true;
					return false;
				}
				
				boolean blockDestroyed;
				
				// Handle destructible dynamic trees
				if(blockIsTree)
				{
					blockDestroyed = handleDynamicTreeHit(pos, blockHitResult.getDirection(), FallingTreeEntity.DestroyType.HARVEST);
					// If we didn't destroy a block, kill the projectile
					if(!blockDestroyed)
					{
						this.remove(RemovalReason.KILLED);
						this.deadProjectile = true;
						return false;
					}
					// Clear all block damage
					BlockDamageManager.removeDamage(this.level, pos);
				}
				// Handle destructible blocks
				else
				{
					blockDestroyed = this.level.destroyBlock(pos, Config.COMMON.projectileGriefingBlockDrops.get());
					BlockDamageManager.removeDamage(this.level, pos);
				}
				
				// Update pierce counters if we destroyed a block
				if(blockDestroyed && !this.infinitePiercing)
				{
					this.pierceCounter += blockCurrentHardness;
					float penalty = this.modifiedGun.getProjectile().getPierceDamagePenalty();
					float maxPenalty = this.modifiedGun.getProjectile().getPierceDamageMaxPenalty();
					this.pierceDamageFraction -= blockCurrentHardness * penalty;
					this.pierceDamageFraction = Mth.clamp(this.pierceDamageFraction, 1F - maxPenalty, 1.0F);
				}
				
				// Skip movement this tick
				this.setPos(hitVec.x, hitVec.y, hitVec.z);
				return true;
			}
		}
		
		if(result instanceof ExtendedEntityRayTraceResult entityHitResult)
		{
			if(this.deadProjectile)
			{
				return false;
			}
			
			Entity entity = entityHitResult.getEntity();
			
			// If projectile hit an immune entity or player hit themselves, ignore the hit
			boolean entityIsImmune = Config.COMMON.enableImmuneEntities.get() && entity.getType().is(IMMUNE);
			if(entity.getId() == this.shooterId || entityIsImmune)
			{
				return false;
			}
			
			if(this.shooter instanceof Player player)
			{
				if(entity.hasIndirectPassenger(player))
				{
					return false;
				}
			}
			
			// Add an entity to hit list only if it is still alive
			boolean entityIsDead = (entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying());
			if(!entityIsDead)
			{
				this.onHitEntity(entity, result.getLocation(), startVec, endVec, entityHitResult.isHeadshot());
				this.hitEntities.add(entity.getUUID());
			}
			
			boolean shouldRemoveProjectile = false;
			// Check if piercing is enabled
			if(this.maxPierceCount >= 0)
			{
				// Check if projectile has already consumed all pierces
				if(this.pierceCounter >= this.maxPierceCount)
				{
					shouldRemoveProjectile = true;
				}
				// Increase pierce counter and apply damage penalties
				else
				{
					this.pierceCounter++;
					this.pierceDamageFraction -= this.modifiedGun.getProjectile().getPierceDamagePenalty();
					this.pierceDamageFraction = Mth.clamp(this.pierceDamageFraction, 1F - this.modifiedGun.getProjectile().getPierceDamageMaxPenalty(), 1.0F);
				}
			}
			
			if(shouldRemoveProjectile)
			{
				this.remove(RemovalReason.KILLED);
				this.deadProjectile = true;
				entity.invulnerableTime = 0;
				return false;
			}
			else
			{
				Vec3 hitVec = result.getLocation();
				this.setPos(hitVec.x, hitVec.y, hitVec.z);
				entity.invulnerableTime = 0;
				return true;
			}
		}
		
		return false;
	}
	
	protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot)
	{
		float damage = this.getDamage();
		float newDamage = this.getCriticalDamage(this.weapon, this.random, damage);
		boolean critical = damage != newDamage;
		damage = newDamage;
		boolean entityIsImmune = Config.COMMON.enableImmuneEntities.get() && entity.getType().is(IMMUNE);
		boolean entityIsResistant = Config.COMMON.enableResistantEntities.get() && entity.getType().is(RESISTANT);
		boolean entityIsDead = (entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying());
		
		// If projectile hit a resistant entity, apply damage penalty and kill the projectile (no piercing)
		if(entityIsResistant)
		{
			damage *= Config.COMMON.resistantEntitiesDamageMultiplier.get();
			this.remove(RemovalReason.KILLED);
			this.deadProjectile = true;
		}
		
		// Calculate headshot damage
		if(headshot && modifiedGun != null)
		{
			if(this.modifiedGun.getProjectile().getHeadshotMultiplierOverride() != 0)
			{
				damage *= this.modifiedGun.getProjectile().getHeadshotMultiplierOverride();
			}
			else
			{
				double hm = Config.COMMON.headShotDamageMultiplier.get();
				float headshotMultiplier = (float) Math.max(hm, this.modifiedGun.getProjectile().getHeadshotMultiplierMin());
				damage *= headshotMultiplier + this.modifiedGun.getProjectile().getHeadshotMultiplierBonus();
			}
			
			if(this.modifiedGun.getProjectile().getHeadshotExtraDamage() > 0)
			{
				damage += this.modifiedGun.getProjectile().getHeadshotExtraDamage();
			}
		}
		
		// Apply piercing penalty
		damage *= this.pierceDamageFraction;
		
		// Apply underwater penalty
		if(entity.isUnderWater())
		{
			damage *= waterDamagePenalty;
		}
		
		DamageSource source = new DamageSourceProjectile("bullet", this, shooter, weapon).setProjectile();
		
		// Calculate armor/protection piercing
		float bypassDamage = 0;
		if(entity instanceof LivingEntity)
		{
			damage = getArmorReducedDamage(this, (LivingEntity) entity, damage);
			bypassDamage = getProtectionBypassDamage(this, (LivingEntity) entity, damage, source);
		}
		
		DamageSource bypassSource = source.bypassArmor();
		entity.hurt(bypassSource, damage + bypassDamage);
		
		// Since we're bypassing armor, we have to add logic to damage armor durability. Headshots damage only helmets.
		if(entity instanceof Player player)
		{
			if(headshot)
			// The damage input gets divided by 4, hence why we're multiplying the durability damage value.
			{
				player.getInventory().hurtArmor(source, 4 * 4, Inventory.HELMET_SLOT_ONLY);
			}
			else
			{
				player.getInventory().hurtArmor(source, 1, Inventory.ALL_ARMOR_SLOTS);
			}
		}
		
		// Endermen do get hit by projectiles, but since they immediately teleport away, we won't show any hit effects for them
		boolean entityIsEnderman = (entity instanceof EnderMan || entity.getType() == EntityType.ENDERMAN);
		
		if(!entityIsImmune && !entityIsDead && !entityIsEnderman)
		{
			PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity instanceof LivingEntity, headshot));
			if(this.shooter instanceof Player)
			{
				int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
				PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
			}
		}
	}
	
	protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z)
	{
		PacketHandler.getPlayChannel().sendToTrackingChunk(() -> this.level.getChunkAt(pos), new S2CMessageProjectileHitBlock(x, y, z, pos, face));
	}
	
	@Override
	protected void readAdditionalSaveData(CompoundTag compound)
	{
		this.projectile = new Gun.Projectile();
		this.projectile.deserializeNBT(compound.getCompound("Projectile"));
		this.general = new Gun.General();
		this.general.deserializeNBT(compound.getCompound("General"));
		this.modifiedGravity = compound.getDouble("ModifiedGravity");
		this.life = compound.getInt("MaxLife");
		this.pierceCounter = compound.getInt("PierceCount");
		this.pierceDamageFraction = compound.getFloat("PierceDamageFraction");
		this.maxPierceCount = this.projectile.getMaxPierceCount() - 1;
		this.ticksToSkip = compound.getInt("TicksToSkip");
		this.hitEntities.clear();
		if(compound.contains("HitEntities", Tag.TAG_COMPOUND))
		{
			CompoundTag hitList = compound.getCompound("HitEntities");
			for(int i = 0; hitList.contains("hit_" + i); i++)
			{
				this.hitEntities.add(hitList.getUUID("hit_" + i));
			}
		}
	}
	
	@Override
	protected void addAdditionalSaveData(CompoundTag compound)
	{
		compound.put("Projectile", this.projectile.serializeNBT());
		compound.put("General", this.general.serializeNBT());
		compound.putDouble("ModifiedGravity", this.modifiedGravity);
		compound.putInt("MaxLife", this.life);
		compound.putInt("PierceCount", this.pierceCounter);
		compound.putFloat("PierceDamageFraction", this.pierceDamageFraction);
		compound.putInt("TicksToSkip", this.ticksToSkip);
		CompoundTag hitList = new CompoundTag();
		int i = 0;
		for(UUID uuid : this.hitEntities)
		{
			hitList.putUUID("hit_" + i, uuid);
			i++;
		}
		compound.put("HitEntities", hitList);
	}
	
	@Override
	public void writeSpawnData(FriendlyByteBuf buffer)
	{
		buffer.writeNbt(this.projectile.serializeNBT());
		buffer.writeNbt(this.general.serializeNBT());
		buffer.writeInt(this.shooterId);
		writeItemStackToBufIgnoreTag(buffer, this.item);
		buffer.writeDouble(this.modifiedGravity);
		buffer.writeVarInt(this.life);
		buffer.writeVarInt(this.pierceCounter);
		buffer.writeFloat(this.pierceDamageFraction);
		buffer.writeVarInt(this.ticksToSkip);
		buffer.writeVarInt(this.hitEntities.size());
		for(UUID uuid : this.hitEntities)
		{
			buffer.writeUUID(uuid);
		}
	}
	
	@Override
	public void readSpawnData(FriendlyByteBuf buffer)
	{
		this.projectile = new Gun.Projectile();
		this.projectile.deserializeNBT(Objects.requireNonNull(buffer.readNbt()));
		this.general = new Gun.General();
		this.general.deserializeNBT(Objects.requireNonNull(buffer.readNbt()));
		this.shooterId = buffer.readInt();
		this.item = readItemStackFromBufIgnoreTag(buffer);
		this.modifiedGravity = buffer.readDouble();
		this.life = buffer.readVarInt();
		this.pierceCounter = buffer.readVarInt();
		this.pierceDamageFraction = buffer.readFloat();
		this.maxPierceCount = this.projectile.getMaxPierceCount() - 1;
		this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
		this.ticksToSkip = buffer.readVarInt();
		int hitCount = buffer.readVarInt();
		this.hitEntities.clear();
		for(int i = 0; i < hitCount; i++)
		{
			this.hitEntities.add(buffer.readUUID());
		}
	}
	
	public void updateHeading()
	{
		double horizontalDistance = this.getDeltaMovement().horizontalDistance();
		this.setYRot((float) (Mth.atan2(this.getDeltaMovement().x(), this.getDeltaMovement().z()) * (180D / Math.PI)));
		this.setXRot((float) (Mth.atan2(this.getDeltaMovement().y(), horizontalDistance) * (180D / Math.PI)));
		this.yRotO = this.getYRot();
		this.xRotO = this.getXRot();
	}
	
	public Projectile getProjectile()
	{
		return this.projectile;
	}
	
	private Vec3 getVectorFromRotation(float pitch, float yaw)
	{
		float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
		float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
		float f2 = -Mth.cos(-pitch * 0.017453292F);
		float f3 = Mth.sin(-pitch * 0.017453292F);
		return new Vec3(f1 * f2, f3, f * f2);
	}
	
	/**
	 * Gets the entity who spawned the projectile
	 */
	public LivingEntity getShooter()
	{
		return this.shooter;
	}
	
	/**
	 * Gets the id of the entity who spawned the projectile
	 */
	public int getShooterId()
	{
		return this.shooterId;
	}
	
	public float getDamage()
	{
		float initialDamage = (this.projectile.getDamage() + this.additionalDamage);
		float maxRangeDamageMultiplier = this.projectile.getMaxRangeDamageMultiplier();
		int effectiveTickCount = this.tickCount - this.ticksToSkip;
		if(this.projectile.isDamageReduceOverLife() && maxRangeDamageMultiplier < 1)
		{
			float modifier = ((float) this.projectile.getLife() - (float) (effectiveTickCount - 1)) / (float) this.projectile.getLife();
			float finalModifier = Mth.lerp(modifier, maxRangeDamageMultiplier, 1);
			initialDamage *= Math.min(finalModifier, 1);
		}
		else if(maxRangeDamageMultiplier > 1)
		{
			float modifier = ((float) this.projectile.getLife() - (float) (effectiveTickCount - 1)) / (float) this.projectile.getLife();
			float finalModifier = Mth.lerp(modifier, 1, maxRangeDamageMultiplier);
			initialDamage *= Math.max(finalModifier, 1);
		}
		float damage = initialDamage / this.general.getProjectileAmount();
		damage = GunModifierHelper.getModifiedDamage(this.weapon, this.modifiedGun, damage);
		
		return Math.max(0F, damage);
	}
	
	private float getCriticalDamage(ItemStack weapon, RandomSource rand, float damage)
	{
		float chance = GunModifierHelper.getCriticalChance(weapon);
		if(rand.nextFloat() < chance)
		{
			return (float) (damage * Config.COMMON.criticalDamageMultiplier.get());
		}
		return damage;
	}
	
	@Override
	public boolean shouldRenderAtSqrDistance(double distance)
	{
		return true;
	}
	
	@Override
	public void onRemovedFromWorld()
	{
		if(!this.level.isClientSide)
		{
			PacketHandler.getPlayChannel().sendToNearbyPlayers(this::getDeathTargetPoint, new S2CMessageRemoveProjectile(this.getId()));
		}
	}
	
	private LevelLocation getDeathTargetPoint()
	{
		return LevelLocation.create(this.level, this.getX(), this.getY(), this.getZ(), 256);
	}
	
	@Override
	public @NotNull Packet<?> getAddEntityPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	/**
	 * A custom implementation of ray tracing that allows you to pass a predicate to ignore certain
	 * blocks when checking for collisions.
	 *
	 * @param world
	 * 		the world to perform the ray trace
	 * @param context
	 * 		the ray trace context
	 * @param ignorePredicate
	 * 		the block state predicate
	 *
	 * @return a result of the raytrace
	 */
	private static BlockHitResult rayTraceBlocks(Level world, ClipContext context, Predicate<BlockState> ignorePredicate)
	{
		return performRayTrace(context, (rayTraceContext, blockPos) ->
		{
			BlockState blockState = world.getBlockState(blockPos);
			if(ignorePredicate.test(blockState))
			{
				return null;
			}
			FluidState fluidState = world.getFluidState(blockPos);
			Vec3 startVec = rayTraceContext.getFrom();
			Vec3 endVec = rayTraceContext.getTo();
			VoxelShape blockShape = rayTraceContext.getBlockShape(blockState, world, blockPos);
			BlockHitResult blockResult = world.clipWithInteractionOverride(startVec, endVec, blockPos, blockShape, blockState);
			VoxelShape fluidShape = rayTraceContext.getFluidShape(fluidState, world, blockPos);
			BlockHitResult fluidResult = fluidShape.clip(startVec, endVec, blockPos);
			double blockDistance = blockResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(blockResult.getLocation());
			double fluidDistance = fluidResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(fluidResult.getLocation());
			return blockDistance <= fluidDistance ? blockResult : fluidResult;
		}, (rayTraceContext) ->
		{
			Vec3 Vector3d = rayTraceContext.getFrom().subtract(rayTraceContext.getTo());
			return BlockHitResult.miss(rayTraceContext.getTo(), Direction.getNearest(Vector3d.x, Vector3d.y, Vector3d.z), new BlockPos(rayTraceContext.getTo()));
		});
	}
	
	private static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> p_217300_2_)
	{
		Vec3 startVec = context.getFrom();
		Vec3 endVec = context.getTo();
		if(!startVec.equals(endVec))
		{
			double startX = Mth.lerp(-0.0000001, endVec.x, startVec.x);
			double startY = Mth.lerp(-0.0000001, endVec.y, startVec.y);
			double startZ = Mth.lerp(-0.0000001, endVec.z, startVec.z);
			double endX = Mth.lerp(-0.0000001, startVec.x, endVec.x);
			double endY = Mth.lerp(-0.0000001, startVec.y, endVec.y);
			double endZ = Mth.lerp(-0.0000001, startVec.z, endVec.z);
			int blockX = Mth.floor(endX);
			int blockY = Mth.floor(endY);
			int blockZ = Mth.floor(endZ);
			BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
			T t = hitFunction.apply(context, mutablePos);
			if(t != null)
			{
				return t;
			}
			
			double deltaX = startX - endX;
			double deltaY = startY - endY;
			double deltaZ = startZ - endZ;
			int signX = Mth.sign(deltaX);
			//noinspection SuspiciousNameCombination
			int signY = Mth.sign(deltaY);
			int signZ = Mth.sign(deltaZ);
			double d9 = signX == 0 ? Double.MAX_VALUE : (double) signX / deltaX;
			double d10 = signY == 0 ? Double.MAX_VALUE : (double) signY / deltaY;
			double d11 = signZ == 0 ? Double.MAX_VALUE : (double) signZ / deltaZ;
			double d12 = d9 * (signX > 0 ? 1.0D - Mth.frac(endX) : Mth.frac(endX));
			double d13 = d10 * (signY > 0 ? 1.0D - Mth.frac(endY) : Mth.frac(endY));
			double d14 = d11 * (signZ > 0 ? 1.0D - Mth.frac(endZ) : Mth.frac(endZ));
			
			while(d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D)
			{
				if(d12 < d13)
				{
					if(d12 < d14)
					{
						blockX += signX;
						d12 += d9;
					}
					else
					{
						blockZ += signZ;
						d14 += d11;
					}
				}
				else if(d13 < d14)
				{
					blockY += signY;
					d13 += d10;
				}
				else
				{
					blockZ += signZ;
					d14 += d11;
				}
				
				T t1 = hitFunction.apply(context, mutablePos.set(blockX, blockY, blockZ));
				if(t1 != null)
				{
					return t1;
				}
			}
		}
		return p_217300_2_.apply(context);
	}
	
	private boolean handleDynamicTreeHit(BlockPos hitPos, Direction face, FallingTreeEntity.DestroyType destroyType)
	{
		if(!GunMod.dynamicTreesLoaded)
		{
			return false;
		}
		
		Level world = this.level;
		BlockState state = world.getBlockState(hitPos);
		
		BlockPos rootPos = TreeHelper.findRootNode(world, hitPos);
		if(rootPos == BlockPos.ZERO)
		{
			return false;
		}
		
		BlockState rootState = world.getBlockState(rootPos);
		RootyBlock rooty = TreeHelper.getRooty(rootState);
		if(rooty == null)
		{
			return false;
		}
		
		Species species = rooty.getSpecies(rootState, world, rootPos);
		if(species == Species.NULL_SPECIES)
		{
			return false;
		}
		
		BranchBlock branch = TreeHelper.getBranch(state);
		if(branch == null)
		{
			return false;
		}
		
		BranchDestructionData destructionData = branch.destroyBranchFromNode(world, hitPos, face, false, this.shooter);
		if(destructionData.getNumBranches() == 0)
		{
			return false;
		}
		
		for(BlockPos pos : destructionData.getPositions(BranchDestructionData.PosType.BRANCHES))
		{
			BlockDamageManager.removeDamage(world, pos);
		}
		for(BlockPos pos : destructionData.getPositions(BranchDestructionData.PosType.LEAVES))
		{
			BlockDamageManager.removeDamage(world, pos);
		}
		
		List<ItemStack> woodDropList = new ArrayList<>(destructionData.species.getBranchesDrops(world, destructionData.woodVolume));
		
		FallingTreeEntity.dropTree(world, destructionData, woodDropList, destroyType);
		return true;
	}
	
	/** Used for handling block damage by projectiles. */
	public static class BlockDamageManager
	{
		private static final Map<Level, Map<BlockPos, BlockDamageData>> DAMAGE = new WeakHashMap<>();
		
		public static void tick(Level level)
		{
			cleanup(level);
		}
		
		public static int getDamage(Level level, BlockPos pos)
		{
			cleanup(level);
			Map<BlockPos, BlockDamageData> map = DAMAGE.get(level);
			if(map == null)
			{
				return 0;
			}
			BlockDamageData data = map.get(pos);
			if(data == null)
			{
				return 0;
			}
			BlockState currentState = level.getBlockState(pos);
			if(!currentState.equals(data.state))
			{
				map.remove(pos);
				level.destroyBlockProgress(getBreakerId(pos), pos, -1);
				return 0;
			}
			return data.damage;
		}
		
		public static void setDamage(Level level, BlockPos pos, int damage, int maxDamage, int stage)
		{
			cleanup(level);
			Map<BlockPos, BlockDamageData> map = DAMAGE.computeIfAbsent(level, k -> new HashMap<>());
			BlockDamageData data = map.computeIfAbsent(pos, k -> new BlockDamageData());
			data.damage = damage;
			data.lastHitTime = level.getGameTime();
			data.state = level.getBlockState(pos);
			level.destroyBlockProgress(getBreakerId(pos), pos, stage);
		}
		
		public static void removeDamage(Level level, BlockPos pos)
		{
			Map<BlockPos, BlockDamageData> map = DAMAGE.get(level);
			if(map != null)
			{
				map.remove(pos);
			}
			level.destroyBlockProgress(getBreakerId(pos), pos, -1);
		}
		
		private static void cleanup(Level level)
		{
			Map<BlockPos, BlockDamageData> map = DAMAGE.get(level);
			if(map == null)
			{
				return;
			}
			long currentTime = level.getGameTime();
			map.entrySet().removeIf(entry ->
			{
				BlockDamageData data = entry.getValue();
				if(currentTime - data.lastHitTime > Config.COMMON.blockDamageResetThreshold.get())
				{
					level.destroyBlockProgress(getBreakerId(entry.getKey()), entry.getKey(), -1);
					return true;
				}
				return false;
			});
		}
		
		private static int getBreakerId(BlockPos pos)
		{
			return -Objects.hash(pos);
		}
		
		private static class BlockDamageData
		{
			int damage = 0;
			long lastHitTime = 0;
			BlockState state = null;
		}
	}
	
	/**
	 * Create a custom block destroying explosion used by projectiles and grenades.
	 * Note: This explosion doesn't produce any SFX and VFX by itself, so you'll need to play those manually.
	 */
	public static void createExplosion(Entity entity, float radius, boolean griefing)
	{
		Level world = entity.level;
		if(world.isClientSide())
		{
			return;
		}
		
		boolean isProjectile = entity instanceof ProjectileEntity;
		boolean isGunProjectile = isProjectile && ((ProjectileEntity) entity).getProjectile() != null;
		boolean isGrenade = entity instanceof ThrowableGrenadeEntity;
		boolean isImpactGrenade = entity instanceof ThrowableImpactGrenadeEntity;
		
		DamageSource source = isProjectile ? DamageSource.explosion(((ProjectileEntity) entity).getShooter()) : null;
		
		float damage = isGunProjectile ? ((ProjectileEntity) entity).getDamage() : isImpactGrenade ? Config.SERVER.impactGrenadeExplosionDamage.getDefault().floatValue() : isGrenade ? Config.SERVER.grenadeExplosionDamage.getDefault().floatValue() : 20F;
		
		boolean universalGriefing = griefing && Config.COMMON.universalExplosionGriefing.get();
		
		Explosion.BlockInteraction mode = universalGriefing ? Explosion.BlockInteraction.BREAK : Explosion.BlockInteraction.NONE;
		
		boolean destructibleGriefing = griefing && mode.equals(Explosion.BlockInteraction.NONE);
		
		// Reduce damage underwater
		if(entity.isInWater())
		{
			if(isGunProjectile)
			{
				damage *= (1.0F - ((ProjectileEntity) entity).getProjectile().getWaterDamagePenalty());
			}
			else
			{
				damage *= 0.5F;
			}
		}
		
		Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius * 0.5F, false, mode, damage);
		
		if(net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
		{
			return;
		}
		
		explosion.explode();
		explosion.finalizeExplosion(true);
		
		// Handle dynamic trees
		if(GunMod.dynamicTreesLoaded)
		{
			Iterator<BlockPos> iterator = explosion.getToBlow().iterator();
			while(iterator.hasNext())
			{
				BlockPos pos = iterator.next();
				BlockState state = world.getBlockState(pos);
				boolean blockIsDestructible = state.is(ModTags.Blocks.DESTRUCTIBLE);
				
				if(TreeHelper.isTreePart(state))
				{
					boolean canDamageTree = (universalGriefing || (destructibleGriefing && blockIsDestructible));
					if(!canDamageTree)
					{
						iterator.remove();
						continue;
					}
					BranchBlock branch = TreeHelper.getBranch(state);
					if(branch != null)
					{
						Vec3 explosionCenter = new Vec3(entity.getX(), entity.getY(), entity.getZ());
						Vec3 blockCenter = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
						Direction fallDir = Direction.getNearest(blockCenter.x - explosionCenter.x, blockCenter.y - explosionCenter.y, blockCenter.z - explosionCenter.z).getOpposite();
						LivingEntity shooter = entity instanceof ProjectileEntity ? ((ProjectileEntity) entity).getShooter() : null;
						BranchDestructionData destructionData = branch.destroyBranchFromNode(world, pos, fallDir, false, shooter);
						if(destructionData.getNumBranches() > 0)
						{
							for(BlockPos p : destructionData.getPositions(BranchDestructionData.PosType.BRANCHES))
							{
								BlockDamageManager.removeDamage(world, p);
							}
							for(BlockPos p : destructionData.getPositions(BranchDestructionData.PosType.LEAVES))
							{
								BlockDamageManager.removeDamage(world, p);
							}
							List<ItemStack> woodDropList = new ArrayList<>(destructionData.species.getBranchesDrops(world, destructionData.woodVolume));
							FallingTreeEntity.dropTree(world, destructionData, woodDropList, FallingTreeEntity.DestroyType.BLAST);
						}
						iterator.remove();
					}
				}
			}
		}
		
		// Handle destructible griefing
		if(destructibleGriefing)
		{
			// Add all destructible blocks to the destruction list
			List<BlockPos> breakBlocksList = new ArrayList<>();
			for(BlockPos pos : explosion.getToBlow())
			{
				BlockState state = world.getBlockState(pos);
				boolean blockIsDestructible = state.is(ModTags.Blocks.DESTRUCTIBLE);
				if(blockIsDestructible)
				{
					breakBlocksList.add(pos);
				}
			}
			
			// Destroy blocks
			for(BlockPos pos : breakBlocksList)
			{
				BlockState state = world.getBlockState(pos);
				state.onBlockExploded(world, pos, explosion);
				BlockDamageManager.removeDamage(world, pos);
			}
		}
	}
	
	/**
	 * Create a fire explosion used by fire grenades that doesn't damage by itself and doesn't destroy any blocks.
	 * Note: This explosion doesn't produce any SFX and VFX by itself, so you'll need to play those manually.
	 */
	public static void createFireExplosion(Entity entity, float radius, boolean griefing)
	{
		Level world = entity.level;
		if(world.isClientSide())
		{
			return;
		}
		
		Explosion explosion = new ProjectileExplosion(world, entity, null, null, entity.getX(), entity.getY(), entity.getZ(), radius, true, Explosion.BlockInteraction.NONE, 0F);
		
		if(net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
		{
			return;
		}
		
		explosion.explode();
		explosion.finalizeExplosion(true);
	}
	
	/**
	 * Author: MrCrayfish
	 */
	public static class EntityResult
	{
		private final Entity entity;
		private final Vec3 startVec;
		private final Vec3 hitVec;
		private final boolean headshot;
		
		public EntityResult(Entity entity, Vec3 startVec, Vec3 hitVec, boolean headshot)
		{
			this.entity = entity;
			this.startVec = startVec;
			this.hitVec = hitVec;
			this.headshot = headshot;
		}
		
		/**
		 * Gets the entity that was hit by the projectile
		 */
		public Entity getEntity()
		{
			return this.entity;
		}
		
		/**
		 * Gets the position the projectile hit
		 */
		public Vec3 getStartVec()
		{
			return this.startVec;
		}
		
		/**
		 * Gets the position the projectile hit
		 */
		public Vec3 getHitPos()
		{
			return this.hitVec;
		}
		
		/**
		 * Gets the position the projectile hit
		 */
		public double getDistanceToHit()
		{
			return this.startVec.distanceTo(this.hitVec);
		}
		
		/**
		 * Gets if this was a headshot
		 */
		public boolean isHeadshot()
		{
			return this.headshot;
		}
	}
	
	/**
	 * Author: MrCrayfish
	 */
	static class HitComparator implements java.util.Comparator<EntityResult>
	{
		@Override
		public int compare(EntityResult a, EntityResult b)
		{
			return (int) (a.getDistanceToHit() * 100 - b.getDistanceToHit() * 100);
		}
	}
	
	/**
	 * Writes an ItemStack to a buffer without its tag compound
	 *
	 * @param buf
	 * 		the byte buffer to write to
	 * @param stack
	 * 		the item stack to write
	 */
	public static void writeItemStackToBufIgnoreTag(ByteBuf buf, ItemStack stack)
	{
		if(stack.isEmpty())
		{
			buf.writeShort(-1);
			return;
		}
		buf.writeShort(Item.getId(stack.getItem()));
		buf.writeByte(stack.getCount());
	}
	
	/**
	 * Reads an ItemStack from a buffer that has no tag compound.
	 *
	 * @param buf
	 * 		the byte buffer to read from
	 *
	 * @return the read item stack
	 */
	public static ItemStack readItemStackFromBufIgnoreTag(ByteBuf buf)
	{
		int id = buf.readShort();
		if(id < 0)
		{
			return ItemStack.EMPTY;
		}
		return new ItemStack(Item.byId(id), buf.readByte());
	}
	
	public static float getArmorReducedDamage(ProjectileEntity bullet, LivingEntity entity, float damage)
	{
		if(!(entity instanceof LivingEntity))
		{
			return damage;
		}
		
		float reducedDamage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getArmorValue(), (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
		
		float bypassLevel = Mth.clamp(bullet.getProjectile().getArmorBypass(), 0, 1);
		return Mth.lerp(bypassLevel, reducedDamage, damage);
	}
	
	public static float getProtectionBypassDamage(ProjectileEntity bullet, LivingEntity entity, float damage, DamageSource source)
	{
		if(!(entity instanceof LivingEntity) || damage == 0)
		{
			return 0;
		}
		
		int protection = Mth.clamp(EnchantmentHelper.getDamageProtection(entity.getArmorSlots(), source), 0, 20);
		float reducedDamage = CombatRules.getDamageAfterMagicAbsorb(damage, (float) protection);
		float damageReductionFactor = reducedDamage / damage;
		float finalDamage = damage / damageReductionFactor;
		
		float bypassLevel = Mth.clamp(bullet.getProjectile().getProtectionBypass(), 0, 1);
		return (finalDamage - damage) * bypassLevel;
	}
	
	public static int updateTargetBlock(TargetBlock block, LevelAccessor accessor, BlockState state, BlockHitResult result, Entity entity)
	{
		try
		{
			return (int) updateRedstoneOutputMethod.invoke(block, accessor, state, result, entity);
		}
		catch(IllegalAccessException | InvocationTargetException ignored)
		{
			return 0;
		}
	}
}

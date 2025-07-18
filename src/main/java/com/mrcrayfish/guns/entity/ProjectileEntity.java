package com.mrcrayfish.guns.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.common.BoundingBoxManager;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.Gun.Projectile;
import com.mrcrayfish.guns.common.ServerAimTracker;
import com.mrcrayfish.guns.common.SpreadTracker;
import com.mrcrayfish.guns.entity.grenade.ThrowableGrenadeEntity;
import com.mrcrayfish.guns.entity.grenade.ThrowableImpactGrenadeEntity;
import com.mrcrayfish.guns.entity.projectile.PipeGrenadeEntity;
import com.mrcrayfish.guns.entity.projectile.RocketEntity;
import com.mrcrayfish.guns.event.GunProjectileHitEvent;
import com.mrcrayfish.guns.init.ModSounds;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.init.ModTags;
import com.mrcrayfish.guns.interfaces.IDamageable;
import com.mrcrayfish.guns.interfaces.IExplosionDamageable;
import com.mrcrayfish.guns.interfaces.IHeadshotBox;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.network.PacketHandler;
import com.mrcrayfish.guns.network.message.S2CMessageBlood;
import com.mrcrayfish.guns.network.message.S2CMessageProjectileHitBlock;
import com.mrcrayfish.guns.network.message.S2CMessageProjectileHitEntity;
import com.mrcrayfish.guns.network.message.S2CMessageRemoveProjectile;
import com.mrcrayfish.guns.util.*;
import com.mrcrayfish.guns.util.math.ExtendedEntityRayTraceResult;
import com.mrcrayfish.guns.world.ProjectileExplosion;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.mrcrayfish.guns.init.ModTags.Entities.HIT_IMMUNE;
import static com.mrcrayfish.guns.init.ModTags.Entities.HIT_RESISTANT;

public class ProjectileEntity extends Entity implements IEntityAdditionalSpawnData
{
    private static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();
    private static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null && Config.COMMON.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;

    protected int shooterId;
    protected LivingEntity shooter;
    protected Gun modifiedGun;
    protected Gun.General general;
    protected Gun.Projectile projectile;
    private ItemStack weapon = ItemStack.EMPTY;
    private ItemStack item = ItemStack.EMPTY;
    protected float additionalDamage = 0.0F;
    protected int pierceCount = 0;
    protected float pierceDamageFraction = 1.0F;
    protected EntityDimensions entitySize;
    protected double modifiedGravity;
    protected int life;
    protected int soundTime = 0;
    protected boolean deadProjectile = false;
    protected float pitch = 0.9F + level.random.nextFloat() * 0.2F;
    public float rotation;
    public float prevRotation;

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
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
        this.modifiedGravity = modifiedGun.getProjectile().isGravity() ? GunModifierHelper.getModifiedProjectileGravity(weapon, -0.04 * modifiedGun.getProjectile().getGravity()) : 0.0;
        this.life = GunModifierHelper.getModifiedProjectileLife(weapon, this.projectile.getLife());

        /* Get speed and set motion */
        Vec3 dir = this.getDirection(shooter, weapon, item, modifiedGun);
        double speed = GunModifierHelper.getModifiedProjectileSpeed(weapon, this.projectile.getSpeed());
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();

        /* Spawn the projectile halfway between the previous and current position */
        double posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
        double posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
        double posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
        this.setPos(posX, posY, posZ);

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
    protected void defineSynchedData() {}

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose)
    {
        return this.entitySize;
    }

    private Vec3 getDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun)
    {
        float gunSpread = GunCompositeStatHelper.getCompositeSpread(weapon, modifiedGun);
        float minSpread = GunCompositeStatHelper.getCompositeMinSpread(weapon, modifiedGun);

        if(gunSpread == 0F)
        {
            return this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        }

        if(shooter instanceof Player)
        {
            float initialGunSpread = Mth.lerp(SpreadTracker.get((Player) shooter).getSpread(item),minSpread,gunSpread);
            if(!this.general.isAlwaysSpread() || minSpread > 0)
            {
                gunSpread = initialGunSpread;
            }

            if(ModSyncedDataKeys.AIMING.getValue((Player) shooter))
            {
                float aimingGunSpread = gunSpread * (1-(this.general.getSpreadAdsReduction()));
                float aimPosition = (float) Mth.clamp(ServerAimTracker.getAimingTicks((Player) shooter)/(5/GunCompositeStatHelper.getCompositeAimDownSightSpeed(weapon)),0,1);
                gunSpread = Mth.lerp(aimPosition,initialGunSpread,aimingGunSpread);
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

        this.prevRotation = this.rotation;
        double speed = this.getDeltaMovement().length();
        if (speed > 0.1)
        {
            this.rotation += speed * 50;
        }

        if(!this.level.isClientSide())
        {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            HitResult result = rayTraceBlocks(this.level, new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);

            //Projectile flyby sound
            boolean isBullet = !(this instanceof RocketEntity || this instanceof PipeGrenadeEntity);
            if(isBullet)
            {
                AABB range = new AABB(startVec.x-5, startVec.y-5, startVec.z-5, startVec.x+5, startVec.y+5, startVec.z+5);
                List<Player> players = this.level.getEntitiesOfClass(Player.class, range);
                float volume = 0.5F + this.level.getRandom().nextFloat() * 0.4F;
                boolean isShotgun = general.getProjectileAmount() > 1;
                if (!players.isEmpty() && this.tickCount > 3 && soundTime < this.tickCount - 3)
                {
                    if(isShotgun) //Divide volume by projectile amount to avoid deafening players irl.
                    {
                        volume = volume / general.getProjectileAmount();
                    }
                    this.level.playSound(null, startVec.x,startVec.y,startVec.z, ModSounds.FLYBY.get(), SoundSource.NEUTRAL, volume, 0.8F + this.level.getRandom().nextFloat() * 0.4F);
                    this.soundTime = this.tickCount;
                }
            }

            if(result.getType() != HitResult.Type.MISS)
            {
                endVec = result.getLocation();
            }
            List<EntityResult> hitEntities = null;
            int maxPierceCount = (projectile.getMaxPierceCount()-1);
            if(maxPierceCount == 0)
            {
                EntityResult entityResult = this.findEntityOnPath(startVec, endVec);
                if(entityResult != null)
                {
                    hitEntities = Collections.singletonList(entityResult);
                }
            }
            else
            {
                hitEntities = this.findEntitiesOnPath(startVec, endVec);
            }
            if(hitEntities != null && !hitEntities.isEmpty())
            {
                for(EntityResult entityResult : hitEntities)
                {
                    result = new ExtendedEntityRayTraceResult(entityResult);
                    if(((EntityHitResult) result).getEntity() instanceof Player player)
                    {

                        if(this.shooter instanceof Player && !((Player) this.shooter).canHarmPlayer(player))
                        {
                            result = null;
                        }
                    }
                    if(result != null)
                    {
                        this.onHit(result, startVec, endVec);
                    }
                }
            }
            else
            {
                this.onHit(result, startVec, endVec);
            }
        }

        double nextPosX = this.getX() + this.getDeltaMovement().x();
        double nextPosY = this.getY() + this.getDeltaMovement().y();
        double nextPosZ = this.getZ() + this.getDeltaMovement().z();
        this.setPos(nextPosX, nextPosY, nextPosZ);

        if(this.projectile.isGravity())
        {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
        }

        if(this.tickCount >= this.life)
        {
            if(this.isAlive())
            {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
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
        List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0+this.projectile.getSize()), PROJECTILE_TARGETS);
        double closestDistance = Double.MAX_VALUE;
        for(Entity entity : entities)
        {
            boolean isDead = (entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying());
            boolean isImmune = Config.COMMON.enableImmuneEntities.get() && entity.getType().is(HIT_IMMUNE);
            if(!entity.equals(this.shooter) && !isImmune)
            {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if(result == null || isDead)
                    continue;
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
        List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0+this.projectile.getSize()), PROJECTILE_TARGETS);
        for(Entity entity : entities)
        {
        	boolean isDead = (entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying());
            boolean isImmune = Config.COMMON.enableImmuneEntities.get() && entity.getType().is(HIT_IMMUNE);
            if(!entity.equals(this.shooter) && !isImmune)
            {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if(result == null || isDead)
                    continue;
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
            HitResult raytraceresult = rayTraceBlocks(this.level, new ClipContext(startVec, grownHitPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), IGNORE_LEAVES);
            if(raytraceresult.getType() == HitResult.Type.BLOCK)
            {
                return null;
            }
            hitPos = grownHitPos;
        }

        /* Check for headshot */
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

    private void onHit(HitResult result, Vec3 startVec, Vec3 endVec)
    {
        if(MinecraftForge.EVENT_BUS.post(new GunProjectileHitEvent(result, this)))
        {
            return;
        }

        if(result instanceof BlockHitResult blockHitResult)
        {
            if(blockHitResult.getType() == HitResult.Type.MISS)
            {
                return;
            }

            Vec3 hitVec = result.getLocation();
            BlockPos pos = blockHitResult.getBlockPos();
            BlockState state = this.level.getBlockState(pos);
            Block block = state.getBlock();
            boolean wasFragileDestroyed = false;

            if(Config.COMMON.enableFragileBreaking.get() && state.is(ModTags.Blocks.FRAGILE))
            {
                float destroySpeed = state.getDestroySpeed(this.level, pos);
                if(destroySpeed >= 0)
                {
                    float chance = Config.COMMON.fragileBaseBreakChance.get().floatValue() / (destroySpeed + 1);
                    if(this.random.nextFloat() < chance)
                    {
                        this.level.destroyBlock(pos, Config.COMMON.fragileBlockDrops.get());
                        wasFragileDestroyed = true;
                    }
                }
            }

            if(!state.getMaterial().isReplaceable() && !state.is(ModTags.Blocks.FRAGILE) || (state.is(ModTags.Blocks.FRAGILE) && !wasFragileDestroyed))
            {
                this.remove(RemovalReason.KILLED);
            }

            if(block instanceof IDamageable)
            {
                ((IDamageable) block).onBlockDamaged(this.level, state, pos, this, this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
            }

            this.onHitBlock(state, pos, blockHitResult.getDirection(), hitVec.x, hitVec.y, hitVec.z);

            if(block instanceof TargetBlock targetBlock)
            {
                int power = ReflectionUtil.updateTargetBlock(targetBlock, this.level, state, blockHitResult, this);
                if(this.shooter instanceof ServerPlayer serverPlayer)
                {
                    serverPlayer.awardStat(Stats.TARGET_HIT);
                    CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, this, blockHitResult.getLocation(), power);
                }
            }

            if(block instanceof BellBlock bell)
            {
                bell.attemptToRing(this.level, pos, blockHitResult.getDirection());
            }
            return;
        }

        if(result instanceof ExtendedEntityRayTraceResult entityHitResult)
        {
            if(this.deadProjectile)
            {
                return;
            }
            
            Entity entity = entityHitResult.getEntity();
            boolean isImmune = Config.COMMON.enableImmuneEntities.get() && entity.getType().is(HIT_IMMUNE);
            if(entity.getId() == this.shooterId || isImmune)
            {
                return;
            }

            if(this.shooter instanceof Player player)
            {
                if(entity.hasIndirectPassenger(player))
                {
                    return;
                }
            }

            boolean isDead = (entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying());
            this.onHitEntity(entity, result.getLocation(), startVec, endVec, entityHitResult.isHeadshot());

        	if (!isDead)
        	{
                int maxPierceCount = (projectile.getMaxPierceCount()-1);
        		if(maxPierceCount == 0)
            	{
                	this.remove(RemovalReason.KILLED);
        			this.deadProjectile = true;
            	}
            	else
            	{
            		if (this.pierceCount>=maxPierceCount && maxPierceCount>=0)
            		{
            			this.remove(RemovalReason.KILLED);
            			this.deadProjectile = true;
            		}
            		else
            		{
            			this.pierceCount++;
            			this.pierceDamageFraction -= this.modifiedGun.getProjectile().getPierceDamagePenalty();
            			this.pierceDamageFraction = Mth.clamp(pierceDamageFraction, 1F-this.modifiedGun.getProjectile().getPierceDamageMaxPenalty(), 1.0F);
            		}
            	}
        	}
            entity.invulnerableTime = 0;
        }
    }

    @SuppressWarnings("deprecation")
	protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot)
    {
        float damage = this.getDamage();
        float newDamage = this.getCriticalDamage(this.weapon, this.random, damage);
        boolean critical = damage != newDamage;
        damage = newDamage;
        boolean isImmune = Config.COMMON.enableImmuneEntities.get() && entity.getType().is(HIT_IMMUNE);
        boolean isResistant = Config.COMMON.enableResistantEntities.get() && entity.getType().is(HIT_RESISTANT);

        if(isResistant)
        {
            damage *= Config.COMMON.resistantDamageMultiplier.get();
            this.remove(RemovalReason.KILLED);
            this.deadProjectile = true;
        }

        if(headshot && modifiedGun != null)
        {
            if (this.modifiedGun.getProjectile().getHeadshotMultiplierOverride()!=0)
            	damage *= this.modifiedGun.getProjectile().getHeadshotMultiplierOverride();
            else
            {
            	double hm = Config.COMMON.headShotDamageMultiplier.get();
            	float headshotMultiplier = (float) Math.max(hm,this.modifiedGun.getProjectile().getHeadshotMultiplierMin());
            	damage *= headshotMultiplier+this.modifiedGun.getProjectile().getHeadshotMultiplierBonus();
            }
            
            if (this.modifiedGun.getProjectile().getHeadshotExtraDamage()>0)
            	damage += this.modifiedGun.getProjectile().getHeadshotExtraDamage();
        }
        
        damage *= this.pierceDamageFraction;

        DamageSource source = new DamageSourceProjectile("bullet", this, shooter, weapon).setProjectile();
        float bypassDamage = 0;
        if (entity instanceof LivingEntity)
        {
        	damage = ProjectileStatHelper.getArmorReducedDamage(this, (LivingEntity) entity, damage);
        	bypassDamage = ProjectileStatHelper.getProtectionBypassDamage(this, (LivingEntity) entity, damage, source);
        }

        boolean isDead = (entity instanceof LivingEntity && ((LivingEntity) entity).isDeadOrDying());
        DamageSource bypassSource = source.bypassArmor();
        entity.hurt(bypassSource, damage+bypassDamage);
        
        // Since we're bypassing armor, we have to add logic to damage the armor's durability
        // And since we're doing that, let's make headshots damage only the helmet, and by a larger amount.
        if (entity instanceof Player player)
        {
            if (headshot)
        	// The damage input gets divided by 4, hence why we're multiplying the durability damage value.
            player.getInventory().hurtArmor(source, 4*4, Inventory.HELMET_SLOT_ONLY);
        	else
            {
                player.getInventory().hurtArmor(source, 1, Inventory.ALL_ARMOR_SLOTS);
            }
        }

        boolean isEnderman = (entity instanceof EnderMan || entity.getType() == EntityType.ENDERMAN);
        if (!isImmune && this.shooter instanceof Player && !isDead && !isEnderman)
        {
            int hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }
        if (!isImmune && !isDead && !isEnderman)
        {
            PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z, entity instanceof LivingEntity, headshot));
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.put("Projectile", this.projectile.serializeNBT());
        compound.put("General", this.general.serializeNBT());
        compound.putDouble("ModifiedGravity", this.modifiedGravity);
        compound.putInt("MaxLife", this.life);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.projectile.serializeNBT());
        buffer.writeNbt(this.general.serializeNBT());
        buffer.writeInt(this.shooterId);
        BufferUtil.writeItemStackToBufIgnoreTag(buffer, this.item);
        buffer.writeDouble(this.modifiedGravity);
        buffer.writeVarInt(this.life);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        this.projectile = new Gun.Projectile();
        this.projectile.deserializeNBT(Objects.requireNonNull(buffer.readNbt()));
        this.general = new Gun.General();
        this.general.deserializeNBT(Objects.requireNonNull(buffer.readNbt()));
        this.shooterId = buffer.readInt();
        this.item = BufferUtil.readItemStackFromBufIgnoreTag(buffer);
        this.modifiedGravity = buffer.readDouble();
        this.life = buffer.readVarInt();
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
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
        if(this.projectile.isDamageReduceOverLife() && maxRangeDamageMultiplier < 1)
        {
            float modifier = ((float) this.projectile.getLife() - (float) (this.tickCount - 1)) / (float) this.projectile.getLife();
            float finalModifier = Mth.lerp(modifier, maxRangeDamageMultiplier, 1);
            initialDamage *= Math.min(finalModifier, 1);
        }
        else
        if(maxRangeDamageMultiplier > 1)
        {
            float modifier = ((float) this.projectile.getLife() - (float) (this.tickCount - 1)) / (float) this.projectile.getLife();
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
     * @param world     the world to perform the ray trace
     * @param context   the ray trace context
     * @param ignorePredicate the block state predicate
     * @return a result of the raytrace
     */
    private static BlockHitResult rayTraceBlocks(Level world, ClipContext context, Predicate<BlockState> ignorePredicate)
    {
        return performRayTrace(context, (rayTraceContext, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if(ignorePredicate.test(blockState)) return null;
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
        }, (rayTraceContext) -> {
            Vec3 Vector3d = rayTraceContext.getFrom().subtract(rayTraceContext.getTo());
            return BlockHitResult.miss(rayTraceContext.getTo(), Direction.getNearest(Vector3d.x, Vector3d.y, Vector3d.z), new BlockPos(rayTraceContext.getTo()));
        });
    }

    private static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> p_217300_2_)
    {
        Vec3 startVec = context.getFrom();
        Vec3 endVec = context.getTo();
        if(startVec.equals(endVec))
        {
            return p_217300_2_.apply(context);
        }
        else
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

            return p_217300_2_.apply(context);
        }
    }

    /**
     * Creates an explosion with customizable parameters. Don't use outside this class.
     *
     * @param entity The entity to explode
     * @param radius The size of the explosion
     * @param griefing If true, forces explosion mode to be NONE
     * @param fire If true, creates a fire explosion
     * @param noFX If true, doesn't use vanilla SFX and VFX
     */
    private static void createExplosionInternal(Entity entity, float radius, boolean griefing, boolean fire, boolean noFX)
    {
        Level world = entity.level;
        if (world.isClientSide()) return;

        // Common parameters
        DamageSource source = null;
        float damage = 0F;
        Explosion.BlockInteraction mode = Explosion.BlockInteraction.NONE;

        if (!fire)
        {
            boolean isProjectile = entity instanceof ProjectileEntity;
            boolean isGrenade = entity instanceof ThrowableGrenadeEntity;
            boolean isImpactGrenade = entity instanceof ThrowableImpactGrenadeEntity;

            source = isProjectile ? DamageSource.explosion(((ProjectileEntity) entity).getShooter()) : null;
            boolean hasGunProjectile = isProjectile && ((ProjectileEntity) entity).getProjectile() != null;

            damage = hasGunProjectile ? ((ProjectileEntity) entity).getDamage() :
                    isImpactGrenade ? Config.COMMON.impactGrenadeExplosionDamage.getDefault().floatValue() :
                            isGrenade ? Config.COMMON.handGrenadeExplosionDamage.getDefault().floatValue() :
                                    20F;

            mode = griefing && Config.COMMON.explosionGriefing.get()
                    ? Explosion.BlockInteraction.BREAK
                    : Explosion.BlockInteraction.NONE;
        }

        Explosion explosion = new ProjectileExplosion(world, entity, source, null,
                entity.getX(), entity.getY(), entity.getZ(), radius, fire, mode, damage);

        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion)) return;

        explosion.explode();
        explosion.finalizeExplosion(true);

        List<BlockPos> fragileBlocks = new ArrayList<>();
        if (!fire) {
            for (BlockPos pos : explosion.getToBlow()) {
                BlockState state = world.getBlockState(pos);
                if (state.is(ModTags.Blocks.FRAGILE)) {
                    fragileBlocks.add(pos);
                }
            }
        }

        if (mode == Explosion.BlockInteraction.NONE && !fire) {
            for (BlockPos pos : fragileBlocks) {
                world.destroyBlock(pos, Config.COMMON.fragileBlockDrops.get());
            }
        }

        if (!fire && !noFX) {
            // Handle block explosion effects
            explosion.getToBlow().forEach(pos -> {
                if (world.getBlockState(pos).getBlock() instanceof IExplosionDamageable) {
                    ((IExplosionDamageable) world.getBlockState(pos).getBlock())
                            .onProjectileExploded(world, world.getBlockState(pos), pos, entity);
                }
            });

            if (mode == Explosion.BlockInteraction.NONE) {
                explosion.clearToBlow();
            }

            // Send explosion packet to nearby players
            for (ServerPlayer player : ((ServerLevel) world).players()) {
                if (player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < 4096) {
                    player.connection.send(new ClientboundExplodePacket(
                            entity.getX(), entity.getY(), entity.getZ(), radius,
                            explosion.getToBlow(), explosion.getHitPlayers().get(player)));
                }
            }
        }
    }

    // Note: doesn't disable particle spawning in ClientPlayHandler.
    public static void createGenericExplosion(Entity entity, float radius, boolean griefing) {
        createExplosionInternal(entity, radius, griefing, false, false);
    }

    public static void createCustomExplosion(Entity entity, float radius, boolean griefing) {
        createExplosionInternal(entity, radius, griefing, false, true);
    }

    public static void createFireExplosion(Entity entity, float radius, boolean griefing) {
        createExplosionInternal(entity, radius, griefing, true, true);
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
        public int compare(EntityResult a, EntityResult b) {
            return (int) (a.getDistanceToHit()*100 - b.getDistanceToHit()*100);
        }
    }
}

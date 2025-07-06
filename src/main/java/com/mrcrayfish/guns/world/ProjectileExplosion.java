package com.mrcrayfish.guns.world;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class ProjectileExplosion extends Explosion
{
    private static final ExplosionDamageCalculator DEFAULT_CONTEXT = new ExplosionDamageCalculator();

    private final Level world;
    private final double x;
    private final double y;
    private final double z;
    private final float size;
    private final float projectileDamage;
    private final boolean causesFire;
    private final Entity exploder;
    private final ExplosionDamageCalculator context;
    private final BlockInteraction mode;
    private final RandomSource random = RandomSource.create();
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();

    public ProjectileExplosion(Level world, Entity exploder, @Nullable DamageSource source, @Nullable ExplosionDamageCalculator context, double x, double y, double z, float size, boolean causesFire, BlockInteraction mode)
    {
        super(world, exploder, source, context, x, y, z, size, causesFire, mode);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
		this.projectileDamage = size*5;
        this.causesFire = causesFire;
        this.exploder = exploder;
        this.context = context == null ? DEFAULT_CONTEXT : context;
        this.mode = mode;
    }

    public ProjectileExplosion(Level world, Entity exploder, @Nullable DamageSource source, @Nullable ExplosionDamageCalculator context, double x, double y, double z, float size, boolean causesFire, BlockInteraction mode, float projectileDamage)
    {
        super(world, exploder, source, context, x, y, z, size, causesFire, mode);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
        this.projectileDamage = projectileDamage;
        this.causesFire = causesFire;
        this.exploder = exploder;
        this.context = context == null ? DEFAULT_CONTEXT : context;
        this.mode = mode;
    }

    @Override
    public void explode()
    {
        Set<BlockPos> set = Sets.newHashSet();
        for(int x = 0; x < 16; x++)
        {
            this.world.gameEvent(this.exploder, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
            for(int y = 0; y < 16; y++)
            {
                for(int z = 0; z < 16; z++)
                {
                    if(x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15)
                    {
                        double d0 = (float) x / 15.0F * 2.0F - 1.0F;
                        double d1 = (float) y / 15.0F * 2.0F - 1.0F;
                        double d2 = (float) z / 15.0F * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float f = this.size * (0.7F + this.world.random.nextFloat() * 0.6F);
                        double blockX = this.x;
                        double blockY = this.y;
                        double blockZ = this.z;

                        for(; f > 0.0F; f -= 0.225F)
                        {
                            BlockPos pos = new BlockPos(blockX, blockY, blockZ);
                            BlockState blockState = this.world.getBlockState(pos);
                            FluidState fluidState = this.world.getFluidState(pos);
                            Optional<Float> optional = this.context.getBlockExplosionResistance(this, this.world, pos, blockState, fluidState);
                            if(optional.isPresent())
                            {
                                f -= (optional.get() + 0.3F) * 0.3F;
                            }

                            if(f > 0.0F && this.context.shouldBlockExplode(this, this.world, pos, blockState, f))
                            {
                                set.add(pos);
                            }

                            blockX += d0 * (double) 0.3F;
                            blockY += d1 * (double) 0.3F;
                            blockZ += d2 * (double) 0.3F;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(set);

        float radius = this.size * 2.0F;
        int minX = Mth.floor(this.x - (double) radius - 1.0D);
        int maxX = Mth.floor(this.x + (double) radius + 1.0D);
        int minY = Mth.floor(this.y - (double) radius - 1.0D);
        int maxY = Mth.floor(this.y + (double) radius + 1.0D);
        int minZ = Mth.floor(this.z - (double) radius - 1.0D);
        int maxZ = Mth.floor(this.z + (double) radius + 1.0D);

        List<Entity> entities = this.world.getEntities(this.exploder, new AABB(minX, minY, minZ, maxX, maxY, maxZ));

        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, entities, radius);

        Vec3 explosionPos = new Vec3(this.x, this.y, this.z);
        for(Entity entity : entities)
        {
            if(entity.ignoreExplosion())
                continue;

            double strength = Math.sqrt(entity.distanceToSqr(explosionPos)) / radius;
            if(strength > 1.0D)
                continue;

            Vec3 entityPos = entity.position();
            ClipContext context = new ClipContext(
                    explosionPos,
                    entityPos,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    entity
            );

            BlockHitResult result = this.world.clip(context);
            if(result.getType() != HitResult.Type.MISS)
                continue;

            double deltaX = entity.getX() - this.x;
            double deltaY = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
            double deltaZ = entity.getZ() - this.z;
            double distanceToExplosion = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if(distanceToExplosion != 0.0D)
            {
                deltaX /= distanceToExplosion;
                deltaY /= distanceToExplosion;
                deltaZ /= distanceToExplosion;
            }
            else
            {
                // Fixes an issue where explosion exactly on the player would cause no damage
                deltaX = 0.0;
                deltaY = 1.0;
                deltaZ = 0.0;
            }

            double blockDensity = getSeenPercent(explosionPos, entity);
            double rawDamage = (1.0D - strength) * blockDensity;
            double damage = Math.min( ((rawDamage * rawDamage + rawDamage) / 2.0D)*(projectileDamage*10) + 1.0D, projectileDamage);
            entity.hurt(this.getDamageSource(), (float) damage);

            //Explosion knockback code
            /*
            double blastDamage = rawDamage;
            if(entity instanceof LivingEntity)
            {
                blastDamage = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, rawDamage);
            }
            entity.setDeltaMovement(entity.getDeltaMovement().add(deltaX * blastDamage, deltaY * blastDamage, deltaZ * blastDamage));
            */

            if(entity instanceof Player player)
            {
                if(!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying))
                {
                    this.getHitPlayers().put(player, new Vec3(deltaX * rawDamage, deltaY * rawDamage, deltaZ * rawDamage));
                }
            }
        }
    }

    // Copied from Explosion.class
    @Override
    public void finalizeExplosion(boolean p_46076_) {
        if (this.world.isClientSide) {
            this.world.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean flag = this.mode != Explosion.BlockInteraction.NONE;
        if (p_46076_) {
            if (!(this.size < 2.0F) && flag) {
                this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            } else {
                this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            }
        }

        if (flag) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
            boolean flag1 = this.getSourceMob() instanceof Player;
            Util.shuffle(this.toBlow, this.world.random);

            for(BlockPos blockpos : this.toBlow) {
                BlockState blockstate = this.world.getBlockState(blockpos);
                Block block = blockstate.getBlock();
                if (!blockstate.isAir()) {
                    BlockPos blockpos1 = blockpos.immutable();
                    this.world.getProfiler().push("explosion_blocks");
                    if (blockstate.canDropFromExplosion(this.world, blockpos, this)) {
                        Level $$9 = this.world;
                        if ($$9 instanceof ServerLevel) {
                            ServerLevel serverlevel = (ServerLevel)$$9;
                            BlockEntity blockentity = blockstate.hasBlockEntity() ? this.world.getBlockEntity(blockpos) : null;
                            LootContext.Builder lootcontext$builder = (new LootContext.Builder(serverlevel)).withRandom(this.world.random).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.exploder);
                            if (this.mode == Explosion.BlockInteraction.DESTROY) {
                                lootcontext$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.size);
                            }

                            blockstate.spawnAfterBreak(serverlevel, blockpos, ItemStack.EMPTY, flag1);
                            blockstate.getDrops(lootcontext$builder).forEach((p_46074_) -> {
                                addBlockDrops(objectarraylist, p_46074_, blockpos1);
                            });
                        }
                    }

                    blockstate.onBlockExploded(this.world, blockpos, this);
                    this.world.getProfiler().pop();
                }
            }

            for(Pair<ItemStack, BlockPos> pair : objectarraylist) {
                Block.popResource(this.world, pair.getSecond(), pair.getFirst());
            }
        }

        if (this.causesFire) {
            for(BlockPos blockpos2 : this.toBlow) {
                if (this.random.nextInt(3) == 0 && this.world.getBlockState(blockpos2).isAir() && this.world.getBlockState(blockpos2.below()).isSolidRender(this.world, blockpos2.below())) {
                    this.world.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.world, blockpos2));
                }
            }
        }
    }

    // The following methods were copied from Explosion.class unchanged.

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> p_46068_, ItemStack p_46069_, BlockPos p_46070_) {
        int i = p_46068_.size();

        for(int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = p_46068_.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, p_46069_)) {
                ItemStack itemstack1 = ItemEntity.merge(itemstack, p_46069_, 16);
                p_46068_.set(j, Pair.of(itemstack1, pair.getSecond()));
                if (p_46069_.isEmpty()) {
                    return;
                }
            }
        }

        p_46068_.add(Pair.of(p_46069_, p_46070_));
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }
}

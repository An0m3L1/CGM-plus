package com.an0m3l1.guns.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An interface for notifying a block it has been destroyed by a projectile explosion.
 * <p>
 * Author: Ocelot
 */
public interface IExplosionDamageable
{
	void onProjectileExploded(Level world, BlockState state, BlockPos pos, Entity entity);
}

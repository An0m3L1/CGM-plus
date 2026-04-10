package com.an0m3l1.guns.interfaces;

import com.an0m3l1.guns.entity.ProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An interface for notifying a block it has been hit by a projectile.
 * <p>
 * Author: MrCrayfish
 */
public interface IDamageable
{
	@SuppressWarnings("EmptyMethod")
	default void onBlockDamaged(Level world, BlockState state, BlockPos pos, ProjectileEntity projectile, float rawDamage, int damage)
	{
	}
}
package com.mrcrayfish.guns.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GrenadeFireHelper
{
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
            if(entity.ignoreExplosion()) continue;

            Vec3 entityPos = entity.getBoundingBox().getCenter();
            double distanceSq = center.distanceToSqr(entityPos);

            if(distanceSq > radiusSq) continue;

            ClipContext context = new ClipContext(
                center,
                entityPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
            );

            if(level.clip(context).getType() == HitResult.Type.MISS)
                entity.setSecondsOnFire(fireDuration);
        }
    }
}
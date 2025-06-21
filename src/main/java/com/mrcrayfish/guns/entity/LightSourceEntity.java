package com.mrcrayfish.guns.entity;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.init.ModEntities;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LightSourceEntity extends Entity implements IEntityAdditionalSpawnData
{
    protected int life;
    private int lightValue;
    private static final Set<EntityType<?>> REGISTERED_TYPES = ConcurrentHashMap.newKeySet();

    public LightSourceEntity(EntityType<? extends Entity> entityType, Level worldIn, int lightValue)
    {
        super(entityType, worldIn);
        this.life = 3;
        this.lightValue = lightValue;
        if (REGISTERED_TYPES.add(entityType)) {
            DynamicLightHandlers.registerDynamicLightHandler(entityType, entity -> ((LightSourceEntity)entity).getLightValue());
        }
    }

    public LightSourceEntity(EntityType<? extends Entity> entityType, Level worldIn)
    {
        this(entityType, worldIn, Config.COMMON.gameplay.dynamicLightValue.get());
    }

    public LightSourceEntity(Level worldIn, double x, double y, double z, int lightValue)
    {
        this(ModEntities.LIGHT_SOURCE.get(), worldIn, lightValue);
        this.setPos(x, y, z);
    }

    public int getLightValue() {
        return this.lightValue;
    }

    @Override
    public void tick()
    {
        super.tick();
        if(this.tickCount >= this.life)
            this.remove(RemovalReason.KILLED);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        this.life = compound.getInt("MaxLife");
        this.lightValue = compound.getInt("LightValue");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putInt("MaxLife", this.life);
        compound.putInt("LightValue", this.lightValue);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(this.life);
        buffer.writeVarInt(this.lightValue);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer)
    {
        this.life = buffer.readVarInt();
        this.lightValue = buffer.readVarInt();
    }

    @Override
    public @NotNull Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
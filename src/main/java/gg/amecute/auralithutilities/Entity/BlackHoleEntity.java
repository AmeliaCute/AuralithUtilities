package gg.amecute.auralithutilities.Entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class BlackHoleEntity extends Entity
{
    private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_INTERIOR_COLOR = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OUTLINE_COLOR = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ROTATION = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);

    private boolean controlled = false;
    private float rotationSpeed = 2.0f;

    public BlackHoleEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SIZE, 1.0f);
        builder.define(DATA_INTERIOR_COLOR, 0xFF000000);
        builder.define(DATA_OUTLINE_COLOR, 0xFFFFFFFF);
        builder.define(DATA_ROTATION, 0.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        if (tag.contains("Size")) setSize(tag.getFloat("Size"));
        if (tag.contains("InteriorColor")) setInteriorColor(tag.getInt("InteriorColor"));
        if (tag.contains("OutlineColor")) setOutlineColor(tag.getInt("OutlineColor"));
        if (tag.contains("Rotation")) setRotation(tag.getFloat("Rotation"));
        if (tag.contains("RotationSpeed")) rotationSpeed = tag.getFloat("RotationSpeed");
        if (tag.contains("Controlled")) controlled = tag.getBoolean("Controlled");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        tag.putFloat("Size", getSize());
        tag.putInt("InteriorColor", getInteriorColor());
        tag.putInt("OutlineColor", getOutlineColor());
        tag.putFloat("Rotation", getRotation());
        tag.putFloat("RotationSpeed", rotationSpeed);
        tag.putBoolean("Controlled", controlled);
    }

    @Override
    public void tick()
    {
        super.tick();

        float currentRotation = getRotation();
        setRotation((currentRotation + rotationSpeed) % 360.0f);
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return false;
    }

    public float getSize()
    {
        return this.entityData.get(DATA_SIZE);
    }

    public void setSize(float size)
    {
        this.entityData.set(DATA_SIZE, Math.max(0.1f, Math.min(25.0f, size)));
        this.refreshDimensions();
    }

    public int getInteriorColor()
    {
        return this.entityData.get(DATA_INTERIOR_COLOR);
    }

    public void setInteriorColor(int argb)
    {
        this.entityData.set(DATA_INTERIOR_COLOR, argb);
    }

    public void setInteriorColor(int r, int g, int b, int a)
    {
        setInteriorColor((a << 24) | (r << 16) | (g << 8) | b);
    }

    public int getOutlineColor()
    {
        return this.entityData.get(DATA_OUTLINE_COLOR);
    }

    public void setOutlineColor(int argb)
    {
        this.entityData.set(DATA_OUTLINE_COLOR, argb);
    }

    public void setOutlineColor(int r, int g, int b, int a)
    {
        setOutlineColor((a << 24) | (r << 16) | (g << 8) | b);
    }

    public float getRotation()
    {
        return this.entityData.get(DATA_ROTATION);
    }

    public void setRotation(float rotation)
    {
        this.entityData.set(DATA_ROTATION, rotation);
    }

    public float getRotationSpeed()
    {
        return rotationSpeed;
    }

    public void setRotationSpeed(float speed)
    {
        this.rotationSpeed = speed;
    }

    public boolean isControlled()
    {
        return controlled;
    }

    public void setControlled(boolean controlled)
    {
        this.controlled = controlled;
    }

    public static class Color
    {
        public final int r, g, b, a;

        public Color(int argb)
        {
            this.a = (argb >> 24) & 0xFF;
            this.r = (argb >> 16) & 0xFF;
            this.g = (argb >> 8) & 0xFF;
            this.b = argb & 0xFF;
        }

        public float rf() { return r / 255.0f; }
        public float gf() { return g / 255.0f; }
        public float bf() { return b / 255.0f; }
        public float af() { return a / 255.0f; }
    }
}
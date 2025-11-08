package gg.amecute.auralithutilities.Entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.math.BigInteger;

import static org.lwjgl.system.linux.X11.True;

public class BlackHoleEntity extends Entity implements GeoEntity
{
    private static final EntityDataAccessor<Float>   DATA_SIZE   = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_STABLE = SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean controlled  = false;
    private float   mass        = 100.0f;
    private Long    age         = 0L;
    private float   temperature = 1.0f;

    public BlackHoleEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);

        this.noPhysics = true;
        this.setNoGravity(true);
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(new AnimationController<>(this, "controller", 0, state->
                state.setAndContinue(RawAnimation.begin().thenLoop("idle"))));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(DATA_SIZE, 2.0f);
        builder.define(DATA_STABLE, true);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag)
    {
        if(compoundTag.contains("Size")) setSize(compoundTag.getFloat("Size"));

        if(compoundTag.contains("Stable")) setStable(compoundTag.getBoolean("Stable"));

        if(compoundTag.contains("Controlled")) setControlled(compoundTag.getBoolean("Controlled"));

        if(compoundTag.contains("Mass")) mass = (compoundTag.getFloat("Mass"));

        if(compoundTag.contains("Age")) age = (compoundTag.getLong("Age"));

        if(compoundTag.contains("Temperature")) setTemperature(compoundTag.getFloat("Temperature"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag)
    {
        compoundTag.putFloat("Size", getSize());
        compoundTag.putBoolean("Stable", isStable());
        compoundTag.putBoolean("Controlled", isControlled());
        compoundTag.putFloat("Mass", getMass());
        compoundTag.putLong("Age", getAge());
        compoundTag.putFloat("Temperature", getTemperature());
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if(!isStable())
        {
            float pulseSpeed = 1f;
            float pulseAmount = (float) Math.sin(age * pulseSpeed) * 0.3f;
            float baseSize = getSize();
            float newSize = baseSize + pulseAmount * .25f;
            setSize(newSize);

            temperature = 1.0f / baseSize;
        }
        if(!isControlled() && !level().isClientSide) applyGravitationalPull();
    }

    public void applyGravitationalPull()
    {
        float pullRadius  = getSize() * 5.0f;
        float pullStrengh = mass / 50.0f;

        level().getEntities(this, getBoundingBox().inflate(pullRadius),
                entity -> entity != this && !entity.isSpectator() && !entity.isSprinting()).forEach(
                entity ->
                {
                    Vec3 toBlackHole = this.position().subtract(entity.position());
                    double distance = Math.max(toBlackHole.length(), 0.2); // min distance = 0.2 to prevent division by 0
                    Vec3 direction = toBlackHole.normalize();
                    double force = pullStrengh / (distance * distance);
                    Vec3 pullVelocity = direction.scale(force * 0.5);

                    if(entity instanceof Player player)
                    {
                        Vec3 newVel = player.getDeltaMovement().add(pullVelocity);
                        player.setDeltaMovement(newVel);
                        player.hurtMarked = true;
                    }
                    else
                        entity.setDeltaMovement(entity.getDeltaMovement().add(pullVelocity));
                }
        );
    }

    @Override
    public boolean isPickable()
    {
        return true;
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

    public boolean isStable()
    {
        return this.entityData.get(DATA_STABLE);
    }

    public void setStable(boolean stable)
    {
        this.entityData.set(DATA_STABLE, stable);
    }

    public boolean isControlled()
    {
        return controlled;
    }

    public void setControlled(boolean controlled)
    {
        this.controlled = controlled;
    }

    public float getMass()
    {
        return mass;
    }

    public long getAge()
    {
        return age;
    }

    public float getTemperature()
    {
        return temperature;
    }

    public void setTemperature(float temperature)
    {
        this.temperature = temperature;
    }


}

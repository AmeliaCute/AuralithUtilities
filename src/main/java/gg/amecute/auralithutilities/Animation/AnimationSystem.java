package gg.amecute.auralithutilities.Animation;

import aztech.modern_industrialization.machines.MachineComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public abstract class AnimationSystem implements MachineComponent {
    protected final Level level;
    protected final Vec3 origin;
    protected UUID uuid;

    public AnimationSystem(Level level, Vec3 position)
    {
        this.level = level;
        this.origin = position;
    }

    public abstract void startAnimation();
    public abstract void stopAnimation();
    public abstract void updateAnimation(float tick);
    public abstract void rebindEntity(UUID entityUuid);

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries)
    {
        if(this.uuid != null) tag.putUUID("entity_uuid", this.uuid);
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine)
    {
        if(tag.contains("entity_uuid")) this.uuid = tag.getUUID("entity_uuid");
    }

    public UUID getUuid()
    {
        return this.uuid;
    }
}

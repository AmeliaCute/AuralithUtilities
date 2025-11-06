package gg.amecute.auralithutilities.Polution;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PollutionDeterminator
{
    public static boolean isActiveMIMachine(BlockEntity blockEntity)
    {
        if(!(blockEntity instanceof MachineBlockEntity mbe)) return false;

        IsActiveComponent activeComponent = mbe.components.get(IsActiveComponent.class);
        return activeComponent != null && activeComponent.isActive;
    }
}

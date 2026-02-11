package gg.amecute.auralithutilities.Multiblock;

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import gg.amecute.auralithutilities.Animation.AnimationSystem;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class AuralithMultiblock extends AbstractElectricCraftingMultiblockBlockEntity
{
    private boolean isAnimating = false;
    private AnimationSystem animationType;

    private final MachineCasing casing;
    private final List<ResourceLocation> workInWorld;

    public AuralithMultiblock(BEP bep, ResourceLocation name, OrientationComponent.Params params, MachineCasing hatchCasing,List<ResourceLocation> workInWorld, ShapeTemplate shape)
    {
        super(
                bep,
                name,
                params,
                new ShapeTemplate[]{ shape }
        );

        this.casing = hatchCasing;
        this.workInWorld = workInWorld;
    }

    protected abstract AnimationSystem createAnimationSystem();

    @Override
    public void tickExtra()
    {
        super.tickExtra();

        if(level != null && !level.isClientSide)
        {
            if(crafter.getProgress() > 0)
            {
                if (!isAnimating) {
                    if(animationType != null && animationType.getUuid() != null)
                    {
                        animationType.rebindEntity(animationType.getUuid());
                    } else
                    {
                        animationType = createAnimationSystem();
                        registerComponents(animationType);

                        animationType.startAnimation();
                    }

                    isAnimating = true;
                }

                animationType.updateAnimation(crafter.getProgress());
            }
            else if(isAnimating)
            {
                isAnimating = false;
                animationType.stopAnimation();
            }
        }
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        if(animationType != null && isAnimating) animationType.stopAnimation();
    }
}

package gg.amecute.auralithutilities.Multiblock;

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.Animation.Impl.BlackHoleCraftingAnim;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public abstract class AuralithMultiblock extends AbstractElectricCraftingMultiblockBlockEntity
{
    private int animationTick = 0;
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
    public void tickExtra() {
        super.tickExtra();

        if(level != null && !level.isClientSide)
        {
            if(crafter.getProgress() > 0)
            {
                if (!isAnimating)
                {
                    if(animationType == null) animationType = createAnimationSystem();

                    isAnimating = true;
                    animationType.startAnimation();
                }

                animationType.updateAnimation(crafter.getProgress());
                animationTick++;
            }
            else if(isAnimating)
            {
                isAnimating = false;
                animationType.stopAnimation();
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(animationType != null && isAnimating) animationType.startAnimation();
    }
}

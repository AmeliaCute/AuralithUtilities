package gg.amecute.auralithutilities.Multiblock;

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.Animation.Impl.MatterCraftingAnim;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Multiblock.ShapeTemplate.BlackHoleCrafterShape;
import gg.amecute.auralithutilities.Registries.AuralithRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class BlackHoleCrafter extends AuralithMultiblock {

    public BlackHoleCrafter(BEP bep, MachineCasing hatchCasing) {
        super(
                bep,
                ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "black_hole_crafter"),
                new OrientationComponent.Params(true, true, false, false),
                hatchCasing,
                null,
                BlackHoleCrafterShape.get()
        );
    }

    @Override
    public MachineRecipeType recipeType() {
        return AuralithRecipeType.MATTER_TRANSFORMER;
    }

    @Override
    public long getBaseRecipeEu() {
        return 1024;
    }

    @Override
    public long getMaxRecipeEu() {
        return 124000;
    }

    @Override
    protected AnimationSystem createAnimationSystem()
    {
        Vec3 controllerCenter = this.getBlockPos().getCenter();
        return new MatterCraftingAnim(
                this.level,
                new Vec3(controllerCenter.x,controllerCenter.y + 12, controllerCenter.z),
                1.2f
        );
    }



}

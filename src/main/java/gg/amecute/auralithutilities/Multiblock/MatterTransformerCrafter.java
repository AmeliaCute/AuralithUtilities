package gg.amecute.auralithutilities.Multiblock;

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.Animation.Impl.MatterCraftingAnim;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Multiblock.ShapeTemplate.MatterTransformerCrafterShape;
import gg.amecute.auralithutilities.Registries.AuralithRecipeType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class MatterTransformerCrafter extends AuralithMultiblock
{
    private static final ResourceLocation STRUCTURE_ID = ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "matter_transformer_crafter");

    private int interiorColor = 0xFF000000;
    private int outlineColor = 0xFFFFFFFF;

    public MatterTransformerCrafter(BEP bep, MachineCasing hatchCasing)
    {
        super(
            bep,
            STRUCTURE_ID,
            new OrientationComponent.Params(true, true, false, false),
            hatchCasing,
            MatterTransformerCrafterShape.get()
        );

        if (structure != null && structure.animationConfig().isPresent())
        {
            var animConfig = structure.animationConfig().get();
            var params = animConfig.parameters();

            if (params.containsKey("interior_color"))
            {
                interiorColor = parseColor((String) params.get("interior_color"));
            }
            if (params.containsKey("outline_color"))
            {
                outlineColor = parseColor((String) params.get("outline_color"));
            }
        }
    }

    @Override
    public MachineRecipeType recipeType()
    {
        return AuralithRecipeType.MATTER_TRANSFORMER;
    }

    @Override
    public long getBaseRecipeEu()
    {
        if (structure != null) return structure.recipeConfig().baseEnergyUsage();
        return 1024;
    }

    @Override
    public long getMaxRecipeEu()
    {
        if (structure != null) return structure.recipeConfig().maxEnergyUsage();
        return 124000;
    }

    @Override
    protected AnimationSystem createAnimationSystem()
    {
        Vec3 controllerCenter = this.getBlockPos().getCenter();
        Vec3 animationPos = new Vec3(controllerCenter.x, controllerCenter.y + 12, controllerCenter.z);

        if (structure != null && structure.animationConfig().isPresent())
        {
            var animConfig = structure.animationConfig().get();
            var offset = animConfig.offset();

            animationPos = new Vec3(
                controllerCenter.x + offset.x(),
                controllerCenter.y + offset.y(),
                controllerCenter.z + offset.z()
            );
        }

        return new MatterCraftingAnim(this.level, animationPos, 1.2f, interiorColor, outlineColor);
    }

    private int parseColor(String colorStr)
    {
        try
        {
            if (colorStr.startsWith("#"))
            {
                colorStr = colorStr.substring(1);
                if (colorStr.length() == 6) colorStr = "FF" + colorStr;
                return (int) Long.parseLong(colorStr, 16);
            }
            else if (colorStr.startsWith("0x")) return (int) Long.parseLong(colorStr.substring(2), 16);
            return Integer.parseInt(colorStr);
        }
        catch (NumberFormatException e)
        {
            return 0xFFFFFFFF;
        }
    }
}
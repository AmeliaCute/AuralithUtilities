package gg.amecute.auralithutilities.Multiblock;

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockStructure;
import gg.amecute.auralithutilities.Multiblock.Data.ShapeConverter;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public abstract class AuralithMultiblock extends AbstractElectricCraftingMultiblockBlockEntity
{
    private boolean isAnimating = false;
    private AnimationSystem animationType;

    protected final ResourceLocation structureId;
    protected final MultiblockStructure structure;
    protected final MachineCasing casing;

    public AuralithMultiblock(
        BEP bep,
        ResourceLocation structureId,
        OrientationComponent.Params params,
        MachineCasing hatchCasing,
        ShapeTemplate fallbackShape
    )
    {
        super(bep, structureId, params, new ShapeTemplate[]{ buildShape(structureId, fallbackShape) });

        this.structureId = structureId;
        this.casing = hatchCasing;
        this.structure = loadStructureFromManager(structureId).orElse(null);
    }

    private static ShapeTemplate buildShape(ResourceLocation structureId, ShapeTemplate fallback)
    {
        Optional<MultiblockStructure> structureOpt = loadStructureFromManager(structureId);

        if (structureOpt.isPresent())
        {
            try
            {
                return ShapeConverter.convert(structureOpt.get());
            }
            catch (Exception e)
            {
                AuralithUtilities.LOGGER.error("Failed to convert structure {}, using fallback", structureId, e);
            }
        }

        return fallback;
    }

    private static Optional<MultiblockStructure> loadStructureFromManager(ResourceLocation id)
    {
        var manager = AuralithUtilities.getStructureManager();
        if (manager != null) return manager.getStructure(id);

        return Optional.empty();
    }

    protected abstract AnimationSystem createAnimationSystem();

    @Override
    public void tickExtra()
    {
        super.tickExtra();

        if (level != null && !level.isClientSide)
        {
            if (crafter.getProgress() > 0)
            {
                if (!isAnimating)
                {
                    if (animationType != null && animationType.getUuid() != null) animationType.rebindEntity(animationType.getUuid());
                    else
                    {
                        animationType = createAnimationSystem();
                        if (animationType != null)
                        {
                            registerComponents(animationType);
                            animationType.startAnimation();
                        }
                    }
                    isAnimating = true;
                }

                if (animationType != null) animationType.updateAnimation(crafter.getProgress());
            }
            else if (isAnimating)
            {
                isAnimating = false;
                if (animationType != null) animationType.stopAnimation();
            }
        }
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        if (animationType != null && isAnimating) animationType.stopAnimation();
    }

    public MultiblockStructure getStructure()
    {
        return structure;
    }
}
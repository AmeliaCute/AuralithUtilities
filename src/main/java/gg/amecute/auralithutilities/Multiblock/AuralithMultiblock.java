package gg.amecute.auralithutilities.Multiblock;

import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractElectricCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import gg.amecute.auralithutilities.Animation.AnimationSystem;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockStructure;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockStructureManager;
import gg.amecute.auralithutilities.Multiblock.Data.ShapeConverter;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AuralithMultiblock extends AbstractElectricCraftingMultiblockBlockEntity
{
    private boolean isAnimating = false;
    private AnimationSystem animationType;

    protected final ResourceLocation structureId;
    protected MultiblockStructure structure;
    protected final MachineCasing casing;

    private static final Map<ResourceLocation, ShapeTemplate[]> SHAPE_REGISTRY = new ConcurrentHashMap<>();

    public AuralithMultiblock(
        BEP bep,
        ResourceLocation structureId,
        OrientationComponent.Params params,
        MachineCasing hatchCasing
    )
    {
        super(bep, structureId, params, getOrCreateShapes(structureId));

        this.structureId = structureId;
        this.casing = hatchCasing;
        this.structure = loadStructureFromManager(structureId).orElse(null);
    }

    private static ShapeTemplate[] getOrCreateShapes(ResourceLocation structureId)
    {
        return SHAPE_REGISTRY.computeIfAbsent(structureId, id -> {
            Optional<MultiblockStructure> structureOpt = loadStructureFromManager(id);

            if (structureOpt.isPresent())
            {
                try
                {
                    ShapeTemplate converted = ShapeConverter.convert(structureOpt.get());
                    AuralithUtilities.LOGGER.info("Successfully loaded shape from datapack for: {}", id);
                    return new ShapeTemplate[] { converted };
                }
                catch (Exception e)
                {
                    AuralithUtilities.LOGGER.error("Failed to convert structure {}", id, e);
                }
            }
            else
            {
                AuralithUtilities.LOGGER.debug("Structure {} not yet loaded, creating empty shape placeholder", id);
            }

            return new ShapeTemplate[] { createEmptyShape() };
        });
    }

    private static ShapeTemplate createEmptyShape()
    {
        return new ShapeTemplate.Builder(MachineCasings.STEEL).build();
    }

    public static void reloadAllShapes(MultiblockStructureManager manager)
    {
        AuralithUtilities.LOGGER.info("Reloading all multiblock shapes from datapack...");

        for (MultiblockStructure structure : manager.getAllStructures())
        {
            ResourceLocation id = structure.id();

            try
            {
                ShapeTemplate converted = ShapeConverter.convert(structure);
                SHAPE_REGISTRY.put(id, new ShapeTemplate[] { converted });
                AuralithUtilities.LOGGER.info("Reloaded shape for: {}", id);
            }
            catch (Exception e)
            {
                AuralithUtilities.LOGGER.error("Failed to reload shape for {}", id, e);
            }
        }

        AuralithUtilities.LOGGER.info("Shape reload complete: {} shapes updated", SHAPE_REGISTRY.size());
    }

    public void updateStructure()
    {
        Optional<MultiblockStructure> newStructure = loadStructureFromManager(structureId);

        if (newStructure.isPresent())
        {
            structure = newStructure.get();
            AuralithUtilities.LOGGER.debug("Updated structure data for {}", structureId);
        }
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
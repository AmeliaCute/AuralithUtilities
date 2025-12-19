package gg.amecute.auralithutilities.Registries;

import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.init.MachineDefinition;
import aztech.modern_industrialization.machines.init.MachineRegistrationHelper;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.models.MachineCasings;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Block.MatterTransformerCrafterBlock;
import gg.amecute.auralithutilities.Multiblock.MatterTransformerCrafter;
import gg.amecute.auralithutilities.Multiblock.ShapeTemplate.MatterTransformerCrafterShape;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AuralithMachines
{
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, AuralithUtilities.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AuralithUtilities.MODID);

    public static final Supplier<Block> MATTER_TRANSFORMER_CRAFTER_BLOCK =
            BLOCKS.register("matter_transformer_crafter", () ->
            {
                return new MatterTransformerCrafterBlock(BlockBehaviour.Properties.of().sound(SoundType.LODESTONE));
            });


    public static MachineDefinition<MachineBlockEntity> MATTER_TRANSFORMER_CRAFTER_BE =
            MachineRegistrationHelper.registerMachine(
                    "Matter transformer crafter",
                    "matter_transformer_crafter",
                    bet -> new MatterTransformerCrafter(bet, MachineCasings.STEEL)
            );

    public static void registerBlockEntities()
    {
    }
}

package gg.amecute.auralithutilities.Registries;

import aztech.modern_industrialization.machines.init.MachineRegistrationHelper;
import aztech.modern_industrialization.machines.models.MachineCasings;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Block.BlackHoleCrafterBlock;
import gg.amecute.auralithutilities.Multiblock.BlackHoleCrafter;
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

    public static final Supplier<Block> BLACK_HOLE_CRAFTER_BLOCK =
            BLOCKS.register("black_hole_crafter", () ->
            {
                return new BlackHoleCrafterBlock(BlockBehaviour.Properties.of().sound(SoundType.LODESTONE));
            });


    public static Supplier<BlockEntityType<?>> BLACK_HOLE_CRAFTER_BE;

    public static void registerBlockEntities() {
        BLACK_HOLE_CRAFTER_BE =
                MachineRegistrationHelper.registerMachine(
                        "Black hole crafter",
                        "black_hole_crafter",
                        bet -> new BlackHoleCrafter(bet, MachineCasings.STEEL)
                );
    }
}

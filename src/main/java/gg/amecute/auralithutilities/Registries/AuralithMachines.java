package gg.amecute.auralithutilities.Registries;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.init.MachineDefinition;
import aztech.modern_industrialization.machines.init.MachineRegistrationHelper;
import aztech.modern_industrialization.machines.models.MachineCasings;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Block.MatterTransformerCrafterBlock;
import gg.amecute.auralithutilities.Block.MortarBlock;
import gg.amecute.auralithutilities.Item.PestleItem;
import gg.amecute.auralithutilities.Multiblock.MatterTransformerCrafter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AuralithMachines {

  public static final DeferredRegister<Block> BLOCKS =
      DeferredRegister.create(Registries.BLOCK, AuralithUtilities.MODID);

  public static final Supplier<Block> MATTER_TRANSFORMER_CRAFTER_BLOCK =
      BLOCKS.register("matter_transformer_crafter", () ->
          new MatterTransformerCrafterBlock(BlockBehaviour.Properties.of().sound(SoundType.LODESTONE)));

  public static final Supplier<Block> FARMING_STATION =
      BLOCKS.register("farming_station_controler", () ->
          new Block(BlockBehaviour.Properties.of().sound(SoundType.LODESTONE)));

  public static final Supplier<Block> MORTAR_BLOCK =
      BLOCKS.register("mortar", () ->
          new MortarBlock(BlockBehaviour.Properties.of()
              .sound(SoundType.STONE)
              .strength(1.5f, 6.0f)
              .requiresCorrectToolForDrops()
              .noOcclusion()   // custom shape, don't occlude neighbours
          ));

  public static final DeferredRegister<Item> MACHINE_ITEMS =
      DeferredRegister.create(Registries.ITEM, AuralithUtilities.MODID);

  public static final Supplier<Item> MORTAR_ITEM =
      MACHINE_ITEMS.register("mortar", () ->
          new BlockItem(MORTAR_BLOCK.get(), new Item.Properties()));

  public static final Supplier<Item> PESTLE_ITEM =
      MACHINE_ITEMS.register("pestle", PestleItem::new);

  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AuralithUtilities.MODID);

  public static MachineDefinition<MachineBlockEntity> MATTER_TRANSFORMER_CRAFTER_BE =
      MachineRegistrationHelper.registerMachine(
          "Matter transformer crafter",
          "matter_transformer_crafter",
          bet -> new MatterTransformerCrafter(bet, MachineCasings.STEEL)
      );

  public static void registerBlockEntities() {
  }
}
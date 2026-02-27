package gg.amecute.auralithutilities.Registries;

import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Block.Entity.MortarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AuralithBlockEntities {

  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
    DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AuralithUtilities.MODID);

  public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MortarBlockEntity>> MORTAR =
    BLOCK_ENTITY_TYPES.register("mortar", () ->
      BlockEntityType.Builder
        .of(MortarBlockEntity::new, AuralithMachines.MORTAR_BLOCK.get())
        .build(null)
    );

  public static void register(IEventBus modEventBus) 
  {
    BLOCK_ENTITY_TYPES.register(modEventBus);
  }
}
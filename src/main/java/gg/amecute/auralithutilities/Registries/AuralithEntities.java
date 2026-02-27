package gg.amecute.auralithutilities.Registries;

import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Entity.BlackHoleEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AuralithEntities
{
  public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(Registries.ENTITY_TYPE, AuralithUtilities.MODID);

  public static final DeferredHolder<EntityType<?>, EntityType<BlackHoleEntity>> BLACK_HOLE =
    ENTITY_TYPE.register("black_hole", () -> EntityType.Builder.<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
      .sized(2.0f, 2.0f)
      .clientTrackingRange(10)
      .updateInterval(1)
      .build("black_hole")
    );
}

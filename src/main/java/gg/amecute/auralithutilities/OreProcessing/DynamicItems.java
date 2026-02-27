package gg.amecute.auralithutilities.OreProcessing;

import gg.amecute.auralithutilities.AuralithUtilities;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

public final class DynamicItems 
{

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, AuralithUtilities.MODID);

  private static final Map<String, DeferredHolder<Item, Item>> REGISTRY = new HashMap<>();

  static 
  {
    for (OreProcessingEntry entry : OreProcessingRegistry.getEntries()) 
      tryRegister(entry.crushed());
      
    AuralithUtilities.LOGGER.info("[DynamicItems] Queued {} dynamic item(s).", REGISTRY.size());
  }

  private DynamicItems() {}

  private static void tryRegister(String itemId) 
  {
    if (itemId == null || itemId.isBlank()) return;

    ResourceLocation rl;
    try 
    {
      rl = ResourceLocation.parse(itemId);
    } catch (Exception e) 
    {
      AuralithUtilities.LOGGER.warn("[DynamicItems] Invalid item id '{}', skipping.", itemId);
      return;
    }

    if (!rl.getNamespace().equals(AuralithUtilities.MODID)) return;

    if (REGISTRY.containsKey(rl.getPath())) return;

    DeferredHolder<Item, Item> holder = ITEMS.register(
        rl.getPath(),
        () -> new Item(new Item.Properties())
    );
    REGISTRY.put(rl.getPath(), holder);
    AuralithUtilities.LOGGER.debug("[DynamicItems] Queued: {}", itemId);
  }

  public static Item getItem(String fullItemId) 
  {
    ResourceLocation rl = ResourceLocation.parse(fullItemId);
    if (!rl.getNamespace().equals(AuralithUtilities.MODID)) return null;

    DeferredHolder<Item, Item> holder = REGISTRY.get(rl.getPath());
    return holder != null ? holder.get() : null;
  }

  public static boolean isDynamic(String fullItemId) 
  {
    try {
      ResourceLocation rl = ResourceLocation.parse(fullItemId);
      return rl.getNamespace().equals(AuralithUtilities.MODID) && REGISTRY.containsKey(rl.getPath());
    } catch (Exception e) {
      return false;
    }
  }
}
package gg.amecute.auralithutilities.REI.Mortar;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.OreProcessing.OreProcessingEntry;
import gg.amecute.auralithutilities.OreProcessing.OreProcessingRegistry;
import gg.amecute.auralithutilities.Registries.AuralithMachines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@EmiEntrypoint
public class MortarEmiPlugin implements EmiPlugin
{
  public static final EmiRecipeCategory MORTAR_CATEGORY = new EmiRecipeCategory(
      ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "mortar"),
      EmiStack.of(new ItemStack(AuralithMachines.MORTAR_BLOCK.get()))
  );

  @Override
  public void register(EmiRegistry registry)
  {
    registry.addCategory(MORTAR_CATEGORY);
    registry.addWorkstation(MORTAR_CATEGORY, EmiStack.of(new ItemStack(AuralithMachines.MORTAR_BLOCK.get())));

    for(OreProcessingEntry entry : OreProcessingRegistry.getEntries())
    for(String rawId : entry.rawItems())
    {
      if(rawId == null || rawId.isBlank()) continue;

      ResourceLocation rawRl = ResourceLocation.tryParse(rawId);
      if(rawRl == null) continue;

      var rawItem = BuiltInRegistries.ITEM.getOptional(rawRl);
      if(rawItem.isEmpty()) continue;

      ResourceLocation crushedRl = ResourceLocation.tryParse(entry.crushed());
      if(crushedRl == null) continue;

      var crushedItem = BuiltInRegistries.ITEM.getOptional(crushedRl);
      if(crushedItem.isEmpty()) continue;

      registry.addRecipe(new MortarEmiRecipe(entry, entry.metal(), new ItemStack(rawItem.get()), new ItemStack(crushedItem.get(), 2)));
    }
  }
}

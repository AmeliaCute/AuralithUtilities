package gg.amecute.auralithutilities.REI.Mortar;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.OreProcessing.OreProcessingEntry;
import gg.amecute.auralithutilities.Registries.AuralithItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MortarEmiRecipe implements EmiRecipe {
  private final ResourceLocation id;
  private final List<EmiIngredient> inputs;
  private final List<EmiStack> outputs;
  private final OreProcessingEntry entry;

  public MortarEmiRecipe(OreProcessingEntry entry, String rawId, ItemStack rawStack, ItemStack crushedStack)
  {
    this.entry = entry;
    this.id = ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "mortar/"+rawId.replace(':', '_'));
    this.inputs = List.of(
        EmiIngredient.of(List.of(EmiStack.of(rawStack))),
        EmiIngredient.of(List.of(EmiStack.of(new ItemStack(AuralithItems.PESTLE.get()))))
    );
    this.outputs = List.of(EmiStack.of(crushedStack));
  }

  @Override
  public EmiRecipeCategory getCategory() { return MortarEmiPlugin.MORTAR_CATEGORY; }

  @Override
  public @Nullable ResourceLocation getId() { return id; }

  @Override
  public List<EmiIngredient> getInputs() { return inputs; }

  @Override
  public List<EmiStack> getOutputs() { return outputs; }

  @Override
  public int getDisplayWidth() { return 118; }

  @Override
  public int getDisplayHeight() { return 54; }

  @Override
  public void addWidgets(WidgetHolder widgets)
  {
    widgets.addSlot(inputs.get(0), 0, 19);
    widgets.addSlot(inputs.get(1), 36, 19).drawBack(false);
    widgets.addFillingArrow(63, 21, 1500);
    widgets.addSlot(outputs.get(0), 94, 19).recipeContext(this);
  }
}

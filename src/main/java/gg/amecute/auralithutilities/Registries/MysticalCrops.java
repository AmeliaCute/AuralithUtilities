package gg.amecute.auralithutilities.Registries;

import com.blakebr0.mysticalagradditions.lib.ModCropTiers;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import gg.amecute.auralithutilities.AuralithUtilities;
import net.neoforged.fml.ModList;

import java.util.Arrays;

public final class MysticalCrops
{
    public static final Crop ANTIMONY =
            new Crop(AuralithUtilities.resGet("antimony"), CropTier.FOUR, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:antimony_ingot"));

    public static final Crop CHROMIUM_HOT =
            new Crop(AuralithUtilities.resGet("chromium_hot"), ModCropTiers.SIX, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:chromium_hot_ingot"));

    public static final Crop KANTHAL_HOT =
            new Crop(AuralithUtilities.resGet("kanthal_hot"), ModCropTiers.SIX, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:kanthal_hot_ingot"));

    public static final Crop SUPERCONDUCTOR_HOT =
            new Crop(AuralithUtilities.resGet("superconductor_hot"), ModCropTiers.SIX, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:superconductor_hot_ingot"));

    public static final Crop TITANIUM_HOT =
            new Crop(AuralithUtilities.resGet("titanium_hot"), ModCropTiers.SIX, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:titanium_hot_ingot"));

    public static final Crop TUNGSTEN =
            new Crop(AuralithUtilities.resGet("tungsten"), ModCropTiers.SIX, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:tungsten_ingot"));

    public static final Crop MANGANESE_DUST =
            new Crop(AuralithUtilities.resGet("manganese_dust"), CropTier.FOUR, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:manganese_dust"));

    public static final Crop BAUXITE_DUST =
            new Crop(AuralithUtilities.resGet("bauxite_dust"), CropTier.FOUR, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:bauxite_dust"));

    public static void onRegisterCrops(ICropRegistry registry)
    {
        registry.register(withRequiredMods(ANTIMONY, "modern_industrialization"));
        registry.register(withRequiredMods(TUNGSTEN, "modern_industrialization"));

        registry.register(withRequiredMods(CHROMIUM_HOT, "modern_industrialization"));
        registry.register(withRequiredMods(KANTHAL_HOT, "modern_industrialization"));
        registry.register(withRequiredMods(SUPERCONDUCTOR_HOT, "modern_industrialization"));
        registry.register(withRequiredMods(TITANIUM_HOT, "modern_industrialization"));

        registry.register(withRequiredMods(MANGANESE_DUST, "modern_industrialization"));
        registry.register(withRequiredMods(BAUXITE_DUST, "modern_industrialization"));
    }

    private static Crop withRequiredMods(Crop crop, String... mods)
    {
        boolean enabled = Arrays.stream(mods).anyMatch(ModList.get()::isLoaded);
        return crop.setEnabled(enabled);
    }
}

package gg.amecute.auralithutilities.Mystical;

import com.blakebr0.mysticalagradditions.lib.ModCropTiers;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import gg.amecute.auralithutilities.AuralithUtilities;
import net.neoforged.fml.ModList;

import java.util.Arrays;

public final class ModCrops
{
    public static final Crop ANTIMONY =
            new Crop(AuralithUtilities.resGet("antimony"), CropTier.FOUR, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:antimony_ingot"));

    public static final Crop TITANIUM_HOT =
            new Crop(AuralithUtilities.resGet("titanium_hot"), ModCropTiers.SIX, CropType.RESOURCE, LazyIngredient.item("modern_industrialization:titanium_hot_ingot"));

    public static void onRegisterCrops(ICropRegistry registry)
    {
        registry.register(withRequiredMods(ANTIMONY, "modern_industrialization"));
        registry.register(withRequiredMods(TITANIUM_HOT, "modern_industrialization"));
    }

    private static Crop withRequiredMods(Crop crop, String... mods)
    {
        boolean enabled = Arrays.stream(mods).anyMatch(ModList.get()::isLoaded);
        return crop.setEnabled(enabled);
    }
}

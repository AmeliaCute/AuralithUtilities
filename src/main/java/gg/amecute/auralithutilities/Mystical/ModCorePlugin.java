package gg.amecute.auralithutilities.Mystical;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.MysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.lib.PluginConfig;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import gg.amecute.auralithutilities.Registries.MysticalCrops;

import static com.blakebr0.mysticalagriculture.MysticalAgriculture.MOD_ID;

@MysticalAgriculturePlugin
public class ModCorePlugin implements IMysticalAgriculturePlugin
{
    @Override
    public void configure(PluginConfig config)
    {
        config.setModId(MOD_ID);
        config.disableDynamicSeedCraftingRecipes();
        config.disableDynamicSeedReprocessingRecipes();
        config.disableDynamicSeedInfusionRecipes();
    }

    @Override
    public void onRegisterCrops(ICropRegistry registry) {
        MysticalCrops.onRegisterCrops(registry);
    }
}
package gg.amecute.auralithutilities;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = AuralithUtilities.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AuralithUtilities.MODID, value = Dist.CLIENT)
public class AuralithUtilitiesClient {
    public AuralithUtilitiesClient(ModContainer container) {

    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }
}

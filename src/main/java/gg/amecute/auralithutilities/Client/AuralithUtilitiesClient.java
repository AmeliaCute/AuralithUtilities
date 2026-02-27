package gg.amecute.auralithutilities.Client;

import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Client.Renderer.BlackHoleEntityRenderer;
import gg.amecute.auralithutilities.Client.Renderer.MortarBlockEntityRenderer;
import gg.amecute.auralithutilities.Registries.AuralithBlockEntities;
import gg.amecute.auralithutilities.Registries.AuralithEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = AuralithUtilities.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AuralithUtilities.MODID, value = Dist.CLIENT)
public class AuralithUtilitiesClient 
{

    public AuralithUtilitiesClient(ModContainer container) {}

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) 
    {
        EntityRenderers.register(AuralithEntities.BLACK_HOLE.get(), BlackHoleEntityRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) 
    {
        event.registerBlockEntityRenderer(AuralithBlockEntities.MORTAR.get(), MortarBlockEntityRenderer::new);
    }
}
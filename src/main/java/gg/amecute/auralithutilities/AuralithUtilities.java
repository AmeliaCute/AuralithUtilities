package gg.amecute.auralithutilities;

import gg.amecute.auralithutilities.Event.MainMenuReplacer;
import gg.amecute.auralithutilities.Registries.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(AuralithUtilities.MODID)
public class AuralithUtilities
{
    public static final String MODID = "auralithutilities";

    public AuralithUtilities(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(MainMenuReplacer.class);

        AuralithRecipeType.register();


        AuralithEntities.ENTITY_TYPE.register(modEventBus);
        AuralithItems.ITEMS.register(modEventBus);

        AuralithMachines.BLOCKS.register(modEventBus);
        AuralithMachines.registerBlockEntities();

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> AuralithREI.init());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) 
    {
    }

    public static ResourceLocation resGet(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

}

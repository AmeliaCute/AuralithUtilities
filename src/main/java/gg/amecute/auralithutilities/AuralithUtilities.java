package gg.amecute.auralithutilities;

import gg.amecute.auralithutilities.Debug.PollutionCommand;
import gg.amecute.auralithutilities.MainMenu.MainMenuHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(AuralithUtilities.MODID)
public class AuralithUtilities
{
    public static final String MODID = "auralithutilities";

    public AuralithUtilities(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(MainMenuHandler.class);

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    @SubscribeEvent
    private void onRegisterCommands(RegisterCommandsEvent event)
    {
        PollutionCommand.register(event.getDispatcher());
    }

    public static ResourceLocation resGet(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

}

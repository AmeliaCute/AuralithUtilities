package gg.amecute.auralithutilities;

import gg.amecute.auralithutilities.Command.MultiblockCommands;
import gg.amecute.auralithutilities.Config.ClientConfig;
import gg.amecute.auralithutilities.Config.CommonConfig;
import gg.amecute.auralithutilities.Event.MainMenuReplacer;
import gg.amecute.auralithutilities.Multiblock.Data.MultiblockStructureManager;
import gg.amecute.auralithutilities.OreProcessing.OreProcessingManager;
import gg.amecute.auralithutilities.Registries.AuralithBlockEntities;
import gg.amecute.auralithutilities.Registries.AuralithEntities;
import gg.amecute.auralithutilities.Registries.AuralithItems;
import gg.amecute.auralithutilities.Registries.AuralithMachines;
import gg.amecute.auralithutilities.Registries.AuralithRecipeType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AuralithUtilities.MODID)
public class AuralithUtilities {

    private static MultiblockStructureManager structureManager;
    public static final Logger LOGGER = LoggerFactory.getLogger(AuralithUtilities.class);
    public static final String MODID = "auralithcore";

    public AuralithUtilities(IEventBus modEventBus, ModContainer modContainer)
    {
      modEventBus.addListener(this::commonSetup);

      NeoForge.EVENT_BUS.addListener(this::onServerStarting);
      NeoForge.EVENT_BUS.addListener(AuralithUtilities::onAddReloadListener);
      NeoForge.EVENT_BUS.addListener(AuralithUtilities::onRegisterCommands);
      NeoForge.EVENT_BUS.register(MainMenuReplacer.class);

      AuralithRecipeType.register();
      AuralithEntities.ENTITY_TYPE.register(modEventBus);

      AuralithItems.ITEMS.register(modEventBus);

      AuralithMachines.BLOCKS.register(modEventBus);
      AuralithMachines.MACHINE_ITEMS.register(modEventBus);

      AuralithBlockEntities.register(modEventBus);

      preloadStructures();
      AuralithMachines.registerBlockEntities();

      modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
      modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    private void onServerStarting(ServerStartingEvent event) {}

    public static void onAddReloadListener(AddReloadListenerEvent event)
    {
      MultiblockStructureManager structureMgr = new MultiblockStructureManager();
      setStructureManager(structureMgr);
      event.addListener(structureMgr);
      LOGGER.info("[Auralith] Registered MultiblockStructureManager reload listener.");

      event.addListener(new OreProcessingManager());
      LOGGER.info("[Auralith] Registered OreProcessingManager reload listener.");
    }

    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
      MultiblockCommands.register(event.getDispatcher());
      LOGGER.info("[Auralith] Registered multiblock commands.");
    }

    public static ResourceLocation resGet(String path)
    {
      return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void preloadStructures()
    {
      try 
      {
        MultiblockStructureManager manager = new MultiblockStructureManager();
        setStructureManager(manager);
      } catch (Exception ignored) {}
    }

    public static MultiblockStructureManager getStructureManager() { return structureManager; }
    public static void setStructureManager(MultiblockStructureManager manager) { structureManager = manager; }
}
  package gg.amecute.auralithutilities.OreProcessing;

  import com.google.gson.Gson;
  import com.google.gson.GsonBuilder;
  import com.google.gson.JsonElement;
  import gg.amecute.auralithutilities.AuralithUtilities;
  import net.minecraft.resources.ResourceLocation;
  import net.minecraft.server.packs.resources.ResourceManager;
  import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
  import net.minecraft.util.profiling.ProfilerFiller;

  import java.util.Map;

  public class OreProcessingManager extends SimpleJsonResourceReloadListener 
  {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public OreProcessingManager() 
    {
      super(GSON, "mortar");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) 
    {
      OreProcessingRegistry.clear();

      int loaded = 0;
      for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) 
      {
        ResourceLocation id = entry.getKey();
        try 
        {
          OreProcessingEntry ope = GSON.fromJson(entry.getValue(), OreProcessingEntry.class);
          if (ope == null || ope.metal() == null || ope.metal().isBlank()) {
            AuralithUtilities.LOGGER.warn("[OreProcessing] Skipping invalid entry at {}", id);
            continue;
          }
          OreProcessingRegistry.register(ope);
          loaded++;
          AuralithUtilities.LOGGER.debug("[OreProcessing] Loaded entry '{}' from {}", ope.metal(), id);
        } 
        catch (Exception e) 
        {
          AuralithUtilities.LOGGER.error("[OreProcessing] Failed to parse entry at {}: {}", id, e.getMessage());
        }
      }

      AuralithUtilities.LOGGER.info("[OreProcessing] Reloaded {} entries ({} raw inputs).", loaded, OreProcessingRegistry.rawInputCount());
    }
  }
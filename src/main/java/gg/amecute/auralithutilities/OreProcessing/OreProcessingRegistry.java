package gg.amecute.auralithutilities.OreProcessing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gg.amecute.auralithutilities.AuralithUtilities;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public final class OreProcessingRegistry 
{

    private static final List<OreProcessingEntry>        ENTRIES          = new ArrayList<>();
    private static final Map<String, OreProcessingEntry> ORE_TO_ENTRY     = new HashMap<>();
    private static final Map<String, OreProcessingEntry> RAW_TO_ENTRY     = new HashMap<>(); 
    private static final Map<String, OreProcessingEntry> METAL_TO_ENTRY   = new HashMap<>();

    static 
    {
        final String path = "/data/auralithcore/ore_processing.json";
        try (InputStream is = OreProcessingRegistry.class.getResourceAsStream(path)) 
        {
          if (is == null)
            AuralithUtilities.LOGGER.error("[OreProcessing] {} not found in classpath!", path);
          else 
          {
            Type listType = new TypeToken<List<OreProcessingEntry>>() {}.getType();
            List<OreProcessingEntry> loaded = new Gson().fromJson(new InputStreamReader(is), listType);
            if (loaded != null) 
            {
              for (OreProcessingEntry entry : loaded) 
              {
                ENTRIES.add(entry);
                METAL_TO_ENTRY.put(entry.metal(), entry);

                for (String raw : entry.rawItems()) RAW_TO_ENTRY.put(raw, entry);
              }
              AuralithUtilities.LOGGER.info("[OreProcessing] Loaded {} entries ({} raw item inputs).", ENTRIES.size(), RAW_TO_ENTRY.size());
            }
          }
        } catch (Exception e) 
        {
          AuralithUtilities.LOGGER.error("[OreProcessing] Failed to load ore_processing.json", e);
        }
    }

    private OreProcessingRegistry() {}

    public static List<OreProcessingEntry> getEntries() 
    {
      return Collections.unmodifiableList(ENTRIES);
    }

    public static boolean isRawOre(String itemId) 
    {
      return RAW_TO_ENTRY.containsKey(itemId);
    }

    public static Optional<OreProcessingEntry> getEntryForRaw(String rawItemId) 
    {
      return Optional.ofNullable(RAW_TO_ENTRY.get(rawItemId));
    }

    public static Optional<OreProcessingEntry> getEntryForOre(String oreId) 
    {
      return Optional.ofNullable(ORE_TO_ENTRY.get(oreId));
    }

    public static Optional<OreProcessingEntry> getEntryForMetal(String metal) 
    {
      return Optional.ofNullable(METAL_TO_ENTRY.get(metal));
    }

    public static int size() { return ENTRIES.size(); }
}
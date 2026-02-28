package gg.amecute.auralithutilities.OreProcessing;

import gg.amecute.auralithutilities.AuralithUtilities;

import java.util.*;

public final class OreProcessingRegistry 
{
  private static final List<OreProcessingEntry>        ENTRIES        = new ArrayList<>();
  private static final Map<String, OreProcessingEntry> RAW_TO_ENTRY   = new HashMap<>();
  private static final Map<String, OreProcessingEntry> METAL_TO_ENTRY = new HashMap<>();

  private OreProcessingRegistry() {}

  public static void clear()
  {
    ENTRIES.clear();
    RAW_TO_ENTRY.clear();
    METAL_TO_ENTRY.clear();
  }

  public static void register(OreProcessingEntry entry) 
  {
    if (entry == null) return;

    ENTRIES.add(entry);
    METAL_TO_ENTRY.put(entry.metal(), entry);
    for (String raw : entry.rawItems())
      if (raw != null && !raw.isBlank())
          RAW_TO_ENTRY.put(raw, entry);
  }

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

  public static Optional<OreProcessingEntry> getEntryForMetal(String metal) 
  {
    return Optional.ofNullable(METAL_TO_ENTRY.get(metal));
  }

  public static int size()           { return ENTRIES.size(); }
  public static int rawInputCount()  { return RAW_TO_ENTRY.size(); }
}
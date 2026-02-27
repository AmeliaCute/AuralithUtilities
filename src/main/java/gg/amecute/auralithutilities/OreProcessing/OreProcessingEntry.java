package gg.amecute.auralithutilities.OreProcessing;

import java.util.List;

public final class OreProcessingEntry 
{
  private final String metal;
  private final List<String> raw_items;
  private final String crushed;
  private final float xp;

  private OreProcessingEntry() 
  {
    this.metal     = "";
    this.raw_items = List.of();
    this.crushed   = "";
    this.xp        = 0f;
  }

  public String       metal()    { return metal; }
  public List<String> rawItems() { return raw_items != null ? raw_items : List.of(); }
  public String       crushed()  { return crushed; }
  public float        xp()       { return xp; }

  public boolean needsItem(String itemId) 
  {
    return itemId != null && itemId.startsWith("auralithcore:");
  }

  @Override
  public String toString() 
  {
    return "OreProcessingEntry[metal=" + metal + ", crushed=" + crushed + "]";
  }
}
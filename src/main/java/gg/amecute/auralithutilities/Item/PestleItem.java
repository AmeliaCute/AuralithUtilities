package gg.amecute.auralithutilities.Item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PestleItem extends Item {

  public static final int MAX_DAMAGE = 64;

  public PestleItem() {
    super(new Item.Properties()
        .durability(MAX_DAMAGE)
        .stacksTo(1)
    );
  }

  public static boolean isPestle(ItemStack stack)
  {
    return stack.getItem() instanceof PestleItem;
  }
}
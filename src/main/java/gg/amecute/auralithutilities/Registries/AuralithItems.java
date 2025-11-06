package gg.amecute.auralithutilities.Registries;

import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Item.BlackHoleSpawnItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AuralithItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, AuralithUtilities.MODID);

    public static final DeferredHolder<Item, Item> STABLE_BLACK_HOLE_SPAWNER =
            ITEMS.register("stable_black_hole_spawner", () ->
                    new BlackHoleSpawnItem(new Item.Properties(), 3, true, 100));


}

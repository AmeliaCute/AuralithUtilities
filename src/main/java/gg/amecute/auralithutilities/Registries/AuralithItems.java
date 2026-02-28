package gg.amecute.auralithutilities.Registries;

import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Item.PestleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AuralithItems {

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, AuralithUtilities.MODID);

    public static final DeferredHolder<Item, PestleItem> PESTLE =
        ITEMS.register("pestle", PestleItem::new);

    public static final DeferredHolder<Item, Item> CRUSHED_ANTIMONY =
        registerSimple("crushed_antimony");

    public static final DeferredHolder<Item, Item> CRUSHED_IRIDIUM =
        registerSimple("crushed_iridium");

    public static final DeferredHolder<Item, Item> CRUSHED_SILVER =
        registerSimple("crushed_silver");

    public static final DeferredHolder<Item, Item> CRUSHED_TITANIUM =
        registerSimple("crushed_titanium");

    public static final DeferredHolder<Item, Item> CRUSHED_TUNGSTEN =
        registerSimple("crushed_tungsten");

    private static DeferredHolder<Item, Item> registerSimple(String name) 
    {
    return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private AuralithItems() {}
}
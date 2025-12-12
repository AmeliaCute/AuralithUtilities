package gg.amecute.auralithutilities.Event;

import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Config.CommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@EventBusSubscriber(modid = AuralithUtilities.MODID)
public class LogBreakGuard
{
    private static List<ResourceLocation> itemList = null;

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event)
    {
        BlockState state = event.getState();
        if (!state.is(BlockTags.LOGS)) return;
        if(itemList == null) itemList = CommonConfig.getAllowedItem();

        ItemStack held = event.getEntity().getMainHandItem();
        if (!(held.getItem() instanceof AxeItem) && !(itemList.contains(BuiltInRegistries.ITEM.getKey(held.getItem()))))
            event.setNewSpeed(0f);
    }
}

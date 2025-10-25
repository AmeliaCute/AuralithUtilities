package gg.amecute.auralithutilities.Event;

import gg.amecute.auralithutilities.AuralithUtilities;
import gg.amecute.auralithutilities.Config;
import gg.amecute.auralithutilities.MainMenu.AuralithTitleScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = AuralithUtilities.MODID, value = Dist.CLIENT)
public class MainMenuReplacer
{
    @SubscribeEvent
    public static void onMainMenuOpen(ScreenEvent.Opening event)
    {

        if(Config.AURALITH_MAIN_MENU.get() && event.getScreen() instanceof TitleScreen)
            event.setNewScreen(new AuralithTitleScreen());
    }
}

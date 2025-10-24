package gg.amecute.auralithutilities.MainMenu;

import gg.amecute.auralithutilities.AuralithUtilities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ModListScreen;
import org.jetbrains.annotations.NotNull;

public class AuralithTitleScreen extends Screen
{
    private final PanoramaRenderer panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
    private final ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "textures/ui/title.png");

    public AuralithTitleScreen()
    {
        super(Component.literal("Auralith Main Menu"));
    }

    @Override
    protected void init()
    {
        int spacing = 24;

        int centerX = this.width / 16 * 9;
        int startY = this.height / 9 * 3;

        assert this.minecraft != null;

        this.addRenderableWidget(AuralithButton.araBuilder(
                Component.translatable("menu.singleplayer"),
                button -> {
                    this.minecraft.setScreen(new SelectWorldScreen(this));
                }
        ).bounds(centerX, startY).build());

        this.addRenderableWidget(AuralithButton.araBuilder(
                Component.translatable("menu.multiplayer"),
                button -> {
                    this.minecraft.setScreen(new JoinMultiplayerScreen(this));
                }
        ).bounds(centerX, startY + spacing).build());

        this.addRenderableWidget(AuralithButton.araBuilder(
                Component.translatable("menu.options"),
                button -> {
                    this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
                }
        ).bounds(centerX, startY + spacing * 2).build());

        this.addRenderableWidget(AuralithButton.araBuilder(
                Component.translatable("fml.menu.mods"),
                button -> this.minecraft.setScreen(new ModListScreen(this))
        ).bounds(centerX, startY + spacing * 3).build());

        this.addRenderableWidget(AuralithButton.araBuilder(
                Component.translatable("menu.quit"),
                button -> {
                    this.minecraft.stop();
                }
        ).bounds(centerX, startY + spacing * 4).build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.panorama.render(guiGraphics, this.width, this.height, 0.4f, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        assert this.minecraft != null;
        guiGraphics.blit(icon,  this.width / 16 * 2, this.height / 9 * 3, 0f, 0f, 204, 64, 204, 64);
        guiGraphics.drawString(this.font, "NeoForge "+ this.minecraft.getLaunchedVersion() + " (" + ModList.get().size() + " Mods)", 2, this.height - 10, 0xFFFFFF);
    }
}

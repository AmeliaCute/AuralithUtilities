package gg.amecute.auralithutilities.Client.MainMenu;

import com.google.gson.Gson;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AuralithTitleScreen extends Screen
{
    private final PanoramaRenderer panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);
    private final ResourceLocation icon = ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "textures/ui/title.png");
    private final ResourceLocation splash = ResourceLocation.fromNamespaceAndPath(AuralithUtilities.MODID, "lang/auralith.json");

    private static final Random random = new Random();
    private static List<String> splashList;
    private static String currentString;

    public AuralithTitleScreen()
    {
        super(Component.literal("Auralith Main Menu"));
    }

    private void loadSplash()
    {
        try {
            InputStream stream  = this.minecraft.getResourceManager().getResource(splash).orElseThrow().open();
            InputStreamReader reader = new InputStreamReader(stream);

            Gson gson = new Gson();

            String[] strings = gson.fromJson(reader, String[].class);
            splashList = Arrays.stream(strings).toList();

            reader.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void init()
    {
        assert this.minecraft != null;
        if(splashList == null)
            loadSplash();

        currentString = splashList.get(random.nextInt(splashList.size()));

        int spacing = 20;
        int centerX = this.width / 2 - 100;
        int startY = this.height / 9 * 3 + 15;


        this.addRenderableWidget(TransButton.araBuilder(
                Component.translatable("menu.singleplayer"),
                button -> {
                    this.minecraft.setScreen(new SelectWorldScreen(this));
                }
        ).bounds(centerX, startY).build());

        this.addRenderableWidget(TransButton.araBuilder(
                Component.translatable("menu.multiplayer"),
                button -> {
                    this.minecraft.setScreen(new JoinMultiplayerScreen(this));
                }
        ).bounds(centerX, startY + spacing).build());

        this.addRenderableWidget(TransButton.araBuilder(
                Component.translatable("menu.options"),
                button -> {
                    this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
                }
        ).bounds(centerX, startY + spacing * 2).build());

        this.addRenderableWidget(TransButton.araBuilder(
                Component.translatable("fml.menu.mods"),
                button -> this.minecraft.setScreen(new ModListScreen(this))
        ).bounds(centerX, startY + spacing * 3).build());

        this.addRenderableWidget(TransButton.araBuilder(
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
        guiGraphics.blit(icon,  this.width / 2 - (204 / 2), this.height / 9, 0f, 0f, 204, 65, 204, 65);

        guiGraphics.drawCenteredString(this.font, currentString,this.width / 2, this.height / 9 * 8, 0xFFFFFFFF);
        guiGraphics.drawString(this.font, "NeoForge "+ this.minecraft.getLaunchedVersion() + " (" + ModList.get().size() + " Mods)", 2, this.height - 10, 0xFFFFFF);
    }
}

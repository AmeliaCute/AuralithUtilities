package gg.amecute.auralithutilities.Client.MainMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TransButton extends Button
{
    private final Minecraft minecraft;

    public TransButton(int x, int y, int width, int height, Component message, OnPress onPress)
    {
        super(x,y,width,height,message,onPress,DEFAULT_NARRATION);
        minecraft = Minecraft.getInstance();

    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHovered;

        int textColor = hovered ? 0x70FFFFFF : 0xFFFFFFFF;
        guiGraphics.drawString(
                minecraft.font,
                this.getMessage(),
                this.getX() + (this.width - minecraft.font.width(this.getMessage())) / 2,
                this.getY() + (this.height - 8) / 2,
                textColor,
                true);


    }

    public static Builder araBuilder(@NotNull Component _message, @NotNull OnPress _onPress)
    {
        return new Builder(_message, _onPress);
    }

    public static class Builder
    {
        private final Component message;
        private final OnPress onPress;

        private int x,y;
        private int width = 200;
        private int height = 20;

        public Builder(Component message, OnPress onPress)
        {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder bounds(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder bounds(int x, int y)
        {
            this.x = x;
            this.y = y;
            return this;
        }


        public TransButton build()
        {
            return new TransButton(x, y, width, height, message, onPress);
        }
    }

}

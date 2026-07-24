package io.github.dennisochulor.paint_literally_anywhere.client.color_picker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.awt.Color;

class ColorPreviewWidget extends AbstractWidget {
    private int argb;

    public ColorPreviewWidget(int argb) {
        super(0, 0, 100, 100, Component.literal("Color preview"));
        this.argb = argb;
    }

    public void setArgb(int argb) {
        this.argb = argb;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Solid color
        graphics.fill(getX(), getY(), getX() + width, getY() + height, argb);

        // Outline
        graphics.outline(getX(), getY(), width, height, Color.WHITE.getRGB());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal("Color preview"));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}

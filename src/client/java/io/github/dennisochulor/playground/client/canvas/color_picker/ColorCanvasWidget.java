package io.github.dennisochulor.playground.client.canvas.color_picker;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.dennisochulor.playground.Playground;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.awt.Color;

class ColorCanvasWidget extends AbstractWidget {
    private static final Identifier TARGET = Playground.id("color_canvas_target");

    private float saturation;
    private float light;
    private int hueArgb;

    public ColorCanvasWidget(int hueArgb, float saturation, float light) {
        super(0, 0, 100, 100, Component.literal("Color canvas"));
        this.hueArgb = hueArgb;
        this.saturation = saturation;
        this.light = light;
    }

    public void setHueArgb(int hueArgb) {
        this.hueArgb = hueArgb;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public float getLight() {
        return light;
    }

    public void setLight(float light) {
        this.light = light;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        super.onClick(event, doubleClick);
        saturation = (float) ((event.x() - gradientStartX()) / gradientWidth());
        light = (float) (1.0 - ((event.y() - gradientStartY()) / gradientHeight())); // since 1 is at top

        saturation = Mth.clamp(saturation, 0, 1);
        light = Mth.clamp(light, 0, 1);
    }

    // Accounting for the outline

    private int gradientStartX() {
        return getX() + 1;
    }

    private int gradientStartY() {
        return getY() + 1;
    }

    private int gradientWidth() {
        return width - 2;
    }

    private int gradientHeight() {
        return height - 2;
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        super.onDrag(event, dx, dy);
        if (isMouseOver(event.x(), event.y())) onClick(event, false);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = getX();
        int y = getY();

        graphics.outline(x, y, width, height, Color.WHITE.getRGB());

        // Saturation gradient, from left (white) to right (argb)
        // Light gradient, from top (white) to bottom (black)
        new GradientRectangleRenderState(graphics, gradientStartX(), gradientStartY(), x + width - 1, y + height - 1,
                Color.WHITE.getRGB(), hueArgb, Color.BLACK.getRGB(), Color.BLACK.getRGB()
        ).submit();

        // Target for currently selected sat/light
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TARGET,
                (int) (gradientStartX() + (saturation * gradientWidth())) - 3,
                (int) (gradientStartY() + (gradientHeight() - (light * gradientHeight()))) - 3,
                6, 6);

        if (isHovered()) graphics.requestCursor(CursorTypes.CROSSHAIR);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.USAGE,
                Component.literal("Saturation: %f, Light: %f".formatted(saturation, light)));
    }
}

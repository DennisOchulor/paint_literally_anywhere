package io.github.dennisochulor.paint_literally_anywhere.client.color_picker;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.awt.Color;

class HueSliderWidget extends AbstractWidget {
    private static final int[] RAINBOW = new int[]{Color.RED.getRGB(), Color.YELLOW.getRGB(),
            Color.GREEN.getRGB(), Color.CYAN.getRGB(), Color.BLUE.getRGB(), Color.MAGENTA.getRGB(), Color.RED.getRGB()};

    private float hue;

    public HueSliderWidget(float hue) {
        super(0, 0, 100, 100, Component.literal("Hue Slider"));
        this.hue = hue;
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        super.onClick(event, doubleClick);
        hue = (float) ((event.x() - gradientStartX()) / totalGradientWidth());
        hue = Mth.clamp(hue, 0 ,1);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        super.onDrag(event, dx, dy);
        if (isMouseOver(event.x(), event.y())) onClick(event, false);
    }

    @Override
    public int getWidth() {
        return totalGradientWidth() + 2;
    }

    // Accounting for the outline

    private int gradientStartX() {
        return getX() + 1;
    }

    private int gradientStartY() {
        return getY() + 1;
    }

    private int totalGradientWidth() {
        return singleGradientWidth() * numOfGradients();
    }

    private int singleGradientWidth() {
        return Math.round((float) (width - 2) / numOfGradients());
    }

    private int numOfGradients() {
        return RAINBOW.length - 1;
    }

    private int gradientHeight() {
        return height - 2;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = getX();
        int y = getY();

        // Outline
        graphics.outline(x, y, totalGradientWidth() + 2, height, Color.WHITE.getRGB());

        // The hue rainbow!
        for (int i = 0; i < numOfGradients(); i++) {
            new GradientRectangleRenderState(graphics,
                    gradientStartX() + (i * singleGradientWidth()),
                    gradientStartY(),
                    gradientStartX() + ((i+1) * singleGradientWidth()),
                    gradientStartY() + gradientHeight(),
                    RAINBOW[i], RAINBOW[i+1], RAINBOW[i+1], RAINBOW[i]
            ).submit();
        }

        // Render selected hue target
        int centerTargetX = (int) (gradientStartX() + (hue * totalGradientWidth()));
        graphics.outline(centerTargetX - 3, y - 5, 6, height + 10, Color.WHITE.getRGB());

        if (isHovered()) graphics.requestCursor(CursorTypes.RESIZE_EW);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.USAGE, Component.literal("Hue: " + hue));
    }
}

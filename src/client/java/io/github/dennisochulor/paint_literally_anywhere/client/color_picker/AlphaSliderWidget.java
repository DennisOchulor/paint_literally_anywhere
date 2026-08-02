package io.github.dennisochulor.paint_literally_anywhere.client.color_picker;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.awt.Color;

class AlphaSliderWidget extends AbstractWidget {
    public static final Identifier TRANSPARENT_TEXTURE = PLAMod.id("transparent");

    private float alpha; // range 0-1, tho anything < 0.1 is discarded by vanilla's terrain shaders
    private int rgb; // ignore the alpha value here

    public AlphaSliderWidget(Color color) {
        super(0, 0, 100, 100, Component.literal("Alpha Slider"));
        this.alpha = color.getAlpha() / 255.0F;
        this.rgb = color.getRGB();
    }

    // there is a getAlpha() method directly on AbstractWidget, not to be confused with this one!
    public int getSliderAlphaInt() {
        return Math.round(alpha * 255);
    }

    public void setAlpha(float alpha) {
        if (alpha < 0 || alpha > 1) throw new IllegalArgumentException("alpha must be 0-1!");
        this.alpha = alpha;
    }

    public void setRgb(int rgb) {
        this.rgb = rgb;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        super.onClick(event, doubleClick);
        alpha = (float) (1.0F - (event.y() - gradientStartY()) / gradientHeight());
        alpha = Math.clamp(alpha, 0, 1);
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

        // Outline
        graphics.outline(x, y, width, height, Color.WHITE.getRGB());

        int opaqueRgb = ARGB.opaque(rgb);
        int transparentRgb = ARGB.transparent(rgb);

        // The transparent texture for the background
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TRANSPARENT_TEXTURE, gradientStartX(), gradientStartY(), gradientWidth(), gradientHeight());

        // The alpha gradient, from top (opaque rgb) to bottom (transparent rgb)
        new GradientRectangleRenderState(graphics,
                gradientStartX(),
                gradientStartY(),
                gradientStartX() + gradientWidth(),
                gradientStartY() + gradientHeight(),
                opaqueRgb, opaqueRgb, transparentRgb, transparentRgb
        ).submit();

        // Render selected alpha target
        int centerTargetY = (int) (gradientStartY() + ((1.0F - alpha) * gradientHeight()));
        graphics.outline(x - 5, centerTargetY - 3, width + 10, 6, Color.WHITE.getRGB());

        if (isHovered()) graphics.requestCursor(CursorTypes.RESIZE_NS);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.USAGE, Component.literal("Alpha: " + alpha));
    }

    // Accounting for the outline

    private int gradientStartX() {
        return getX() + 1;
    }

    private int gradientStartY() {
        return getY() + 1;
    }

    private int gradientHeight() {
        return height - 2;
    }

    private int gradientWidth() {
        return width - 2;
    }
}

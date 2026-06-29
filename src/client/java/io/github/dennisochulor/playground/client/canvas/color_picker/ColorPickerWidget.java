package io.github.dennisochulor.playground.client.canvas.color_picker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.List;

public class ColorPickerWidget extends AbstractContainerWidget {
    @FunctionalInterface
    public interface ColorChangeListener {
        void onColorChange(Color color, boolean fromUserInput);
    }

    private Color color;

    private final ColorPreviewWidget previewWidget;
    private final ColorCanvasWidget canvasWidget;
    private final HueSliderWidget hueSliderWidget;
    private final LinearLayout root;
    private final ColorChangeListener listener;

    public ColorPickerWidget(int x, int y, int width, int height, Color color) {
        this(x, y, width, height, color, (_, _) -> {});
    }

    public ColorPickerWidget(int x, int y, int width, int height, Color color, ColorChangeListener listener) {
        super(x, y, width, height, Component.literal("Color picker"));
        this.listener = listener;
        this.color = color;
        float[] hsl = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsl);

        hueSliderWidget = new HueSliderWidget(hsl[0]);
        canvasWidget = new ColorCanvasWidget(Color.getHSBColor(hsl[0], 1, 1).getRGB(), hsl[1], hsl[2]);
        previewWidget = new ColorPreviewWidget(color.getRGB());

        root = LinearLayout.vertical().spacing(10);
        root.defaultCellSetting().alignHorizontallyCenter();
        root.addChild(previewWidget);
        root.addChild(canvasWidget);
        root.addChild(hueSliderWidget);
    }

    public Color getSelectedColor() {
        return color;
    }

    public void setSelectedColor(Color color) {
        this.color = color;
        float[] hsl = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsl);

        hueSliderWidget.setHue(hsl[0]);
        canvasWidget.setSaturation(hsl[1]);
        canvasWidget.setLight(hsl[2]);
        canvasWidget.setHueArgb(Color.getHSBColor(hueSliderWidget.getHue(), 1, 1).getRGB());
        previewWidget.setArgb(color.getRGB());

        listener.onColorChange(color, false);
    }

    private void updateColorFromHSL() {
        Color newColor = Color.getHSBColor(hueSliderWidget.getHue(), canvasWidget.getSaturation(), canvasWidget.getLight());

        if (!color.equals(newColor)) {
            color = newColor;
            previewWidget.setArgb(color.getRGB());
            canvasWidget.setHueArgb(Color.getHSBColor(hueSliderWidget.getHue(), 1, 1).getRGB());
            listener.onColorChange(color, true);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        updateColorFromHSL();

        int x = getX();
        int y = getY();

        int heightNoSpacing = height - 20;

        previewWidget.setWidth(width);
        previewWidget.setHeight((int) (heightNoSpacing * 0.2));

        canvasWidget.setWidth(width);
        canvasWidget.setHeight((int) (heightNoSpacing * 0.7));

        hueSliderWidget.setWidth(width);
        hueSliderWidget.setHeight((int) (heightNoSpacing * 0.1));

        root.arrangeElements();
        FrameLayout.alignInRectangle(root, x, y, width, height, 0.5F, 0.1F);
        root.visitWidgets(widget -> widget.extractRenderState(graphics, mouseX, mouseY, a));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal("Color picker widget"));
        output.add(NarratedElementType.USAGE, Component.literal("Current selected color: red %d green %d blue %d"
                .formatted(color.getRed(), color.getGreen(), color.getBlue())));
    }

    @Override
    protected int contentHeight() {
        return height;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(previewWidget, canvasWidget, hueSliderWidget);
    }
}

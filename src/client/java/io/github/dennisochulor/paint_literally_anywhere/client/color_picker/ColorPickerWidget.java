package io.github.dennisochulor.paint_literally_anywhere.client.color_picker;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractScrollArea;
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
    private final AlphaSliderWidget alphaSliderWidget;
    private final HueSliderWidget hueSliderWidget;
    private final LinearLayout root;
    private final ColorChangeListener listener;

    public ColorPickerWidget(int x, int y, int width, int height, Color color) {
        this(x, y, width, height, color, (_, _) -> {});
    }

    public ColorPickerWidget(int x, int y, int width, int height, Color color, ColorChangeListener listener) {
        super(x, y, width, height, Component.literal("Color picker"), AbstractScrollArea.defaultSettings(9));
        this.listener = listener;
        this.color = color;
        float[] hsl = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsl);

        hueSliderWidget = new HueSliderWidget(hsl[0]);
        canvasWidget = new ColorCanvasWidget(hsl[0], hsl[1], hsl[2]);
        alphaSliderWidget = new AlphaSliderWidget(color);
        previewWidget = new ColorPreviewWidget(color.getRGB());

        LinearLayout secondRow = LinearLayout.horizontal().spacing(15);
        secondRow.defaultCellSetting().alignHorizontallyCenter();
        secondRow.addChild(canvasWidget);
        secondRow.addChild(alphaSliderWidget);

        root = LinearLayout.vertical().spacing(10);
        root.defaultCellSetting().alignHorizontallyCenter();
        root.addChild(previewWidget);
        root.addChild(secondRow);
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
        canvasWidget.updateRefHueValue(hueSliderWidget.getHue());
        alphaSliderWidget.setAlpha(color.getAlpha() / 255.0F);
        alphaSliderWidget.setRgb(color.getRGB());
        previewWidget.setArgb(color.getRGB());

        listener.onColorChange(color, false);
    }

    private void updateColorFromWidgets() {
        Color newColor = Color.getHSBColor(hueSliderWidget.getHue(), canvasWidget.getSaturation(), canvasWidget.getLight());
        newColor = new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), alphaSliderWidget.getSliderAlphaInt());

        // for certain sat/light values, newColor == color regardless of hue value
        canvasWidget.updateRefHueValue(hueSliderWidget.getHue());

        if (!color.equals(newColor)) {
            color = newColor;
            previewWidget.setArgb(color.getRGB());
            alphaSliderWidget.setRgb(color.getRGB());
            listener.onColorChange(color, true);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        updateColorFromWidgets();

        int x = getX();
        int y = getY();

        int heightNoSpacing = height - 20;
        int secondRowWidthNoSpacing = width - 20;

        previewWidget.setWidth(width);
        previewWidget.setHeight((int) (heightNoSpacing * 0.2));

        canvasWidget.setWidth((int) (secondRowWidthNoSpacing * 0.95));
        canvasWidget.setHeight((int) (heightNoSpacing * 0.7));

        alphaSliderWidget.setWidth((int) (secondRowWidthNoSpacing * 0.05));
        alphaSliderWidget.setHeight((int) (heightNoSpacing * 0.7));

        hueSliderWidget.setWidth(width);
        hueSliderWidget.setHeight((int) (heightNoSpacing * 0.1));

        root.arrangeElements();
        FrameLayout.alignInRectangle(root, x, y, width, height, 0.5F, 0.1F);
        root.visitWidgets(widget -> widget.extractRenderState(graphics, mouseX, mouseY, a));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal("Color picker widget"));
        output.add(NarratedElementType.USAGE, Component.literal("Current selected color: red %d green %d blue %d alpha %d"
                .formatted(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())));
    }

    @Override
    protected int contentHeight() {
        return height;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(previewWidget, canvasWidget, alphaSliderWidget, hueSliderWidget);
    }
}

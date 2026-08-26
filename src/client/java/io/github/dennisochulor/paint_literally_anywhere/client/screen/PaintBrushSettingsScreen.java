package io.github.dennisochulor.paint_literally_anywhere.client.screen;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.client.PaintBrushClientInteractions;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItemIds;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintbrushUpdatePacket;
import io.github.dennisochulor.paint_literally_anywhere.client.color_picker.ColorPickerWidget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.stream.IntStream;

public class PaintBrushSettingsScreen extends Screen {
    private static final Identifier DYE_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/dye"); // from LoomScreen
    private static final Identifier CONFIRM_SPRITE = Identifier.withDefaultNamespace("container/beacon/confirm"); // from BeaconScreen
    private static final Identifier CANCEL_SPRITE = Identifier.withDefaultNamespace("container/beacon/cancel"); // from BeaconScreen
    private static final Identifier EYEDROPPER_SPRITE = PLAMod.id("eyedropper");
    private static final int[] BRUSH_SIZE_SLIDER_VALUES = IntStream.range(PaintBrushProperties.MIN_BRUSH_SIZE, PaintBrushProperties.MAX_BRUSH_SIZE + 1).toArray();
    private static final InputWithModifiers DUMMY_INPUT = new MouseButtonInfo(0, 0);

    private final HeaderAndFooterLayout root;
    private final CycleButton<PaintBrushProperties.Tool> toolButton;
    private final IntSliderButton brushSizeSlider;
    private final IntSliderButton resolutionSlider;
    private final ColorPickerWidget colorPickerWidget;
    private final Checkbox emissiveCheckbox;
    private final SpriteIconButton eyedropperButton;

    public PaintBrushSettingsScreen(PaintBrushProperties properties, ItemStack paintBrush) {
        super(Component.literal("Paint brush settings"));

        if (!paintBrush.is(ModItemIds.PAINT_BRUSH)) {
            throw new IllegalArgumentException("Passed in item stack is not a paint brush!");
        }

        StringWidget title = new StringWidget(Component.literal("Paint Brush Settings"), Minecraft.getInstance().font);

        toolButton = CycleButton.builder(tool -> Component.literal(tool.toString()), properties.tool())
                .withValues(PaintBrushProperties.Tool.values())
                .withTooltip(tool -> Tooltip.create(tool.component))
                .create(0, 0, 100, 20, Component.literal("Tool"));

        brushSizeSlider = new IntSliderButton(0, 0, 100, 20, "Brush Size", properties.brushSize(), BRUSH_SIZE_SLIDER_VALUES);
        brushSizeSlider.setTooltip(Tooltip.create(Component.literal("Controls number of pixels painted at once, higher values use more durability.")));
        resolutionSlider = new IntSliderButton(0, 0, 100, 20, "Resolution", properties.resolution(), PaintBrushProperties.RESOLUTION_VALUES);
        resolutionSlider.setTooltip(Tooltip.create(Component.literal("Controls number of pixels in the grid, only effective for unpainted surfaces.")));

        colorPickerWidget = new ColorPickerWidget(0, 0, 250, 100, new Color(properties.argb(), true));
        emissiveCheckbox = Checkbox.builder(Component.literal("Emissive"), Minecraft.getInstance().font).selected(properties.emissive()).build();
        emissiveCheckbox.setTooltip(Tooltip.create(Component.literal("Whether the paint should glow in the dark")));

        eyedropperButton = SpriteIconButton.builder(
                Component.literal("Pick color from a block"),
                        _ -> {
                            PaintBrushClientInteractions.useEyedropper(this, paintBrush);
                            this.onClose();
                        },
                        true
                )
                .size(20, 20)
                .sprite(EYEDROPPER_SPRITE, 14, 14)
                .withTootip()
                .build();

        Button applyButton = Button.builder(Component.literal("Apply"), _ -> {
            PaintBrushProperties newProps = new PaintBrushProperties(
                    colorPickerWidget.getSelectedColor().getRGB(),
                    emissiveCheckbox.selected(),
                    brushSizeSlider.getActualValue(),
                    resolutionSlider.getActualValue(),
                    toolButton.getValue()
            );
            ClientPlayNetworking.send(new ServerboundPaintbrushUpdatePacket(newProps));
            this.onClose();
        }).size(100, 20).build();


        LinearLayout colorPickerLayout = LinearLayout.horizontal().spacing(10);
        colorPickerLayout.defaultCellSetting().alignVerticallyTop();
        colorPickerLayout.addChild(colorPickerWidget);
        colorPickerLayout.addChild(eyedropperButton);

        LinearLayout contents = LinearLayout.vertical().spacing(10);
        contents.defaultCellSetting().alignHorizontallyCenter();
        contents.addChild(toolButton);
        contents.addChild(brushSizeSlider);
        contents.addChild(resolutionSlider);
        contents.addChild(emissiveCheckbox);
        contents.addChild(colorPickerLayout);

        root = new HeaderAndFooterLayout(this, 20, 33);
        root.addToHeader(title);
        root.addToContents(contents);
        root.addToFooter(applyButton);
    }

    @Override
    protected void init() {
        super.init();

        colorPickerWidget.setWidth((int) (width * 0.5));
        colorPickerWidget.setHeight((int) (height * 0.35));

        root.arrangeElements();
        FrameLayout.alignInRectangle(root, 0, 0, width, height, 0.5F, 0.1F);
        root.visitWidgets(this::addRenderableWidget);
    }

    public void setColorFromEyedropper(int argb, boolean emissive) {
        colorPickerWidget.setSelectedColor(new Color(argb, true));

        // Mojang why can't i just setSelected() wtf
        boolean isCurrentlyEmissive = emissiveCheckbox.selected();

        if ((isCurrentlyEmissive && !emissive) || (!isCurrentlyEmissive && emissive)) {
            emissiveCheckbox.onPress(DUMMY_INPUT); // toggles it
        }

        Minecraft.getInstance().gui.setScreen(this);
    }
}

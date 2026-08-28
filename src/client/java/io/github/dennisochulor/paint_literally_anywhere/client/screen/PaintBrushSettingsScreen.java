package io.github.dennisochulor.paint_literally_anywhere.client.screen;

import com.mojang.datafixers.util.Pair;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.client.PaintBrushClientInteractions;
import io.github.dennisochulor.paint_literally_anywhere.client.color_picker.ColorPickerWidget;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItemIds;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushItem;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintbrushUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.awt.Color;
import java.util.stream.IntStream;

public class PaintBrushSettingsScreen extends Screen {
    private static final Identifier CONFIRM_SPRITE = Identifier.withDefaultNamespace("container/beacon/confirm"); // from BeaconScreen
    private static final Identifier CANCEL_SPRITE = Identifier.withDefaultNamespace("container/beacon/cancel"); // from BeaconScreen
    private static final Identifier EYEDROPPER_SPRITE = PLAMod.id("eyedropper");
    private static final int[] BRUSH_SIZE_SLIDER_VALUES = IntStream.range(PaintBrushProperties.MIN_BRUSH_SIZE, PaintBrushProperties.MAX_BRUSH_SIZE + 1).toArray();
    private static final InputWithModifiers DUMMY_INPUT = new MouseButtonInfo(0, 0);
    private static final ItemStack GLOW_INK_SAC = new ItemStack(Items.GLOW_INK_SAC);
    private static final Tooltip MISSING_ITEMS_TOOLTIP = Tooltip.create(Component.literal("You don't have some of the required items!"));

    private final HeaderAndFooterLayout root;
    private final CycleButton<PaintBrushProperties.Tool> toolButton;
    private final IntSliderButton brushSizeSlider;
    private final IntSliderButton resolutionSlider;
    private final Checkbox emissiveCheckbox;
    private final ColorPickerWidget colorPickerWidget;
    private final SpriteIconButton eyedropperButton;
    private final Button applyButton;

    private final ItemStack[] requiredItems = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private final PaintBrushProperties originalProperties;
    private final Player player;

    public PaintBrushSettingsScreen(ItemStack paintBrush, Player player) {
        super(Component.literal("Paint brush settings"));

        if (!paintBrush.is(ModItemIds.PAINT_BRUSH)) {
            throw new IllegalArgumentException("Passed in item stack is not a paint brush!");
        }

        PaintBrushProperties properties = paintBrush.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
        originalProperties = properties;
        this.player = player;

        StringWidget title = new StringWidget(Component.literal("Paint Brush Settings"), Minecraft.getInstance().font);

        toolButton = CycleButton.builder(tool -> Component.literal(tool.toString()), properties.tool())
                .withValues(PaintBrushProperties.Tool.values())
                .withTooltip(tool -> Tooltip.create(tool.component))
                .create(0, 0, 100, 20, Component.literal("Tool"));

        brushSizeSlider = new IntSliderButton(0, 0, 100, 20, "Brush Size", properties.brushSize(), BRUSH_SIZE_SLIDER_VALUES);
        brushSizeSlider.setTooltip(Tooltip.create(Component.literal("Controls number of pixels painted at once, higher values use more durability.")));
        resolutionSlider = new IntSliderButton(0, 0, 100, 20, "Resolution", properties.resolution(), PaintBrushProperties.RESOLUTION_VALUES);
        resolutionSlider.setTooltip(Tooltip.create(Component.literal("Controls number of pixels in the grid, only effective for unpainted surfaces.")));

        colorPickerWidget = new ColorPickerWidget(0, 0, 250, 100, new Color(properties.argb(), true), this::onColorChange);
        emissiveCheckbox = Checkbox.builder(Component.literal("Emissive"), font).selected(properties.emissive()).onValueChange(this::onEmissiveCheckboxChange).build();
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

        applyButton = Button.builder(Component.literal("Apply"), _ -> {
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


        LinearLayout colorPickerLayout = LinearLayout.horizontal().spacing(20);
        colorPickerLayout.defaultCellSetting().alignVerticallyTop();
        colorPickerLayout.addChild(colorPickerWidget);
        colorPickerLayout.addChild(eyedropperButton);

        LinearLayout contents = LinearLayout.vertical().spacing(10);
        contents.defaultCellSetting().alignHorizontallyCenter();
        contents.addChild(toolButton);
        contents.addChild(brushSizeSlider);
        contents.addChild(resolutionSlider);
        contents.addChild(emissiveCheckbox, contents.newCellSettings().paddingTop(10));
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

    @Override
    public void tick() {
        super.tick();

        boolean pass = true;
        if (!player.hasInfiniteMaterials()) {
            for (ItemStack itemStack : requiredItems) {
                if (!itemStack.isEmpty()) {
                    if (!player.getInventory().contains(stack -> stack.is(itemStack.getItem()) && !stack.isEmpty())) {
                        pass = false;
                        break;
                    }
                }
            }
        }

        applyButton.active = pass;
        applyButton.setTooltip(pass ? null : MISSING_ITEMS_TOOLTIP);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        int x = eyedropperButton.getX();
        int y = eyedropperButton.getY();
        graphics.text(font, "Required Items:", x, y + 40, Color.WHITE.getRGB());

        for (int i = 0; i < requiredItems.length; i++) {
            ItemStack itemStack = requiredItems[i];

            if (!itemStack.isEmpty()) {
                int x1 = x + (i * 20);
                int y1 = y + 60;
                graphics.item(itemStack, x1, y1);

                boolean hasItem = player.hasInfiniteMaterials() ||
                        player.getInventory().contains(stack -> stack.is(itemStack.getItem()) && !stack.isEmpty());
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hasItem ? CONFIRM_SPRITE : CANCEL_SPRITE, x1 + 8, y1 + 8, 10, 10);

                if (isHovering(x1, y1, 16, 16, mouseX, mouseY)) {
                    graphics.setTooltipForNextFrame(font, getTooltipFromItem(Minecraft.getInstance(), itemStack), itemStack.getTooltipImage(),
                            mouseX, mouseY, itemStack.get(DataComponents.TOOLTIP_STYLE));
                }
            }
        }
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



    private void onColorChange(Color color, boolean fromUserInput) {
        Pair<ItemStack, ItemStack> dyes = PaintBrushItem.getRequiredDyesIfNeeded(new Color(originalProperties.argb()), color);
        requiredItems[0] = dyes.getFirst();
        requiredItems[1] = dyes.getSecond();
        requiredItems[2] = ItemStack.EMPTY;

        onEmissiveCheckboxChange(emissiveCheckbox, emissiveCheckbox.selected());
    }

    private void onEmissiveCheckboxChange(Checkbox checkbox, boolean value) {
        boolean needsInkSac = !originalProperties.emissive() && value;
        boolean seenInkSac = false;

        for (int i = 0; i < requiredItems.length; i++) {
            if (requiredItems[i].isEmpty() && needsInkSac && !seenInkSac) {
                requiredItems[i] = GLOW_INK_SAC;
                seenInkSac = true;
            }
            else if (requiredItems[i].is(Items.GLOW_INK_SAC)) {
                seenInkSac = true;

                if (!needsInkSac) {
                    requiredItems[i] = ItemStack.EMPTY;
                }
            }
        }
    }

    // from AbstractContainerScreen
    @SuppressWarnings("SameParameterValue")
    private static boolean isHovering(final int left, final int top, final int w, final int h, double xm, double ym) {
        return xm >= left - 1 && xm < left + w + 1 && ym >= top - 1 && ym < top + h + 1;
    }
}

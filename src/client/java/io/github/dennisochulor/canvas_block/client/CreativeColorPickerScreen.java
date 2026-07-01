package io.github.dennisochulor.canvas_block.client;

import io.github.dennisochulor.canvas_block.network.ServerboundPaintbrushUpdatePacket;
import io.github.dennisochulor.canvas_block.client.color_picker.ColorPickerWidget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;

public class CreativeColorPickerScreen extends Screen {
    private final ColorPickerWidget colorPickerWidget;
    private final HeaderAndFooterLayout root;
    private final Checkbox emissiveCheckbox;

    protected CreativeColorPickerScreen(Color color, boolean emissive) {
        super(Component.literal("Creative color picker screen"));

        StringWidget title = new StringWidget(Component.literal("Choose a color"), Minecraft.getInstance().font);
        colorPickerWidget = new ColorPickerWidget(0, 0, 250, 100, color);
        emissiveCheckbox = Checkbox.builder(Component.literal("Emissive"), Minecraft.getInstance().font).selected(emissive).build();
        emissiveCheckbox.setTooltip(Tooltip.create(Component.literal("Whether the paint should glow in the dark")));

        Button applyButton = Button.builder(Component.literal("Apply"), _ -> {
            ClientPlayNetworking.send(new ServerboundPaintbrushUpdatePacket(colorPickerWidget.getSelectedColor().getRGB(), emissiveCheckbox.selected()));
            this.onClose();
        }).size(100, 20).build();

        LinearLayout contents = LinearLayout.vertical().spacing(5);
        contents.defaultCellSetting().alignHorizontallyCenter();
        contents.addChild(emissiveCheckbox);
        contents.addChild(colorPickerWidget);

        root = new HeaderAndFooterLayout(this, 20, 33);
        root.addToHeader(title);
        root.addToContents(contents);
        root.addToFooter(applyButton);
    }

    @Override
    protected void init() {
        super.init();

        colorPickerWidget.setWidth((int) (width * 0.8));
        colorPickerWidget.setHeight((int) (height * 0.6));

        root.arrangeElements();
        FrameLayout.alignInRectangle(root, 0, 0, width, height, 0.5F, 0.1F);
        root.visitWidgets(this::addRenderableWidget);
    }
}

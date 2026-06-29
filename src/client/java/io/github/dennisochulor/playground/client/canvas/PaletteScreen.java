package io.github.dennisochulor.playground.client.canvas;

import io.github.dennisochulor.playground.Playground;
import io.github.dennisochulor.playground.canvas.CanvasMod;
import io.github.dennisochulor.playground.canvas.PaletteMenu;
import io.github.dennisochulor.playground.canvas.ServerboundPaletteMenuUpdatePacket;
import io.github.dennisochulor.playground.client.canvas.color_picker.ColorPickerWidget;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;

public class PaletteScreen extends AbstractContainerScreen<PaletteMenu> {
    private static final Identifier CONTAINER_TEXTURE = Playground.id("textures/gui/container/palette.png");

    private final ColorPickerWidget colorPickerWidget;
    private final PaletteMenu menu;

    public PaletteScreen(PaletteMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, DEFAULT_IMAGE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
        // Center the title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelY = PaletteMenu.INVENTORY_START_Y - 12;
        this.colorPickerWidget = new ColorPickerWidget(0, 0, this.imageWidth - 10, this.imageHeight / 3 + 13,
                PaletteMenu.STARTING_COLOR,
                ((color, fromUserInput) -> {
                    if (fromUserInput) updateRequestedColor(color, false);
                }));
        this.menu = menu;

        menu.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack itemStack) {
                if (slotIndex == PaletteMenu.INPUT_SLOT_INDEX && !itemStack.isEmpty()) {
                    Integer color = itemStack.get(CanvasMod.RGB_COLOR);
                    if (color != null) updateRequestedColor(new Color(color), true);
                }
            }

            @Override
            public void dataChanged(AbstractContainerMenu container, int id, int value) {}
        });
    }

    @Override
    protected void init() {
        super.init();

        colorPickerWidget.setPosition(
                this.leftPos + 5,
                this.topPos + 15
        );

        this.addRenderableWidget(colorPickerWidget);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
    }

    private void updateRequestedColor(Color color, boolean updateColorPickerWidget) {
        menu.setRequestedColor(color.getRGB());
        if (updateColorPickerWidget) colorPickerWidget.setSelectedColor(color);
        ClientPlayNetworking.send(new ServerboundPaletteMenuUpdatePacket(color.getRGB()));
    }
}

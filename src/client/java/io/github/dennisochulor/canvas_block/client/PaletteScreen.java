package io.github.dennisochulor.canvas_block.client;

import io.github.dennisochulor.canvas_block.CanvasMod;
import io.github.dennisochulor.canvas_block.item.ModComponents;
import io.github.dennisochulor.canvas_block.item.PaletteMenu;
import io.github.dennisochulor.canvas_block.network.ServerboundPaletteMenuUpdatePacket;
import io.github.dennisochulor.canvas_block.client.color_picker.ColorPickerWidget;
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
    private static final Identifier CONTAINER_TEXTURE = CanvasMod.id("textures/gui/container/palette.png");
    private static final Identifier DYE_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/dye"); // from LoomScreen
    private static final Identifier CONFIRM_SPRITE = Identifier.withDefaultNamespace("container/beacon/confirm"); // from BeaconScreen
    private static final Identifier CANCEL_SPRITE = Identifier.withDefaultNamespace("container/beacon/cancel"); // from BeaconScreen

    private final ColorPickerWidget colorPickerWidget;
    private final PaletteMenu menu;
    private final Inventory inventory;

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
        this.inventory = inventory;

        menu.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack itemStack) {
                if (slotIndex == PaletteMenu.INPUT_SLOT_INDEX && !itemStack.isEmpty()) {
                    Integer color = itemStack.get(ModComponents.RGB_COLOR);
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

        if (menu.getDyeSlots().getItem(PaletteMenu.FIRST_DYE_SLOT_INDEX).isEmpty()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DYE_SLOT_SPRITE, this.leftPos + PaletteMenu.FIRST_DYE_SLOT_START_X,
                    this.topPos + PaletteMenu.FIRST_DYE_SLOT_START_Y, 16, 16);
        }
        if (menu.getDyeSlots().getItem(PaletteMenu.SECOND_DYE_SLOT_INDEX).isEmpty()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DYE_SLOT_SPRITE, this.leftPos + PaletteMenu.SECOND_DYE_SLOT_START_X,
                    this.topPos + PaletteMenu.SECOND_DYE_SLOT_START_Y, 16, 16);
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractContents(graphics, mouseX, mouseY, a);

        ItemStack firstDye = menu.getDyeSlots().getItem(PaletteMenu.FIRST_DYE_SLOT_INDEX);
        ItemStack secondDye = menu.getDyeSlots().getItem(PaletteMenu.SECOND_DYE_SLOT_INDEX);

        if (!firstDye.isEmpty()) {
            boolean hasDye = inventory.contains(itemStack -> itemStack.is(firstDye.getItem()) && !itemStack.isEmpty());
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hasDye ? CONFIRM_SPRITE : CANCEL_SPRITE,
                    this.leftPos + PaletteMenu.FIRST_DYE_SLOT_START_X + 8,
                    this.topPos + PaletteMenu.FIRST_DYE_SLOT_START_Y + 8,
                    10, 10
            );
        }
        if (!secondDye.isEmpty()) {
            boolean hasDye = inventory.contains(itemStack -> itemStack.is(secondDye.getItem()) && !itemStack.isEmpty());
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hasDye ? CONFIRM_SPRITE : CANCEL_SPRITE,
                    this.leftPos + PaletteMenu.SECOND_DYE_SLOT_START_X + 8,
                    this.topPos + PaletteMenu.SECOND_DYE_SLOT_START_Y + 8,
                    10, 10
            );
        }
    }

    private void updateRequestedColor(Color color, boolean updateColorPickerWidget) {
        menu.setRequestedColor(color);
        if (updateColorPickerWidget) colorPickerWidget.setSelectedColor(color);
        ClientPlayNetworking.send(new ServerboundPaletteMenuUpdatePacket(color.getRGB()));
    }
}

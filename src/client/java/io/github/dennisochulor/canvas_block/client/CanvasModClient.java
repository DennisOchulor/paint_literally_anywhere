package io.github.dennisochulor.canvas_block.client;

import io.github.dennisochulor.canvas_block.ModMenuTypes;
import io.github.dennisochulor.canvas_block.block.ModBlockEntities;
import io.github.dennisochulor.canvas_block.item.ModComponents;
import io.github.dennisochulor.canvas_block.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;

public class CanvasModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModBlockEntities.CANVAS_BLOCK_ENTITY, CanvasRenderer::new);
        ItemTintSources.ID_MAPPER.put(RGBColorTintSource.ID, RGBColorTintSource.MAP_CODEC);
        MenuScreens.register(ModMenuTypes.PALETTE_MENU, PaletteScreen::new);

        ModClientNetworking.init();

        ItemEvents.USE.register((level, player, _) -> {
            ItemStack itemStack = player.getMainHandItem();

            if (itemStack.getItem() != ModItems.PAINT_BRUSH) return null;
            if (!level.isClientSide() || !player.isCreative() || !player.isCrouching()) return null;

            int rgb = itemStack.getOrDefault(ModComponents.RGB_COLOR, Color.YELLOW.getRGB());
            boolean emissive = itemStack.has(ModComponents.EMISSIVE);

            Minecraft.getInstance().gui.setScreen(new CreativeColorPickerScreen(new Color(rgb), emissive));
            return InteractionResult.CONSUME;
        });
    }
}

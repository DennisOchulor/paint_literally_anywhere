package io.github.dennisochulor.playground.client.canvas;

import io.github.dennisochulor.playground.canvas.CanvasBlockEntity;
import io.github.dennisochulor.playground.canvas.CanvasMod;
import io.github.dennisochulor.playground.canvas.ClientboundCanvasUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.awt.Color;
import java.util.Objects;

public class CanvasModClient {
    public static void init() {
        BlockEntityRenderers.register(CanvasMod.CANVAS_BLOCK_ENTITY, CanvasRenderer::new);
        ItemTintSources.ID_MAPPER.put(RGBColorTintSource.ID, RGBColorTintSource.MAP_CODEC);
        MenuScreens.register(CanvasMod.PALETTE_MENU, PaletteScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundCanvasUpdatePacket.TYPE, (packet, context) -> {
            BlockEntity blockEntity = Objects.requireNonNull(context.client().level).getBlockEntity(packet.pos());

            if (blockEntity instanceof CanvasBlockEntity canvas) {
                canvas.setPixel(packet.side(), packet.index(), packet.color(), packet.emissive());
            }
        });

        ItemEvents.USE.register((level, player, _) -> {
            ItemStack itemStack = player.getMainHandItem();

            if (itemStack.getItem() != CanvasMod.PAINT_BRUSH) return InteractionResult.PASS;
            if (!level.isClientSide() || !player.isCreative() || !player.isCrouching()) return InteractionResult.PASS;

            int rgb = itemStack.getOrDefault(CanvasMod.RGB_COLOR, Color.YELLOW.getRGB());
            boolean emissive = itemStack.has(CanvasMod.EMISSIVE);

            Minecraft.getInstance().gui.setScreen(new CreativeColorPickerScreen(new Color(rgb), emissive));
            return InteractionResult.CONSUME;
        });
    }
}

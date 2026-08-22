package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.item.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;

public final class ModNetworking {
    private ModNetworking() {}

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPaintbrushUpdatePacket.TYPE, ServerboundPaintbrushUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPaletteMenuUpdatePacket.TYPE, ServerboundPaletteMenuUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPaintPacket.TYPE, ServerboundPaintPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundChunkCanvasDataUpdatePacket.TYPE, ClientboundChunkCanvasDataUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundChunkCanvasDataAddPacket.TYPE, ClientboundChunkCanvasDataAddPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundChunkCanvasDataInitialSyncPacket.TYPE, ClientboundChunkCanvasDataInitialSyncPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPaintbrushUpdatePacket.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            ItemStack itemStack = player.getMainHandItem();

            if (!player.isCreative()) return;
            if (itemStack.getItem() != ModItems.PAINT_BRUSH) {
                PLAMod.LOGGER.warn("Received paintbrush update packet from player {} not holding paintbrush in main hand!", player);
                return;
            }

            PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT)
                    .withArgb(payload.argb()).withEmissive(payload.emissive());
            itemStack.set(ModComponents.PAINT_BRUSH, properties);
        });

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPaletteMenuUpdatePacket.TYPE, ((payload, context) -> {
            if (context.player().containerMenu instanceof PaletteMenu menu) {
                menu.setRequestedColor(new Color(payload.argb(), true));
            }
        }));

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPaintPacket.TYPE, ((payload, context) -> {
            PaintBrushItem.handlePacket(payload, context.player());
        }));
    }
}

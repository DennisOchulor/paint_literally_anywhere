package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
    private ModNetworking() {}

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPaintbrushUpdatePacket.TYPE, ServerboundPaintbrushUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPaintPacket.TYPE, ServerboundPaintPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundChunkCanvasDataUpdatePacket.TYPE, ClientboundChunkCanvasDataUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundChunkCanvasDataAddPacket.TYPE, ClientboundChunkCanvasDataAddPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().registerLarge(ClientboundChunkCanvasDataInitialSyncPacket.TYPE, ClientboundChunkCanvasDataInitialSyncPacket.STREAM_CODEC, 10 * 1024 * 1024);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPaintbrushUpdatePacket.TYPE, (payload, context) -> {
            PaintBrushItem.handlePaintbrushUpdatePacket(payload, context.player());
        });

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPaintPacket.TYPE, ((payload, context) -> {
            PaintBrushItem.handlePaintPacket(payload, context.player());
        }));
    }
}

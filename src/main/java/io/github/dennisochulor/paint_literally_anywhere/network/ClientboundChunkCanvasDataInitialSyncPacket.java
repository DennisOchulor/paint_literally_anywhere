package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;

public record ClientboundChunkCanvasDataInitialSyncPacket(ChunkPos pos, ChunkCanvasData data) implements CustomPacketPayload {
    public static final Type<ClientboundChunkCanvasDataInitialSyncPacket> TYPE = new Type<>(PLAMod.id("initial_sync_chunk_canvas_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChunkCanvasDataInitialSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ChunkPos.STREAM_CODEC, ClientboundChunkCanvasDataInitialSyncPacket::pos,
            ChunkCanvasData.STREAM_CODEC, ClientboundChunkCanvasDataInitialSyncPacket::data,
            ClientboundChunkCanvasDataInitialSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

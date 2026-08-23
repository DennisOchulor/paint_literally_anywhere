package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.OddCodecs;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundChunkCanvasDataUpdatePacket(
        BlockPos pos,
        QuadTemplate template,
        int[] indices,
        int argb,
        boolean emissive,
        int resolution
) implements CustomPacketPayload {
    public static final Type<ClientboundChunkCanvasDataUpdatePacket> TYPE = new Type<>(PLAMod.id("update_chunk_canvas_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChunkCanvasDataUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundChunkCanvasDataUpdatePacket::pos,
            QuadTemplate.STREAM_CODEC, ClientboundChunkCanvasDataUpdatePacket::template,
            OddCodecs.INT_ARRAY_STREAM_CODEC, ClientboundChunkCanvasDataUpdatePacket::indices,
            ByteBufCodecs.INT, ClientboundChunkCanvasDataUpdatePacket::argb,
            ByteBufCodecs.BOOL, ClientboundChunkCanvasDataUpdatePacket::emissive,
            ByteBufCodecs.VAR_INT, ClientboundChunkCanvasDataUpdatePacket::resolution,
            ClientboundChunkCanvasDataUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientboundChunkCanvasDataUpdatePacket(BlockPos pos, QuadTemplate template, int index, int argb, boolean emissive) implements CustomPacketPayload {
    public static final Type<ClientboundChunkCanvasDataUpdatePacket> TYPE = new Type<>(PLAMod.id("update_chunk_canvas_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChunkCanvasDataUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundChunkCanvasDataUpdatePacket::pos,
            QuadTemplate.STREAM_CODEC, ClientboundChunkCanvasDataUpdatePacket::template,
            ByteBufCodecs.INT, ClientboundChunkCanvasDataUpdatePacket::index,
            ByteBufCodecs.INT, ClientboundChunkCanvasDataUpdatePacket::argb,
            ByteBufCodecs.BOOL, ClientboundChunkCanvasDataUpdatePacket::emissive,
            ClientboundChunkCanvasDataUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

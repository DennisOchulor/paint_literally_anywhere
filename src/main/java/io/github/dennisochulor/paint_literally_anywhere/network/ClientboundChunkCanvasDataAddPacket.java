package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record ClientboundChunkCanvasDataAddPacket(BlockPos pos, List<QuadInstance> instances) implements CustomPacketPayload {
    public static final Type<ClientboundChunkCanvasDataAddPacket> TYPE = new Type<>(PLAMod.id("add_chunk_canvas_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChunkCanvasDataAddPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundChunkCanvasDataAddPacket::pos,
            QuadInstance.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ArrayList::new, List::copyOf), ClientboundChunkCanvasDataAddPacket::instances,
            ClientboundChunkCanvasDataAddPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

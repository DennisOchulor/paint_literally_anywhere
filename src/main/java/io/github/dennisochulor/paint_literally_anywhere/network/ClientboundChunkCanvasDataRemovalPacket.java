package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashSet;
import java.util.Set;

public record ClientboundChunkCanvasDataRemovalPacket(BlockPos pos, Set<QuadTemplate> templates) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundChunkCanvasDataRemovalPacket> TYPE = new CustomPacketPayload.Type<>(PLAMod.id("remove_chunk_canvas_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChunkCanvasDataRemovalPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientboundChunkCanvasDataRemovalPacket::pos,
            QuadTemplate.STREAM_CODEC.apply(ByteBufCodecs.collection(HashSet::new)), ClientboundChunkCanvasDataRemovalPacket::templates,
            ClientboundChunkCanvasDataRemovalPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

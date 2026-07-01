package io.github.dennisochulor.canvas_block.network;

import io.github.dennisochulor.canvas_block.CanvasMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundPaintbrushUpdatePacket(int rgb, boolean emissive) implements CustomPacketPayload {
    public static final Type<ServerboundPaintbrushUpdatePacket> TYPE = new Type<>(CanvasMod.id("canvas/update_paintbrush"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPaintbrushUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundPaintbrushUpdatePacket::rgb,
            ByteBufCodecs.BOOL, ServerboundPaintbrushUpdatePacket::emissive,
            ServerboundPaintbrushUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

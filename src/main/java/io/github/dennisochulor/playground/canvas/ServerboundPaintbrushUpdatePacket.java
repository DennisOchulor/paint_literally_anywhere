package io.github.dennisochulor.playground.canvas;

import io.github.dennisochulor.playground.Playground;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundPaintbrushUpdatePacket(int rgb) implements CustomPacketPayload {
    public static final Type<ServerboundPaintbrushUpdatePacket> TYPE = new Type<>(Playground.id("canvas/update_paintbrush"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPaintbrushUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundPaintbrushUpdatePacket::rgb, ServerboundPaintbrushUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

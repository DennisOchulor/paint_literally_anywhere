package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundPaintbrushUpdatePacket(int argb, boolean emissive) implements CustomPacketPayload {
    public static final Type<ServerboundPaintbrushUpdatePacket> TYPE = new Type<>(PLAMod.id("update_paintbrush"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPaintbrushUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundPaintbrushUpdatePacket::argb,
            ByteBufCodecs.BOOL, ServerboundPaintbrushUpdatePacket::emissive,
            ServerboundPaintbrushUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

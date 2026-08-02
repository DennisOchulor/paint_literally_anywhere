package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundPaletteMenuUpdatePacket(int argb) implements CustomPacketPayload {
    public static final Type<ServerboundPaletteMenuUpdatePacket> TYPE = new Type<>(PLAMod.id("update_palette_menu"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPaletteMenuUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundPaletteMenuUpdatePacket::argb,
            ServerboundPaletteMenuUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

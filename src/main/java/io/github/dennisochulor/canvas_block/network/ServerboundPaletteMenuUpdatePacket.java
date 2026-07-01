package io.github.dennisochulor.canvas_block.network;

import io.github.dennisochulor.canvas_block.CanvasMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerboundPaletteMenuUpdatePacket(int rgb) implements CustomPacketPayload {
    public static final Type<ServerboundPaletteMenuUpdatePacket> TYPE = new Type<>(CanvasMod.id("canvas/update_palette_menu"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPaletteMenuUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundPaletteMenuUpdatePacket::rgb,
            ServerboundPaletteMenuUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

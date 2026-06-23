package io.github.dennisochulor.playground.canvas;

import io.github.dennisochulor.playground.Playground;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * So that we don't resend all the data each time there is an update.
 */
public record ClientboundCanvasUpdatePacket(BlockPos pos, Direction side, int index, int color) implements CustomPacketPayload {
    public static final Type<ClientboundCanvasUpdatePacket> TYPE = new Type<>(Playground.id("canvas/update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCanvasUpdatePacket> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, ClientboundCanvasUpdatePacket::pos,
                    Direction.STREAM_CODEC, ClientboundCanvasUpdatePacket::side,
                    ByteBufCodecs.INT, ClientboundCanvasUpdatePacket::index,
                    ByteBufCodecs.INT, ClientboundCanvasUpdatePacket::color,
                    ClientboundCanvasUpdatePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

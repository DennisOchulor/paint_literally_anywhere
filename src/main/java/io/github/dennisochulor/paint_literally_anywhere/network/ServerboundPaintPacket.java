package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockHitResultExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public record ServerboundPaintPacket(QuadTemplate template, BlockPos pos, Vec3 hitPos) implements CustomPacketPayload {
    public static final Type<ServerboundPaintPacket> TYPE = new Type<>(PLAMod.id("paint"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPaintPacket> STREAM_CODEC = StreamCodec.composite(
            QuadTemplate.STREAM_CODEC, ServerboundPaintPacket::template,
            BlockPos.STREAM_CODEC, ServerboundPaintPacket::pos,
            Vec3.STREAM_CODEC, ServerboundPaintPacket::hitPos,
            ServerboundPaintPacket::new
    );

    public ServerboundPaintPacket(BlockHitResult blockHitResult) {
        this(Objects.requireNonNull(((BlockHitResultExt) blockHitResult).pla$clippedQuad()),
                blockHitResult.getBlockPos(), blockHitResult.getLocation());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

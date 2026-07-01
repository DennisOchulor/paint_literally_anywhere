package io.github.dennisochulor.canvas_block.client;

import io.github.dennisochulor.canvas_block.block.CanvasBlockEntity;
import io.github.dennisochulor.canvas_block.network.ClientboundCanvasUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public final class ModClientNetworking {
    private ModClientNetworking() {}

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundCanvasUpdatePacket.TYPE, (packet, context) -> {
            BlockEntity blockEntity = Objects.requireNonNull(context.client().level).getBlockEntity(packet.pos());

            if (blockEntity instanceof CanvasBlockEntity canvas) {
                canvas.setPixel(packet.side(), packet.index(), packet.color(), packet.emissive());
            }
        });
    }
}

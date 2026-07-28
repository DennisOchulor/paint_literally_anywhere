package io.github.dennisochulor.paint_literally_anywhere.client;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.network.ClientboundChunkCanvasDataUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Objects;

public final class ModClientNetworking {
    private ModClientNetworking() {}

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundChunkCanvasDataUpdatePacket.TYPE,
                (payload, context) -> {
                    LevelChunk chunk = Objects.requireNonNull(context.client().level).getChunkAt(payload.pos());
                    ChunkCanvasData.paintClient(chunk, payload);
                }
        );
    }
}

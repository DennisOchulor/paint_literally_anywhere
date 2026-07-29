package io.github.dennisochulor.paint_literally_anywhere.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.ModAttachmentTypes;
import io.github.dennisochulor.paint_literally_anywhere.network.ClientboundChunkCanvasDataInitialSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerChunkSender.class)
abstract class PlayerChunkSenderMixin {
    @WrapOperation(
            method = "sendNextChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/PlayerChunkSender;sendChunk(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;)V"
            )
    )
    private void sendInitialAttachmentData(ServerGamePacketListenerImpl connection, ServerLevel level, LevelChunk chunk, Operation<Void> original, ServerPlayer player) {
        original.call(connection, level, chunk);
        // do a wrap operation so this packet is sent *after* the chunk ones
        // necessary as attachment api caches the serialized data on setAttached()
        // but that is not called cause we need to avoid re-syncing the whole attachment data

        ChunkCanvasData data = chunk.getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);

        if (data != null) {
            ServerPlayNetworking.send(player, new ClientboundChunkCanvasDataInitialSyncPacket(chunk.getPos(), data));
        }
    }
}

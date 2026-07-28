package io.github.dennisochulor.paint_literally_anywhere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.network.ClientboundChunkCanvasDataUpdatePacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ChunkCanvasData(
        Map<BlockPos, List<QuadInstance>> blocks
) {
    public static final Codec<ChunkCanvasData> CODEC = RecordCodecBuilder.create(
            instance ->
                    instance.group(
                            Codec.unboundedMap(BlockPos.CODEC, QuadInstance.CODEC.listOf()
                                            .xmap(list -> (List<QuadInstance>) new ArrayList<>(list), List::copyOf))
                                    .fieldOf("blocks").forGetter(ChunkCanvasData::blocks)
                    ).apply(instance, ChunkCanvasData::new)
    );

    public static final StreamCodec<ByteBuf, ChunkCanvasData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    BlockPos.STREAM_CODEC,
                    QuadInstance.STREAM_CODEC.apply(ByteBufCodecs.list()).map(ArrayList::new, List::copyOf)
            ),
            ChunkCanvasData::blocks, ChunkCanvasData::new
    );



    public static void paintServer(LevelChunk chunk, QuadTemplate template, BlockPos blockPos, Vec3 hitPos, int argb, boolean emissive) {
        if (chunk.getLevel().isClientSide()) throw new IllegalStateException("paintServer called on client!");

        var blocks = chunk.getAttachedOrCreate(ModAttachmentTypes.CHUNK_CANVAS_DATA, () -> new ChunkCanvasData(new HashMap<>())).blocks();
        List<QuadInstance> quads = blocks.computeIfAbsent(blockPos, _ -> new ArrayList<>());
        QuadInstance quad = null;
        for (QuadInstance instance : quads) {
            if (instance.template().equals(template)) {
                quad = instance;
                break;
            }
        }

        if (quad == null) {
            quad = new QuadInstance(template, 16); // todo get from config
            quads.add(quad);
        }

        int indexPainted = quad.paintServer(hitPos, argb, emissive);

        chunk.markUnsaved(); // ensure attachment saves properly since we might not have called setAttached()

        var updatePacket = new ClientboundChunkCanvasDataUpdatePacket(blockPos, template, indexPainted, argb, emissive);
        PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()).forEach(player -> ServerPlayNetworking.send(player, updatePacket));
    }

    public static void paintClient(LevelChunk chunk, ClientboundChunkCanvasDataUpdatePacket packet) {
        if (!chunk.getLevel().isClientSide()) throw new IllegalStateException("paintClient called on server!");

        var blocks = chunk.getAttachedOrCreate(ModAttachmentTypes.CHUNK_CANVAS_DATA, () -> new ChunkCanvasData(new HashMap<>())).blocks();
        List<QuadInstance> quads = blocks.computeIfAbsent(packet.pos(), _ -> new ArrayList<>());
        QuadInstance quad = null;
        for (QuadInstance instance : quads) {
            if (instance.template().equals(packet.template())) {
                quad = instance;
                break;
            }
        }

        if (quad == null) {
            quad = new QuadInstance(packet.template(), 16); // todo get from config
            quads.add(quad);
        }

        quad.paintClient(packet.index(), packet.argb(), packet.emissive());
    }
}

package io.github.dennisochulor.paint_literally_anywhere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.network.ClientboundChunkCanvasDataAddPacket;
import io.github.dennisochulor.paint_literally_anywhere.network.ClientboundChunkCanvasDataUpdatePacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector3fc;

import java.util.*;

public record ChunkCanvasData(
        Map<BlockPos, List<QuadInstance>> blocks
) {
    public static final Codec<ChunkCanvasData> CODEC = RecordCodecBuilder.create(
            instance ->
                    instance.group(
                            new ParallelListMapCodec<>(
                                    BlockPos.CODEC,
                                    QuadInstance.CODEC.listOf().xmap(list -> (List<QuadInstance>) new ArrayList<>(list), List::copyOf),
                                    false
                            ).fieldOf("blocks").forGetter(ChunkCanvasData::blocks)
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



    public static void paintServer(LevelChunk chunk, QuadTemplate template, BlockPos blockPos, Vector3fc localHitPos, int argb, boolean emissive) {
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

        int indexPainted = quad.paintServer(localHitPos, argb, emissive);
        if (indexPainted != -1) {
            chunk.markUnsaved(); // ensure attachment saves properly since we might not have called setAttached()

            var updatePacket = new ClientboundChunkCanvasDataUpdatePacket(blockPos, template, indexPainted, argb, emissive);
            PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()).forEach(player -> ServerPlayNetworking.send(player, updatePacket));
        }
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
        BlockState state = chunk.getBlockState(packet.pos());
        chunk.getLevel().sendBlockUpdated(packet.pos(), state, state, Block.UPDATE_ALL); // trigger chunk rebuild
    }

    public static void addServer(LevelChunk chunk, BlockPos pos, List<QuadInstance> newInstances) {
        if (chunk.getLevel().isClientSide()) throw new IllegalStateException("addServer called on client!");

        ChunkCanvasData data = chunk.getAttachedOrCreate(ModAttachmentTypes.CHUNK_CANVAS_DATA, () -> new ChunkCanvasData(new HashMap<>()));
        List<QuadInstance> currentInstances = data.blocks().computeIfAbsent(pos, _ -> new ArrayList<>());

        BlockState state = chunk.getBlockState(pos);
        Set<QuadTemplate> templates = ((BlockStateBaseExt) state).pla$quads(chunk.getLevel(), pos);
        List<QuadInstance> added = new ArrayList<>();
        for (QuadInstance instance : newInstances) {
            if (templates.contains(instance.template())) {
                currentInstances.add(instance);
                added.add(instance);
            }
        }

        if (!added.isEmpty()) {
            chunk.markUnsaved(); // ensure attachment saves properly
            var packet = new ClientboundChunkCanvasDataAddPacket(pos, added);
            PlayerLookup.tracking((ServerLevel) chunk.getLevel(), pos).forEach(player -> ServerPlayNetworking.send(player, packet));
        }
    }

    public static void addClient(LevelChunk chunk, ClientboundChunkCanvasDataAddPacket packet) {
        if (!chunk.getLevel().isClientSide()) throw new IllegalStateException("addClient called on server!");

        ChunkCanvasData data = chunk.getAttachedOrCreate(ModAttachmentTypes.CHUNK_CANVAS_DATA, () -> new ChunkCanvasData(new HashMap<>()));
        List<QuadInstance> currentInstances = data.blocks().computeIfAbsent(packet.pos(), _ -> new ArrayList<>());

        currentInstances.addAll(packet.instances());

        BlockState state = chunk.getBlockState(packet.pos());
        chunk.getLevel().sendBlockUpdated(packet.pos(), state, state, Block.UPDATE_ALL); // trigger chunk rebuild
    }

    public static void remove(LevelChunk chunk, BlockPos pos, BlockState newState) {
        var data = chunk.getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);

        if (data == null) return;

        var blocks = data.blocks();
        List<QuadInstance> instances = blocks.get(pos);

        if (instances == null) return;

        Set<QuadTemplate> templates = ((BlockStateBaseExt) newState).pla$quads(chunk.getLevel(), pos);
        Iterator<QuadInstance> iterator = instances.listIterator();
        Set<QuadTemplate> removed = new HashSet<>();

        while (iterator.hasNext()) {
            QuadTemplate instanceTemplate = iterator.next().template();

            if (!templates.contains(instanceTemplate)) {
                removed.add(instanceTemplate);
                iterator.remove();
            }
        }

        if (!removed.isEmpty()) {
            if (instances.isEmpty()) {
                blocks.remove(pos);
            }
            if (blocks.isEmpty()) {
                chunk.removeAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);
            }

            if (chunk.getLevel().isClientSide()) {
                chunk.getLevel().sendBlockUpdated(pos, newState, newState, Block.UPDATE_ALL); // trigger chunk rebuild
            }
            else {
                chunk.markUnsaved(); // ensure attachment saves properly since we might not have called removeAttached()
            }
        }
    }

    public static void validateOnChunkLoad(LevelChunk chunk) {
        if (chunk.getLevel().isClientSide()) throw new IllegalStateException("validateOnChunkLoad called on client!");

        var data = chunk.getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);

        if (data == null) return;

        var iterator = data.blocks().entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            BlockPos pos = entry.getKey();
            List<QuadInstance> instances = entry.getValue();

            BlockState state = chunk.getBlockState(pos);
            Set<QuadTemplate> templates = ((BlockStateBaseExt) state).pla$quads(chunk.getLevel(), pos);

            instances.removeIf(quadInstance -> !templates.contains(quadInstance.template()));

            if (instances.isEmpty()) {
                iterator.remove();
            }
        }

        if (data.blocks().isEmpty()) {
            chunk.removeAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);
        }

        // Since this is called on server chunk load, supposedly no clients have been sent the chunk yet.
        // So no manual syncing of anything is needed, let initial sync handle it.
    }
}

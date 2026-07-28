package io.github.dennisochulor.paint_literally_anywhere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

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
}

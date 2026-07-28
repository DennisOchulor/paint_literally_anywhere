package io.github.dennisochulor.paint_literally_anywhere.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.OddCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import java.util.BitSet;

public record QuadInstance(
        QuadTemplate template,
        int rows,
        int cols,
        int[] pixels,
        BitSet emissiveData
) {
    public static final Codec<QuadInstance> CODEC = RecordCodecBuilder.create(
        instance ->
                instance.group(
                        QuadTemplate.CODEC.fieldOf("template").forGetter(QuadInstance::template),
                        Codec.INT.fieldOf("rows").forGetter(QuadInstance::rows),
                        Codec.INT.fieldOf("cols").forGetter(QuadInstance::cols),
                        OddCodecs.INT_ARRAY_CODEC.fieldOf("pixels").forGetter(QuadInstance::pixels),
                        ExtraCodecs.BIT_SET.fieldOf("emissiveData").forGetter(QuadInstance::emissiveData)
                ).apply(instance, QuadInstance::new)
    );

    public static final StreamCodec<ByteBuf, QuadInstance> STREAM_CODEC = StreamCodec.composite(
            QuadTemplate.STREAM_CODEC, QuadInstance::template,
            ByteBufCodecs.INT, QuadInstance::rows,
            ByteBufCodecs.INT, QuadInstance::cols,
            OddCodecs.INT_ARRAY_STREAM_CODEC, QuadInstance::pixels,
            OddCodecs.BIT_SET_STREAM_CODEC, QuadInstance::emissiveData,
            QuadInstance::new
    );
}

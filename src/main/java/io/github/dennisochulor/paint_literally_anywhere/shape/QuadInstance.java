package io.github.dennisochulor.paint_literally_anywhere.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.OddCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

import java.util.BitSet;

public record QuadInstance(
        QuadTemplate template,
        int rows,
        int cols,
        int resolution,
        int[] pixels,
        BitSet emissiveData
) {
    public static final Codec<QuadInstance> CODEC = RecordCodecBuilder.create(
        instance ->
                instance.group(
                        QuadTemplate.CODEC.fieldOf("template").forGetter(QuadInstance::template),
                        Codec.INT.fieldOf("rows").forGetter(QuadInstance::rows),
                        Codec.INT.fieldOf("cols").forGetter(QuadInstance::cols),
                        Codec.INT.fieldOf("resolution").forGetter(QuadInstance::resolution),
                        OddCodecs.INT_ARRAY_CODEC.fieldOf("pixels").forGetter(QuadInstance::pixels),
                        ExtraCodecs.BIT_SET.fieldOf("emissiveData").forGetter(QuadInstance::emissiveData)
                ).apply(instance, QuadInstance::new)
    );

    public static final StreamCodec<ByteBuf, QuadInstance> STREAM_CODEC = StreamCodec.composite(
            QuadTemplate.STREAM_CODEC, QuadInstance::template,
            ByteBufCodecs.INT, QuadInstance::rows,
            ByteBufCodecs.INT, QuadInstance::cols,
            ByteBufCodecs.INT, QuadInstance::resolution,
            OddCodecs.INT_ARRAY_STREAM_CODEC, QuadInstance::pixels,
            OddCodecs.BIT_SET_STREAM_CODEC, QuadInstance::emissiveData,
            QuadInstance::new
    );


    public static int index(int row, int col, int totalCols) {
        return row * totalCols + col;
    }



    public QuadInstance(QuadTemplate template, int resolution) {
        float resPixelLength = 1.0F / resolution;
        int rows = (int) Math.ceil(template.rowVector().length() / resPixelLength);
        int cols = (int) Math.ceil(template.colVector().length() / resPixelLength);
        int numOfPixels = rows * cols;

        this(template, rows, cols, resolution, new int[numOfPixels], new BitSet(numOfPixels));
    }

    /**
     * @return the painted index
     */
    public int paintServer(Vec3 hitPos, int argb, boolean emissive) {
        Vector3fc localHitPos = QuadTemplate.localize(hitPos);

        float colDistance = template.colVector().distance(localHitPos);
        float rowDistance = template.rowVector().distance(localHitPos);
        int col = (int) (colDistance / template.colVector().length() * this.cols);
        int row = (int) (rowDistance / template.rowVector().length() * this.rows);

        int index = index(row, col, this.cols);
        pixels[index] = argb;
        emissiveData.set(index, emissive);

        return index;
    }

    public void paintClient(int index, int argb, boolean emissive) {
        pixels[index] = argb;
        emissiveData.set(index, emissive);
    }
}

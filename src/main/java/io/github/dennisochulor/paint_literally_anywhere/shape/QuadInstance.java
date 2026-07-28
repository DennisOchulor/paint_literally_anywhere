package io.github.dennisochulor.paint_literally_anywhere.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.OddCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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



    public static int index(int row, int col, int totalCols) {
        return row * totalCols + col;
    }



    public QuadInstance(QuadTemplate template, int resolution) {
        float resPixelLength = 1.0F / resolution;
        float templateColLength = template.v0().distance(template.v1());
        float templateRowLength = template.v0().distance(template.v3());

        int rows = (int) Math.ceil(templateRowLength / resPixelLength);
        int cols = (int) Math.ceil(templateColLength / resPixelLength);
        int numOfPixels = rows * cols;

        this(template, rows, cols, new int[numOfPixels], new BitSet(numOfPixels));
    }

    /**
     * @return the painted index
     */
    public int paintServer(Vec3 hitPos, int argb, boolean emissive) {
        Vector3f colVector = new Vector3f();
        Vector3f rowVector = new Vector3f();
        template.v1().sub(template.v0(), colVector);
        template.v3().sub(template.v0(), rowVector);

        Vector3fc localHitPos = new Vector3f((float) (hitPos.x() - Math.floor(hitPos.x())),
                (float) (hitPos.y() - Math.floor(hitPos.y())), (float) (hitPos.z() - Math.floor(hitPos.z())));

        float colDistance = colVector.distance(localHitPos);
        float rowDistance = rowVector.distance(localHitPos);
        int col = (int) (colDistance / colVector.length() * this.cols);
        int row = (int) (rowDistance / rowVector.length() * this.rows);

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

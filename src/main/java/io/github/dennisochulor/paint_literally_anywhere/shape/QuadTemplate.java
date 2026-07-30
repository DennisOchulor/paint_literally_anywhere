package io.github.dennisochulor.paint_literally_anywhere.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record QuadTemplate(
        Vector3fc v0,
        Vector3fc v1,
        Vector3fc v2,
        Vector3fc v3,
        Direction direction,

        // non-serialized data
        Vector3fc rowVector, // goes along the rows
        Vector3fc colVector // goes along the cols
) {
    public static final Codec<QuadTemplate> CODEC = RecordCodecBuilder.create(
            instance ->
                instance.group(
                        ExtraCodecs.VECTOR3F.fieldOf("v0").forGetter(QuadTemplate::v0),
                        ExtraCodecs.VECTOR3F.fieldOf("v1").forGetter(QuadTemplate::v1),
                        ExtraCodecs.VECTOR3F.fieldOf("v2").forGetter(QuadTemplate::v2),
                        ExtraCodecs.VECTOR3F.fieldOf("v3").forGetter(QuadTemplate::v3),
                        Direction.CODEC.fieldOf("direction").forGetter(QuadTemplate::direction)
                ).apply(instance, (v0, v1, v2, v3, dir) -> ShapeUtil.cache(new QuadTemplate(v0, v1, v2, v3, dir)))
    );

    public static final StreamCodec<ByteBuf, QuadTemplate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, QuadTemplate::v0,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v1,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v2,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v3,
            Direction.STREAM_CODEC, QuadTemplate::direction,
            (v0, v1, v2, v3, dir) -> ShapeUtil.cache(new QuadTemplate(v0, v1, v2, v3, dir))
    );


    public static Vector3f localize(Vec3 vec) {
        float xAbsolute = (float) Math.abs(vec.x());
        float yAbsolute = (float) Math.abs(vec.y());
        float zAbsolute = (float) Math.abs(vec.z());

        return new Vector3f((float) (xAbsolute - Math.floor(xAbsolute)),
                (float) (yAbsolute - Math.floor(yAbsolute)), (float) (zAbsolute - Math.floor(zAbsolute)));
    }


    public QuadTemplate(Vector3fc v0, Vector3fc v1, Vector3fc v2, Vector3fc v3, Direction direction) {
        Vector3f colVector = new Vector3f();
        Vector3f rowVector = new Vector3f();
        v1.sub(v0, colVector);
        v3.sub(v0, rowVector);

        this(v0, v1, v2, v3, direction, rowVector, colVector);
    }


    public boolean clip(Vec3 hitPos) {
        Vector3fc localHitPos = localize(hitPos);

        // 2D cross product of each directed edge from A to B, and hit point P
        float[] products = new float[4];
        products[0] = (v1.x() - v0.x()) * (localHitPos.y() - v0.y()) - (v1.y() - v0.y()) * (localHitPos.x() - v0.x());
        products[1] = (v2.x() - v1.x()) * (localHitPos.y() - v1.y()) - (v2.y() - v1.y()) * (localHitPos.x() - v1.x());
        products[2] = (v3.x() - v2.x()) * (localHitPos.y() - v2.y()) - (v3.y() - v2.y()) * (localHitPos.x() - v2.x());
        products[3] = (v0.x() - v3.x()) * (localHitPos.y() - v3.y()) - (v0.y() - v3.y()) * (localHitPos.x() - v3.x());

        boolean positive = products[0] > 0;
        for (int i = 0; i < products.length; i++) {
            float p = products[i];
            if (p == 0) return true;
            if (p > 0 && !positive) return false;
            if (p < 0 && positive) return false;
        }

        return true;
    }
}

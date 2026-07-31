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


    public Vector3f localize(Vec3 vec) {
        float x = (float) Math.abs((Math.abs(vec.x()) - Math.abs(Math.floor(vec.x()))));
        float y = (float) Math.abs((Math.abs(vec.y()) - Math.abs(Math.floor(vec.y()))));
        float z = (float) Math.abs((Math.abs(vec.z()) - Math.abs(Math.floor(vec.z()))));

        // Account for both most +ve and -ve of each axis being 0
        if (x == 0 && direction == Direction.EAST) x = 1;
        if (y == 0 && direction == Direction.UP) y = 1;
        if (z == 0 && direction == Direction.SOUTH) z = 1;

        return new Vector3f(x, y, z);
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

        // check if hitPos is on the quad's plane
        Vector3fc planeNormal = colVector.cross(v2.sub(v0, new Vector3f()), new Vector3f());
        float distance = localHitPos.sub(v0, new Vector3f()).dot(planeNormal);

        if (Math.abs(distance) > 0.0001F) {
            return false;
        }

        //noinspection UnnecessaryLocalVariable
        Vector3fc e1 = colVector;
        Vector3fc e2 = v2.sub(v1, new Vector3f());
        Vector3fc e3 = v3.sub(v2, new Vector3f());
        Vector3fc e4 = v0.sub(v3, new Vector3f());

        Vector3fc v0ToHitPos = localHitPos.sub(v0, new Vector3f());
        Vector3fc v1ToHitPos = localHitPos.sub(v1, new Vector3f());
        Vector3fc v2ToHitPos = localHitPos.sub(v2, new Vector3f());
        Vector3fc v3ToHitPos = localHitPos.sub(v3, new Vector3f());

        float dot0 = e1.cross(v0ToHitPos, new Vector3f()).dot(planeNormal);
        float dot1 = e2.cross(v1ToHitPos, new Vector3f()).dot(planeNormal);
        float dot2 = e3.cross(v2ToHitPos, new Vector3f()).dot(planeNormal);
        float dot3 = e4.cross(v3ToHitPos, new Vector3f()).dot(planeNormal);

        return dot0 >= 0 && dot1 >= 0 && dot2 >= 0 && dot3 >= 0;
    }
}

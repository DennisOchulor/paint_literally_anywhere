package io.github.dennisochulor.paint_literally_anywhere.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

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
                ).apply(instance, QuadTemplate::create)
    );

    public static final StreamCodec<ByteBuf, QuadTemplate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, QuadTemplate::v0,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v1,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v2,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v3,
            Direction.STREAM_CODEC, QuadTemplate::direction,
            QuadTemplate::create
    );


    /**
     * @return the cached {@link QuadTemplate}, always prefer this method instead of using the constructor directly.
     */
    public static QuadTemplate create(Vector3fc v0, Vector3fc v1, Vector3fc v2, Vector3fc v3, Direction direction) {
        Vector3f colVector = new Vector3f();
        Vector3f rowVector = new Vector3f();
        v1.sub(v0, colVector);
        v3.sub(v0, rowVector);

        QuadTemplate template = new QuadTemplate(v0, v1, v2, v3, direction, rowVector, colVector);
        return ShapeUtil.cache(template);
    }

    public @Nullable BlockHitResult clip(Vector3fc from, Vector3fc to, BlockPos pos, Vector3fc offset) {
        // find intersection between a line (from/to) and this quad
        Vector3fc v0 = unlocalize(this.v0, pos).add(offset, new Vector3f());
        Vector3fc v1 = unlocalize(this.v1, pos).add(offset, new Vector3f());
        Vector3fc v2 = unlocalize(this.v2, pos).add(offset, new Vector3f());
        Vector3fc v3 = unlocalize(this.v3, pos).add(offset, new Vector3f());

        // (v1 - v0) x (v2 - v0)
        Vector3fc normal = v1.sub(v0, new Vector3f()).cross(v2.sub(v0, new Vector3f()));

        // (n . (v0 - p0)) / (n . (p1 - p0))
        Vector3fc fromToVec = to.sub(from, new Vector3f());
        float denom = normal.dot(fromToVec);
        if (denom == 0) {
            return null;
        }

        float t = normal.dot(v0.sub(from, new Vector3f())) / denom;
        if (t < 0 || t > 1) {
            return null;
        }

        // from + t(to - from)
        Vector3fc intersect = from.add(fromToVec.mul(t, new Vector3f()), new Vector3f());

        // for all edges, (vi+1 - vi) x (intersect - vi)
        Vector3fc c0 = v1.sub(v0, new Vector3f()).cross(intersect.sub(v0, new Vector3f()));
        Vector3fc c1 = v2.sub(v1, new Vector3f()).cross(intersect.sub(v1, new Vector3f()));
        Vector3fc c2 = v3.sub(v2, new Vector3f()).cross(intersect.sub(v2, new Vector3f()));
        Vector3fc c3 = v0.sub(v3, new Vector3f()).cross(intersect.sub(v3, new Vector3f()));

        boolean clip = normal.dot(c0) >= 0 && normal.dot(c1) >= 0 && normal.dot(c2) >= 0 && normal.dot(c3) >= 0;
        if (!clip) {
            return null;
        }

        Vector3fc delta = from.sub(to, new Vector3f());
        Direction dir = Direction.getApproximateNearest(delta.x(), delta.y(), delta.z());
        // hitResult should use no offset values
        BlockHitResult result = new BlockHitResult(new Vec3(intersect.sub(offset, new Vector3f())), dir, pos, true);
        ((BlockHitResultExt) result).pla$setClippedQuad(this);
        return result;
    }



    // pos is needed as some blocks extend outside the normal block range
    // so vec could be referring to a neighbor pos
    public static Vector3fc localize(Vec3 vec, BlockPos pos) {
        float x = (float) vec.x() - pos.getX();
        float y = (float) vec.y() - pos.getY();
        float z = (float) vec.z() - pos.getZ();

        return new Vector3f(x, y, z);
    }

    public static Vector3fc unlocalize(Vector3fc vec, BlockPos pos) {
        float x = pos.getX() + vec.x();
        float y = pos.getY() + vec.y();
        float z = pos.getZ() + vec.z();

        return new Vector3f(x, y, z);
    }
}

package io.github.dennisochulor.paint_literally_anywhere.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public record QuadTemplate(
        Vector3fc v0,
        Vector3fc v1,
        Vector3fc v2,
        Vector3fc v3
) {
    public static final Codec<QuadTemplate> CODEC = RecordCodecBuilder.create(
            instance ->
                instance.group(
                        ExtraCodecs.VECTOR3F.fieldOf("v0").forGetter(QuadTemplate::v0),
                        ExtraCodecs.VECTOR3F.fieldOf("v1").forGetter(QuadTemplate::v1),
                        ExtraCodecs.VECTOR3F.fieldOf("v2").forGetter(QuadTemplate::v2),
                        ExtraCodecs.VECTOR3F.fieldOf("v3").forGetter(QuadTemplate::v3)
                ).apply(instance, (v0, v1, v2, v3) -> ShapeUtil.cache(new QuadTemplate(v0, v1, v2, v3)))
    );

    public static final StreamCodec<ByteBuf, QuadTemplate> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, QuadTemplate::v0,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v1,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v2,
            ByteBufCodecs.VECTOR3F, QuadTemplate::v3,
            (v0, v1, v2, v3) -> ShapeUtil.cache(new QuadTemplate(v0, v1, v2, v3))
    );



    public boolean clip(Vec3 hitPos) {
        return false;
    }
}

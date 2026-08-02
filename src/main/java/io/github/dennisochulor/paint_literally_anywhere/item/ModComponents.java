package io.github.dennisochulor.paint_literally_anywhere.item;

import com.mojang.serialization.Codec;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;

public final class ModComponents {
    private ModComponents() {}

    public static final DataComponentType<Integer> ARGB_COLOR = register(
            "argb_color",
            Codec.INT,
            ByteBufCodecs.INT
    );

    public static final DataComponentType<Unit> EMISSIVE = register(
            "emissive",
            Unit.CODEC,
            Unit.STREAM_CODEC
    );



    public static void init() {}

    private static <T> DataComponentType<T> register(String name, Codec<T> codec, StreamCodec<ByteBuf, T> streamCodec) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                PLAMod.id("component/" + name),
                DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build()
        );
    }
}

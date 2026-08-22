package io.github.dennisochulor.paint_literally_anywhere.item;

import com.mojang.serialization.Codec;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;

public final class ModComponents {
    private ModComponents() {}

    public static final DataComponentType<PaintBrushProperties> PAINT_BRUSH = register(
            "paint_brush",
            PaintBrushProperties.CODEC,
            PaintBrushProperties.STREAM_CODEC
    );



    public static void init() {}

    @SuppressWarnings("SameParameterValue")
    private static <T> DataComponentType<T> register(String name, Codec<T> codec, StreamCodec<ByteBuf, T> streamCodec) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                PLAMod.id("component/" + name),
                DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build()
        );
    }
}

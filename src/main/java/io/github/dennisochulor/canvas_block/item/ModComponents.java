package io.github.dennisochulor.canvas_block.item;

import com.mojang.serialization.Codec;
import io.github.dennisochulor.canvas_block.CanvasMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;

public final class ModComponents {
    private ModComponents() {}

    public static final DataComponentType<Integer> RGB_COLOR = register(
            "rgb_color",
            ExtraCodecs.RGB_COLOR_CODEC,
            ByteBufCodecs.RGB_COLOR
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
                CanvasMod.id("component/" + name),
                DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build()
        );
    }
}

package io.github.dennisochulor.paint_literally_anywhere;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;

public final class OddCodecs {
    private OddCodecs() {}

    public static final Codec<int[]> INT_ARRAY_CODEC = Codec.of(
            new Encoder<>() {
                @Override
                public <T> DataResult<T> encode(int[] input, DynamicOps<T> ops, T prefix) {
                    return DataResult.success(ops.createIntList(Arrays.stream(input)));
                }
            },
            new Decoder<>() {
                @Override
                public <T> DataResult<Pair<int[], T>> decode(DynamicOps<T> ops, T input) {
                    DataResult<IntStream> dataResult = ops.getIntStream(input);

                    if (dataResult.isSuccess()) {
                        return DataResult.success(Pair.of(dataResult.getOrThrow().toArray(), ops.empty()));
                    }
                    else {
                        return DataResult.error(() -> dataResult.error().orElseThrow().message());
                    }
                }
            }
    );

    public static final StreamCodec<ByteBuf, int[]> INT_ARRAY_STREAM_CODEC = StreamCodec.of(
            (output, value) -> {
                output.writeInt(value.length);
                for (int i : value) {
                    output.writeInt(i);
                }
            },
            input -> {
                int[] arr = new int[input.readInt()];

                for (int i = 0; i < arr.length; i++) {
                    arr[i] = input.readInt();
                }
                return arr;
            }
    );

    public static final StreamCodec<ByteBuf, BitSet> BIT_SET_STREAM_CODEC = StreamCodec.of(
            (output, value) -> FriendlyByteBuf.writeByteArray(output, value.toByteArray()),
            input -> BitSet.valueOf(FriendlyByteBuf.readByteArray(input))
    );
}

package io.github.dennisochulor.paint_literally_anywhere;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.BitSet;
import java.util.Optional;
import java.util.stream.Stream;

public final class OddCodecs {
    private OddCodecs() {}

    public static final Codec<int[]> INT_ARRAY_CODEC = Codec.of(
            /*
            new Encoder<>() {
                @Override
                public <T> DataResult<T> encode(int[] input, DynamicOps<T> ops, T prefix) {
                    return DataResult.success(ops.createIntList(Arrays.stream(input)));
                }
            },
            new Decoder<>() {
                @Override
                public <T> DataResult<Pair<int[], T>> decode(DynamicOps<T> ops, T input) {
                    DataResult<IntStream> intStreamDataResult = ops.getIntStream(input);

                    if (intStreamDataResult.isSuccess()) {
                        return DataResult.success(Pair.of(intStreamDataResult.getOrThrow().toArray(), ops.empty()));
                    }
                    else {
                        return DataResult.error(() -> "int array not present!");
                    }
                }
            }
             */

            new Encoder<>() {
                @Override
                public <T> DataResult<T> encode(int[] input, DynamicOps<T> ops, T prefix) {
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(() -> "QuadInstance", PLAMod.LOGGER)) {
                        TagValueOutput valueOutput = TagValueOutput.createWithoutContext(reporter);
                        valueOutput.putIntArray("int_array", input);
                        return DataResult.success(NbtOps.INSTANCE.convertTo(ops, valueOutput.buildResult()));
                    }
                }
            },
            new Decoder<>() {
                @Override
                public <T> DataResult<Pair<int[], T>> decode(DynamicOps<T> ops, T input) {
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(() -> "QuadInstance", PLAMod.LOGGER)) {
                        ValueInput valueInput = TagValueInput.create(reporter, HolderLookup.Provider.create(Stream.empty()), (CompoundTag) ops.convertTo(NbtOps.INSTANCE, input));
                        Optional<int[]> optionalInts = valueInput.getIntArray("int_array");

                        if (optionalInts.isPresent()) return DataResult.success(Pair.of(optionalInts.get(), ops.empty()));
                        else return DataResult.error(() -> "int array not present!");
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
            (output, value) -> output.writeBytes(value.toByteArray()),
            input -> BitSet.valueOf(FriendlyByteBuf.readByteArray(input))
    );
}

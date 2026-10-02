package io.github.dennisochulor.paint_literally_anywhere.shape;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.OddCodecs;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.BitStorage;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SimpleBitStorage;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;
import java.util.Objects;

public class PixelData {
    public static final Codec<PixelData> CODEC = RecordCodecBuilder.create(
            instance ->
                    instance.group(
                            Codec.INT.fieldOf("rows").forGetter(data -> data.rows),
                            Codec.INT.fieldOf("cols").forGetter(data -> data.cols),
                            ExtraCodecs.BIT_SET.fieldOf("emissiveData").forGetter(data -> data.emissiveData),
                            ARGBData.CODEC.fieldOf("argbData").forGetter(data -> data.argbData)
                    ).apply(instance, PixelData::new)
    );
    public static final StreamCodec<ByteBuf, PixelData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, data -> data.rows,
            ByteBufCodecs.INT, data -> data.cols,
            OddCodecs.BIT_SET_STREAM_CODEC, data -> data.emissiveData,
            ARGBData.STREAM_CODEC, data -> data.argbData,
            PixelData::new
    );

    private final int rows;
    private final int cols;
    private final BitSet emissiveData;
    private ARGBData argbData;

    public PixelData(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        int size = rows * cols;
        this.emissiveData = new BitSet(size);
        this.argbData = size <= 128 ? new ARGBData.FlatARGBData(size) : new ARGBData.PalettedARGBData(size);
    }

    private PixelData(int rows, int cols, BitSet emissiveData, ARGBData argbData) {
        this.rows = rows;
        this.cols = cols;
        this.emissiveData = emissiveData;
        this.argbData = argbData;
    }

    public boolean isEmissive(int index) {
        return emissiveData.get(index);
    }

    public boolean isEmissive(int row, int col) {
        return isEmissive(index(row, col));
    }

    public void setEmissive(int index, boolean emissive) {
        emissiveData.set(index, emissive);
    }

    @SuppressWarnings("unused")
    public void setEmissive(int row, int col, boolean emissive) {
        setEmissive(index(row, col), emissive);
    }


    public int getARGB(int index) {
        return argbData.getARGB(index);
    }

    public int getARGB(int row, int col) {
        return getARGB(index(row, col));
    }

    public void setARGB(int index, int argb) {
        ARGBData newData = argbData.setARGB(index, argb);

        if (newData != null) {
            argbData = newData;
        }
    }

    @SuppressWarnings("unused")
    public void setARGB(int row, int col, int argb) {
        setARGB(index(row, col), argb);
    }


    public int size() {
        return rows * cols;
    }


    private int index(int row, int col) {
        return row * cols + col;
    }



    private sealed interface ARGBData {
        int FLAT = 0;
        int PALETTED = 1;
        Codec<ARGBData> CODEC = Codec.of(
                new Encoder<>() {
                    @Override
                    public <T> DataResult<T> encode(ARGBData input, DynamicOps<T> ops, T prefix) {
                        switch (input) {
                            case FlatARGBData flatARGBData -> {
                                return ops.mapBuilder()
                                        .add("type", ops.createInt(FLAT))
                                        .add("data", FlatARGBData.CODEC.encode(flatARGBData, ops, prefix))
                                        .build(prefix);
                            }
                            case PalettedARGBData palettedARGBData -> {
                                return ops.mapBuilder()
                                        .add("type", ops.createInt(PALETTED))
                                        .add("data", PalettedARGBData.CODEC.encode(palettedARGBData, ops, prefix))
                                        .build(prefix);
                            }
                        }
                    }
                },
                new Decoder<>() {
                    @Override
                    public <T> DataResult<Pair<ARGBData, T>> decode(DynamicOps<T> ops, T input) {
                        MapLike<T> map = ops.getMap(input).getOrThrow();
                        int type = ops.getNumberValue(Objects.requireNonNull(map.get("type"))).map(Number::intValue).getOrThrow();
                        T data = map.get("data");

                        return switch (type) {
                            case FLAT -> {
                                ARGBData argbData = FlatARGBData.CODEC.decode(ops, data).getOrThrow().getFirst();
                                yield DataResult.success(Pair.of(argbData, ops.empty()));
                            }
                            case PALETTED -> {
                                ARGBData argbData = PalettedARGBData.CODEC.decode(ops, data).getOrThrow().getFirst();
                                yield DataResult.success(Pair.of(argbData, ops.empty()));
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + type);
                        };
                    }
                }
        );

        StreamCodec<ByteBuf, ARGBData> STREAM_CODEC = StreamCodec.of(
                (output, argbData) -> {
                    switch (argbData) {
                        case FlatARGBData flatARGBData -> {
                            output.writeInt(FLAT);
                            FlatARGBData.STREAM_CODEC.encode(output, flatARGBData);
                        }
                        case PalettedARGBData palettedARGBData -> {
                            output.writeInt(PALETTED);
                            PalettedARGBData.STREAM_CODEC.encode(output, palettedARGBData);
                        }
                    }
                },

                input -> {
                    int type = input.readInt();

                    return switch (type) {
                        case FLAT -> FlatARGBData.STREAM_CODEC.decode(input);
                        case PALETTED -> PalettedARGBData.STREAM_CODEC.decode(input);
                        default -> throw new IllegalStateException("Unexpected value: " + type);
                    };
                }
        );


        int getARGB(int index);
        @Nullable ARGBData setARGB(int index, int argb);


        final class PalettedARGBData implements ARGBData {
            private static final Codec<PalettedARGBData> CODEC = RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                    OddCodecs.INT_ARRAY_CODEC.fieldOf("palette").forGetter(data -> data.palette.toIntArray()),
                                    OddCodecs.LONG_ARRAY_CODEC.fieldOf("bitStorage").forGetter(data -> data.bitStorage.getRaw()),
                                    Codec.INT.fieldOf("bits").forGetter(data -> data.bitStorage.getBits()),
                                    Codec.INT.fieldOf("size").forGetter(data -> data.bitStorage.getSize())
                            ).apply(instance, PalettedARGBData::new)
            );

            private static final StreamCodec<ByteBuf, PalettedARGBData> STREAM_CODEC = StreamCodec.composite(
                    OddCodecs.INT_ARRAY_STREAM_CODEC, data -> data.palette.toIntArray(),
                    ByteBufCodecs.LONG_ARRAY, data -> data.bitStorage.getRaw(),
                    ByteBufCodecs.INT, data -> data.bitStorage.getBits(),
                    ByteBufCodecs.INT, data -> data.bitStorage.getSize(),
                    PalettedARGBData::new
            );

            private static final float SWITCH_THRESHOLD = 0.1F;

            private final Int2IntMap argbToIdMap;
            private final IntList palette;
            private BitStorage bitStorage;

            PalettedARGBData(int size) {
                // default to 3 bits which is 8 unique colors in the palette
                argbToIdMap = new Int2IntOpenHashMap(8);
                argbToIdMap.put(PaintBrushProperties.EMPTY_COLOR, 0);
                palette = new IntArrayList(8);
                palette.add(PaintBrushProperties.EMPTY_COLOR);
                bitStorage = new SimpleBitStorage(3, size);
            }

            private PalettedARGBData(int[] ints, long[] rawBits, int bits, int size) {
                palette = new IntArrayList(ints);
                bitStorage = new SimpleBitStorage(bits, size, rawBits);

                argbToIdMap = new Int2IntOpenHashMap(palette.size());
                for (int i = 0; i < ints.length; i++) {
                    argbToIdMap.put(ints[i], i);
                }
            }

            @Override
            public int getARGB(int index) {
                int id = bitStorage.get(index);
                return palette.getInt(id);
            }

            @Override
            public @Nullable ARGBData setARGB(int index, int argb) {
                int id = argbToIdMap.computeIfAbsent(argb, _ -> {
                    int newId = palette.size();
                    palette.add(argb);
                    return newId;
                });

                // switch to flat if palette size exceeds threshold
                if ((float) palette.size() / bitStorage.getSize() > SWITCH_THRESHOLD) {
                    int[] arr = new int[bitStorage.getSize()];
                    bitStorage.unpack(arr);

                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = palette.getInt(arr[i]);
                    }

                    arr[index] = argb;
                    return new FlatARGBData(arr);
                }

                // resize BitStorage with one extra bit per entry
                if (id >= 1 << bitStorage.getBits()) {
                    int[] values = new int[bitStorage.getSize()];
                    bitStorage.unpack(values);
                    bitStorage = new SimpleBitStorage(bitStorage.getBits() + 1, bitStorage.getSize(), values);
                }

                bitStorage.set(index, id);
                return null;
            }
        }


        final class FlatARGBData implements ARGBData {
            static final Codec<FlatARGBData> CODEC = RecordCodecBuilder.create(
                    instance ->
                            instance.group(
                                    OddCodecs.INT_ARRAY_CODEC.fieldOf("data").forGetter(data -> data.data)
                            ).apply(instance, FlatARGBData::new)
            );

            static final StreamCodec<ByteBuf, FlatARGBData> STREAM_CODEC = StreamCodec.composite(
                    OddCodecs.INT_ARRAY_STREAM_CODEC, data -> data.data,
                    FlatARGBData::new
            );

            private final int[] data;

            FlatARGBData(int size) {
                data = new int[size];
            }

            FlatARGBData(int[] arr) {
                data = arr;
            }

            @Override
            public int getARGB(int index) {
                return data[index];
            }

            @Override
            public @Nullable ARGBData setARGB(int index, int argb) {
                data[index] = argb;
                return null;
            }
        }
    }
}

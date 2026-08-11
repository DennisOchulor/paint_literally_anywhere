package io.github.dennisochulor.paint_literally_anywhere;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ParallelListMapCodec<K, V>(
        Codec<K> keyCodec,
        Codec<V> valueCodec,
        boolean immutable
) implements Codec<Map<K, V>> {
    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
        var mapLikeResult = ops.getMap(input);
        if (mapLikeResult.isError()) {
            return DataResult.error(() -> mapLikeResult.error().orElseThrow().message());
        }

        var mapAsList = mapLikeResult.getOrThrow().entries().toList();
        var firstEntryTagResult = ops.getStringValue(mapAsList.getFirst().getFirst());
        if (firstEntryTagResult.isError()) {
            return DataResult.error(() -> firstEntryTagResult.error().orElseThrow().message());
        }

        boolean keysIsFirst = firstEntryTagResult.getOrThrow().equals("keys");
        var keyList = mapAsList.get(keysIsFirst ? 0 : 1).getSecond();
        var valueList = mapAsList.get(keysIsFirst ? 1 : 0).getSecond();

        var keyListResult = ops.getList(keyList);
        if (keyListResult.isError()) {
            return DataResult.error(() -> keyListResult.error().orElseThrow().message());
        }
        var valueListResult = ops.getList(valueList);
        if (valueListResult.isError()) {
            return DataResult.error(() -> valueListResult.error().orElseThrow().message());
        }

        List<T> keys = new ArrayList<>();
        List<T> vals = new ArrayList<>();
        keyListResult.getOrThrow().accept(keys::add);
        valueListResult.getOrThrow().accept(vals::add);

        if (keys.size() != vals.size()) {
            return DataResult.error(() -> "List sizes differ!");
        }

        Map<K, V> resultMap = HashMap.newHashMap(keys.size());
        StringBuilder errorBuilder = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            var keyResult = keyCodec.parse(ops, keys.get(i));
            var valResult = valueCodec.parse(ops, vals.get(i));

            if (keyResult.isError() || valResult.isError()) {
                errorBuilder.append("Error decoding map entry #").append(i).append(":\n");
                if (keyResult.isError()) {
                    errorBuilder.append("KeyError: ").append(keyResult.error().orElseThrow().message()).append("\n");
                }
                if (valResult.isError()) {
                    errorBuilder.append("ValueError: ").append(valResult.error().orElseThrow().message()).append("\n");
                }
                errorBuilder.append("\n");
            }
            else {
                resultMap.put(keyResult.getOrThrow(), valResult.getOrThrow());
            }
        }

        var finalMap = immutable ? Map.copyOf(resultMap) : resultMap;
        var finalPair = Pair.of(finalMap, ops.empty());
        return errorBuilder.isEmpty() ? DataResult.success(finalPair) : DataResult.error(errorBuilder::toString, finalPair);
    }

    @Override
    public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
        var keyList = ops.listBuilder();
        var valueList = ops.listBuilder();

        for (var entry : input.entrySet()) {
            keyList.add(keyCodec.encodeStart(ops, entry.getKey()));
            valueList.add(valueCodec.encodeStart(ops, entry.getValue()));
        }

        return ops.mapBuilder().add("keys", keyList.build(prefix)).add("values", valueList.build(prefix)).build(prefix);
    }
}

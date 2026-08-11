package io.github.dennisochulor.paint_literally_anywhere.shape.parser;

import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ShapeFileParseResult {
    public static final ShapeFileParseResult EMPTY = new ShapeFileParseResult(Map.of());

    /**
     * Merges the two, prioritizing entries from {@code map} if they are clashes.
     */
    public static ShapeFileParseResult merge(ShapeFileParseResult result, Map<String, ShapeFile> map) {
        Map<String, ShapeFile> newMap = new HashMap<>(result.shapeFiles);
        newMap.putAll(map);
        return new ShapeFileParseResult(newMap);
    }

    // namespace -> ShapeFile
    private final Map<String, ShapeFile> shapeFiles;

    ShapeFileParseResult(Map<String, ShapeFile> map) {
        shapeFiles = Map.copyOf(map);
    }

    public @Nullable Set<QuadTemplate> get(BlockState state) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        ShapeFile shapeFile = shapeFiles.get(id.getNamespace());

        if (shapeFile == null) {
            return null;
        }

        List<QuadTemplate> templates = new ArrayList<>();
        int[] templateIndexes = shapeFile.blockStates().get(state);

        if (templateIndexes == null) {
            return null;
        }

        for (int index : templateIndexes) {
            templates.add(shapeFile.templates().get(index));
        }

        return Set.copyOf(templates);
    }

    public Map<String, ModMetadata> namespacesToGenerate() {
        Map<String, ModMetadata> needsRegen = new HashMap<>();
        Set<String> allNamespacesWithBlocks = BuiltInRegistries.BLOCK.keySet().stream().map(Identifier::getNamespace).collect(Collectors.toUnmodifiableSet());

        allNamespacesWithBlocks.forEach(namespace -> {
            ModContainer modContainer = FabricLoader.getInstance().getModContainer(namespace).orElseThrow();
            String version = modContainer.getMetadata().getVersion().getFriendlyString();
            ShapeFile shapeFile = shapeFiles.get(namespace);

            if (shapeFile == null ||
                    shapeFile.schemaVersion() != ShapeFile.LATEST_SCHEMA_VERSION ||
                    !shapeFile.namespaceVersion().equals(version)
            ) {
                needsRegen.put(namespace, modContainer.getMetadata());
            }
        });

        return Map.copyOf(needsRegen);
    }
}

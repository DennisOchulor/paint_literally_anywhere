package io.github.dennisochulor.paint_literally_anywhere.client.model.generator;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.client.mixin.BlockStateModelSetAccessor;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFile;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParseResult;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class ShapeFileGenerator {
    private ShapeFileGenerator() {}

    public static ShapeFileParseResult generate(Minecraft minecraft, ShapeFileParseResult currentResult) {
        Map<String, ModMetadata> namespacesToGenerate = currentResult.namespacesToGenerate();
        Map<BlockState, BlockStateModel> stateToModel = ((BlockStateModelSetAccessor) minecraft.getModelManager().getBlockStateModelSet()).pla$modelByState();

        if (namespacesToGenerate.isEmpty()) {
            return currentResult;
        }

        long startTime = Util.getMillis();
        Map<String, ShapeFile.Builder> builders = HashMap.newHashMap(namespacesToGenerate.size());
        namespacesToGenerate.values().forEach(modMetadata -> {
            builders.put(modMetadata.getId(), new ShapeFile.Builder(modMetadata.getId(), modMetadata.getVersion().getFriendlyString()));
        });

        FakeQuadEmitter quadEmitter = new FakeQuadEmitter();
        RandomSource random = RandomSource.createThreadLocalInstance();

        for (var entry : stateToModel.entrySet()) {
            BlockState state = entry.getKey();
            BlockStateModel model = entry.getValue();
            String namespace = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();

            if (!namespacesToGenerate.containsKey(namespace)) continue;

            model.emitQuads(quadEmitter, BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, random, _ -> false);
            List<QuadTemplate> templates = quadEmitter.getAndClearTemplates();

            // some blocks like chests/banners emit no block quads by default because they render almost entirely via BER
            // so just let these blocks fallback to the VoxelShape at runtime
            if (!templates.isEmpty()) {
                builders.get(namespace).add(state, templates);
            }
        }

        Map<String, ShapeFile> shapeFiles = builders.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().build()));

        writeShapeFiles(shapeFiles.values(), minecraft.gameDirectory.toPath().resolve(ShapeFileParser.RELATIVE_PATH_TO_SHAPES_DIR));

        ShapeFileParseResult newResult = ShapeFileParseResult.merge(currentResult, shapeFiles);

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Set<String> remainderNamespaces = newResult.namespacesToGenerate().keySet();

            if (!remainderNamespaces.isEmpty()) {
                StringBuilder sb = new StringBuilder("Still got namespaces with ungenerated shape files!\n");
                remainderNamespaces.forEach(namespace -> sb.append(namespace).append(", "));
                throw new IllegalStateException(sb.toString());
            }
        }

        PLAMod.LOGGER.info("Took {} ms to generate {} shape files for the namespaces: {}",
                Util.getMillis() - startTime, shapeFiles.size(), shapeFiles.keySet().stream().reduce((s1, s2) -> s1 + ", " + s2).orElse("<none>"));
        return newResult;
    }

    private static void writeShapeFiles(Collection<ShapeFile> shapeFiles, Path shapesFolder) {
        try {
            Files.createDirectories(shapesFolder);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        for (ShapeFile shapeFile : shapeFiles) {
            ShapeFile.CODEC.encode(shapeFile, NbtOps.INSTANCE, NbtOps.INSTANCE.empty())
                    .ifSuccess(tag -> {
                        try {
                            NbtIo.writeCompressed(tag.asCompound().orElseThrow(), shapesFolder.resolve(shapeFile.namespace() + ".dat"));
                        }
                        catch (IOException e) {
                            PLAMod.LOGGER.warn("Failed to write shape file for {}!", shapeFile.namespace(), e);
                        }
                    })
                    .ifError(err -> PLAMod.LOGGER.warn("Failed to encode shape file {}!\n{}", shapeFile.namespace(), err.message()));
        }
    }
}

package io.github.dennisochulor.paint_literally_anywhere.client.model.generator;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.client.mixin.BlockStateModelSetAccessor;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFile;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParseResult;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
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
        Map<String, ModMetadata> namespacesToGenerate = currentResult.missingNamespaces(true);
        Map<BlockState, BlockStateModel> stateToModel = ((BlockStateModelSetAccessor) minecraft.getModelManager().getBlockStateModelSet()).pla$modelByState();

        if (namespacesToGenerate.isEmpty()) {
            return currentResult;
        }

        long startTime = Util.getMillis();
        Map<String, ShapeFile.Builder> builders = HashMap.newHashMap(namespacesToGenerate.size());
        namespacesToGenerate.forEach((namespace, metadata) -> {
            builders.put(namespace, new ShapeFile.Builder(namespace, metadata.getVersion().getFriendlyString()));
        });

        FakeQuadEmitter quadEmitter = new FakeQuadEmitter();
        RandomSource random = RandomSource.createThreadLocalInstance();

        for (var entry : stateToModel.entrySet()) {
            BlockState state = entry.getKey();
            BlockStateModel model = entry.getValue();
            String namespace = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();

            if (!namespacesToGenerate.containsKey(namespace)) continue;
            // needed because of vanilla's hacks in BlockStateDefinitions for item frames, thanks Mojank
            if (state.isAir()) continue;

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

        List<String> writtenFiles = writeShapeFiles(shapeFiles.values(), minecraft.gameDirectory.toPath().resolve(ShapeFileParser.RELATIVE_PATH_TO_SHAPES_DIR));

        ShapeFileParseResult newResult = ShapeFileParseResult.merge(currentResult, shapeFiles);
        Set<String> remainderNamespaces = newResult.missingNamespaces(true).keySet();

        if (!remainderNamespaces.isEmpty()) {
            StringBuilder sb = new StringBuilder("Still got namespaces with ungenerated shape files!\n");
            remainderNamespaces.forEach(namespace -> sb.append(namespace).append(", "));

            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                throw new IllegalStateException(sb.toString());
            }
            else {
                PLAMod.LOGGER.warn(sb.toString());
            }
        }

        PLAMod.LOGGER.info("Took {} ms to generate {} shape files: {}", Util.getMillis() - startTime, writtenFiles.size(), writtenFiles);
        return newResult;
    }

    private static List<String> writeShapeFiles(Collection<ShapeFile> shapeFiles, Path shapesFolder) {
        try {
            Files.createDirectories(shapesFolder);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<String> writtenFiles = new ArrayList<>();
        for (ShapeFile shapeFile : shapeFiles) {
            ShapeFile.CODEC.encode(shapeFile, NbtOps.INSTANCE, NbtOps.INSTANCE.empty())
                    .ifSuccess(tag -> {
                        try {
                            // sanitizing the namespaceVersion does mean it may not match up during parsing
                            // In practice most namespaceVersions will be compliant, so this is probably fine shrug
                            String filename = shapeFile.namespace() + ShapeFileParser.FILENAME_SEPARATOR + sanitizeName(shapeFile.namespaceVersion()) + ".dat";
                            NbtIo.writeCompressed(tag.asCompound().orElseThrow(), shapesFolder.resolve(filename));
                            writtenFiles.add(filename);
                        }
                        catch (IOException e) {
                            PLAMod.LOGGER.warn("Failed to write shape file for {}!", shapeFile.namespace(), e);
                        }
                    })
                    .ifError(err -> PLAMod.LOGGER.warn("Failed to encode shape file {}!\n{}", shapeFile.namespace(), err.message()));
        }

        return writtenFiles;
    }

    // Copied from FileUtil
    private static String sanitizeName(String baseName) {
        for(char replacer : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
            baseName = baseName.replace(replacer, '_');
        }

        // removed the . from regex
        return baseName.replaceAll("[/\"]", "_");
    }
}

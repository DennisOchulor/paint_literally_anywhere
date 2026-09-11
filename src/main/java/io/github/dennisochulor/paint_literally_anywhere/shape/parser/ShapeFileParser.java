package io.github.dennisochulor.paint_literally_anywhere.shape.parser;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ShapeFileParser {
    private ShapeFileParser() {}

    public static final String RELATIVE_PATH_TO_SHAPES_DIR = "config/paint_literally_anywhere/shapes/";
    public static final char FILENAME_SEPARATOR = '@';
    public static final Pattern FILENAME_PATTERN = Pattern.compile("(.+?)" + FILENAME_SEPARATOR + "(.+)\\.dat");

    public static ShapeFileParseResult parse(Path rootDirectory) {
        Path shapesFolder = rootDirectory.resolve(RELATIVE_PATH_TO_SHAPES_DIR);
        if (Files.notExists(shapesFolder)) {
            return ShapeFileParseResult.EMPTY;
        }

        List<Path> shapeFiles;
        try (Stream<Path> stream = Files.list(shapesFolder)) {
            Map<String, Path> namespacesSeen = new HashMap<>();

            stream.forEach(path -> {
                String filename = path.getFileName().toString();
                Matcher matcher = FILENAME_PATTERN.matcher(filename);

                if (matcher.matches()) {
                    String namespace = matcher.group(1);
                    String version = matcher.group(2);

                    FabricLoader.getInstance().getModContainer(namespace).ifPresentOrElse(modContainer -> {
                        // If there is never any exact match for a namespace, then it will just use the first one.
                        boolean isExactMatch = version.equals(modContainer.getMetadata().getVersion().getFriendlyString());

                        if (!namespacesSeen.containsKey(namespace) || isExactMatch) {
                            namespacesSeen.put(namespace, path);
                        }
                    },

                    () -> {
                        PLAMod.LOGGER.warn("Cannot find matching ModContainer for namespace {}", namespace);
                    });
                }
                else {
                    PLAMod.LOGGER.warn("Ignoring unknown shape file: {}", filename);
                }
            });

            shapeFiles = namespacesSeen.values().stream().toList();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (shapeFiles.isEmpty()) {
            return ShapeFileParseResult.EMPTY;
        }

        StringBuilder sb = new StringBuilder();
        shapeFiles.forEach(path -> sb.append(path.getFileName().toString()).append(", "));
        PLAMod.LOGGER.debug("About to parse {}", sb);

        long startTime = Util.getMillis();
        Map<String, ShapeFile> map = new HashMap<>();
        for (Path path : shapeFiles) {
            try {
                ShapeFile.CODEC.decode(NbtOps.INSTANCE, NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap()))
                        .ifSuccess(pair -> {
                            ShapeFile record = pair.getFirst();
                            map.put(record.namespace(), record);
                        })
                        .ifError(err -> {
                            PLAMod.LOGGER.warn("Encountered errors while parsing shape file {}\n{}", path.getFileName(), err.message());
                            err.resultOrPartial().ifPresent(pair -> {
                                ShapeFile record = pair.getFirst();
                                map.put(record.namespace(), record);
                            });
                        });
            }
            catch (IOException e) {
                PLAMod.LOGGER.warn("Failed to read shape file {}", path.getFileName(), e);
            }
        }

        PLAMod.LOGGER.info("Took {} ms to parse {} shape files for the namespaces: {}",
                Util.getMillis() - startTime, shapeFiles.size(), map.keySet().stream().reduce((s1, s2) -> s1 + ", " + s2).orElse("<none>"));
        return new ShapeFileParseResult(map);
    }
}

package io.github.dennisochulor.paint_literally_anywhere.shape.parser;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class ShapeFileParser {
    private ShapeFileParser() {}

    public static final String RELATIVE_PATH_TO_SHAPES_DIR = "config/paint_literally_anywhere/shapes/";

    public static ShapeFileParseResult parse(Path rootDirectory) {
        Path shapesFolder = rootDirectory.resolve(RELATIVE_PATH_TO_SHAPES_DIR);
        List<Path> shapeFiles;
        try (Stream<Path> stream = Files.list(shapesFolder)) {
            shapeFiles = stream.toList();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (shapeFiles.isEmpty()) {
            return ShapeFileParseResult.EMPTY;
        }

        long startTime = Util.getMillis();
        Map<String, ShapeFile> map = new HashMap<>();
        for (Path path : shapeFiles) {
            try {
                ShapeFile.CODEC.decode(NbtOps.INSTANCE, NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap()))
                        .ifSuccess(pair -> {
                            ShapeFile record = pair.getFirst();
                            map.put(record.namespace(), record);
                        })
                        .ifError(err -> PLAMod.LOGGER.warn("Failed to parse shape file {}\n{}", path.getFileName(), err.message()));
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

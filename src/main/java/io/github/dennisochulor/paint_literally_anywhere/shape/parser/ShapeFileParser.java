package io.github.dennisochulor.paint_literally_anywhere.shape.parser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ShapeFileParser {
    private ShapeFileParser() {}

    public static final String RELATIVE_PATH_TO_SHAPES_DIR = "config/paint_literally_anywhere/shapes/";

    public static ShapeFileParseResult parse(Path rootDirectory) {
        File shapesFolder = rootDirectory.resolve(RELATIVE_PATH_TO_SHAPES_DIR).toFile();
        File[] shapeFiles = shapesFolder.listFiles();

        if (shapeFiles == null || shapeFiles.length == 0) {
            return ShapeFileParseResult.EMPTY;
        }

        long startTime = Util.getMillis();
        Map<String, ShapeFile> map = new HashMap<>();
        Gson gson = new Gson();
        for (File file : shapeFiles) {
            try {
                JsonElement json = gson.fromJson(new FileReader(file), JsonElement.class);
                ShapeFile.CODEC.decode(JsonOps.COMPRESSED, json)
                        .ifSuccess(pair -> {
                            ShapeFile record = pair.getFirst();
                            map.put(record.namespace(), record);
                        })
                        .ifError(err -> PLAMod.LOGGER.warn("Failed to parse shape file {}\n{}", file.getName(), err.message()));
            }
            catch (IOException e) {
                PLAMod.LOGGER.warn("Failed to read shape file {}", file.getName(), e);
            }
        }

        PLAMod.LOGGER.info("Took {} ms to parse {} shape files for the namespaces: {}",
                Util.getMillis() - startTime, shapeFiles.length, map.keySet().stream().reduce((s1, s2) -> s1 + ", " + s2).orElse("<none>"));
        return new ShapeFileParseResult(map);
    }
}

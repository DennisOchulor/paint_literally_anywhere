package io.github.dennisochulor.paint_literally_anywhere.shape.parser;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.OddCodecs;
import io.github.dennisochulor.paint_literally_anywhere.ParallelListMapCodec;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ShapeFile(
        int schemaVersion,
        int mcDataVersion,
        String namespace,
        String namespaceVersion,
        List<QuadTemplate> templates,
        Map<BlockState, int[]> blockStates
) {
    public static final int LATEST_SCHEMA_VERSION = 1;
    public static final int DEFAULT_MC_DATA_VERSION = 4903; // 26.2 data version by default
    public static final Codec<ShapeFile> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("schemaVersion").forGetter(ShapeFile::schemaVersion),
                    Codec.INT.optionalFieldOf("mcDataVersion", DEFAULT_MC_DATA_VERSION).forGetter(ShapeFile::mcDataVersion),
                    Codec.STRING.fieldOf("namespace").forGetter(ShapeFile::namespace),
                    Codec.STRING.fieldOf("namespaceVersion").forGetter(ShapeFile::namespaceVersion),
                    QuadTemplate.CODEC.listOf().fieldOf("templates").forGetter(ShapeFile::templates),
                    new ParallelListMapCodec<>(BlockState.CODEC, OddCodecs.INT_ARRAY_CODEC, true).fieldOf("blockStates").forGetter(ShapeFile::blockStates)
            ).apply(instance, ShapeFile::new)
    );


    public static class Builder {
        private final String namespace;
        private final String namespaceVersion;
        private final List<QuadTemplate> templates = new ArrayList<>();
        private final Object2IntMap<QuadTemplate> templateToIndex = new Object2IntOpenHashMap<>();
        private final Map<BlockState, int[]> blockStates = new HashMap<>();

        public Builder(String namespace, String namespaceVersion) {
            this.namespace = namespace;
            this.namespaceVersion = namespaceVersion;
        }

        public void add(BlockState state, List<QuadTemplate> templates) {
            int[] indexes = new int[templates.size()];

            for (int i = 0; i < templates.size(); i++) {
                QuadTemplate template = templates.get(i);
                int index = templateToIndex.getOrDefault(template, -1);

                if (index == -1) {
                    this.templates.add(template);
                    index = this.templates.size() - 1;
                    templateToIndex.put(template, index);
                }

                indexes[i] = index;
            }

            blockStates.put(state, indexes);
        }

        public ShapeFile build() {
            return new ShapeFile(
                    LATEST_SCHEMA_VERSION,
                    SharedConstants.getCurrentVersion().dataVersion().version(),
                    namespace,
                    namespaceVersion,
                    List.copyOf(templates),
                    Map.copyOf(blockStates)
            );
        }
    }

}

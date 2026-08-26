package io.github.dennisochulor.paint_literally_anywhere.client.model;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.ModAttachmentTypes;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

public class CanvasBlockStateModel extends WrapperBlockStateModel {
    private static final float OFFSET = 0.0001F;
    private static final EnumMap<Direction, Vector3fc> OFFSETS = Util.make(() -> {
        var map = new EnumMap<Direction, Vector3fc>(Direction.class);

        map.put(Direction.UP, new Vector3f(0, OFFSET, 0));
        map.put(Direction.DOWN, new Vector3f(0, -OFFSET, 0));
        map.put(Direction.NORTH, new Vector3f(0, 0, -OFFSET));
        map.put(Direction.SOUTH, new Vector3f(0, 0 ,OFFSET));
        map.put(Direction.EAST, new Vector3f(OFFSET, 0, 0));
        map.put(Direction.WEST, new Vector3f(-OFFSET, 0, 0));

        return map;
    });
    private static @Nullable TextureAtlasSprite SPRITE;

    public CanvasBlockStateModel(BlockStateModel wrappedModel) {
        this.wrapped = wrappedModel;
    }

    private static TextureAtlasSprite sprite() {
        if (SPRITE == null) {
            SPRITE = Minecraft.getInstance().getAtlasManager()
                    .get(new SpriteId(QuadAtlas.BLOCK.getTextureLocation(), PLAMod.id("block/canvas")));
        }

        return SPRITE;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        super.emitQuads(emitter, level, pos, state, random, cullTest);

        ClientLevel clientLevel = Minecraft.getInstance().level;

        if (clientLevel == null) {
            return;
        }

        // yikes
        LevelChunk chunk = clientLevel.getChunkAt(pos);
        ChunkCanvasData data = chunk.getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);

        if (data == null) {
            return;
        }

        List<QuadInstance> instances = data.blocks().get(pos);

        if (instances == null || instances.isEmpty()) {
            return;
        }

        instances.forEach(instance -> render(emitter, level, pos, instance));
    }

    private static void render(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, QuadInstance instance) {
        int[] pixels = instance.pixels();
        BitSet emissiveData = instance.emissiveData();
        QuadTemplate template = instance.template();
        Direction direction = template.direction();
        Vector3fc offset = OFFSETS.get(direction);
        int rows = instance.rows();
        int cols = instance.cols();
        int resolution = instance.resolution();
        float step = 1.0F / resolution;

        int lightCoords = LightCoordsUtil.getLightCoords(level, pos.relative(direction));

        Vector3f refVertex = new Vector3f();
        Vector3f refScalerVec = new Vector3f();
        template.rowVector().normalize(step, refVertex);
        template.colVector().normalize(step, refScalerVec);
        Vector3fc rowUnitVec = new Vector3f(refVertex);
        Vector3fc colUnitVec = new Vector3f(refScalerVec);

        // To handle cases where last pixel in row/col is not a full res pixel
        float clipRowScale = step - (step * rows - template.rowVector().length());
        Vector3fc clippedRowUnitVec = new Vector3f(rowUnitVec.normalize(clipRowScale, refScalerVec));

        float clipColScale = step - (step * cols - template.colVector().length());
        Vector3fc clippedColUnitVec = new Vector3f(colUnitVec.normalize(clipColScale, refScalerVec));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int i = instance.index(row, col);
                int argb = pixels[i];
                boolean emissive = emissiveData.get(i);

                if (argb == 0) {
                    continue;
                }

                emitter.color(argb, argb, argb, argb)
                        .emissive(emissive)
                        .lightmap(lightCoords, lightCoords, lightCoords, lightCoords)
                        //.cullFace(direction) // causes too many false positives :(
                        .chunkLayer(ChunkSectionLayer.TRANSLUCENT)
                        .uv(0, sprite().getU0(), sprite().getV0())
                        .uv(1, sprite().getU0(), sprite().getV1())
                        .uv(2, sprite().getU1(), sprite().getV1())
                        .uv(3, sprite().getU1(), sprite().getV0());

                // set to template v0 first and offset to prevent z-fighting
                refVertex.set(template.v0()).add(offset);

                // v0
                refVertex.add(colUnitVec.mul(col, refScalerVec));
                refVertex.add(rowUnitVec.mul(row, refScalerVec));
                emitter.pos(0, refVertex);

                // v1 - step col
                refVertex.add(col == cols - 1 ? clippedColUnitVec : colUnitVec);
                emitter.pos(1, refVertex);

                // v2 - step row/col
                refVertex.add(row == rows - 1 ? clippedRowUnitVec : rowUnitVec);
                emitter.pos(2, refVertex);

                // v3 - step row
                refVertex.sub(col == cols - 1 ? clippedColUnitVec : colUnitVec);
                emitter.pos(3, refVertex);

                emitter.emit();
            }
        }
    }



    public record PairKey(@Nullable Object o1, @Nullable Object o2) {}

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return new PairKey(((Level) level).getChunkAt(pos).getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA), super.createGeometryKey(level, pos, state, random));
    }
}

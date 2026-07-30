package io.github.dennisochulor.paint_literally_anywhere.client.model;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.ModAttachmentTypes;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class CanvasBlockStateModel extends WrapperBlockStateModel {
    private static @Nullable TextureAtlasSprite SPRITE;

    public CanvasBlockStateModel(BlockStateModel wrappedModel) {
        this.wrapped = wrappedModel;
    }

    private static TextureAtlasSprite sprite() {
        if (SPRITE == null) {
            SPRITE = Minecraft.getInstance().getAtlasManager()
                    .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, PLAMod.id("block/canvas")));
        }

        return SPRITE;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        super.emitQuads(emitter, level, pos, state, random, cullTest);

        // yikes
        ChunkCanvasData data = Objects.requireNonNull(Minecraft.getInstance().level).getChunkAt(pos).getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);

        if (data == null) return;

        List<QuadInstance> instances = data.blocks().get(pos);

        if (instances == null || instances.isEmpty()) return;

        instances.forEach(instance -> render(emitter, level, pos, instance));
    }

    private static void render(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, QuadInstance instance) {
        int[] pixels = instance.pixels();
        BitSet emissiveData = instance.emissiveData();
        QuadTemplate template = instance.template();
        Direction direction = template.direction();
        int rows = instance.rows();
        int cols = instance.cols();
        int resolution = instance.resolution();
        float step = 1.0F / resolution;

        int lightCoords = LightCoordsUtil.getLightCoords(level, pos.relative(direction));

        Vector3f rowUnitVecMutable = new Vector3f();
        Vector3f colUnitVecMutable = new Vector3f();
        template.rowVector().normalize(step, rowUnitVecMutable);
        template.colVector().normalize(step, colUnitVecMutable);
        //noinspection UnnecessaryLocalVariable - ensure we don't accidentally mutate it
        Vector3fc rowUnitVec = rowUnitVecMutable;
        //noinspection UnnecessaryLocalVariable - ensure we don't accidentally mutate it
        Vector3fc colUnitVec = colUnitVecMutable;

        Vector3f refVertex = new Vector3f();
        Vector3f refScalerVec = new Vector3f();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int i = QuadInstance.index(row, col, cols);
                int argb = pixels[i];
                boolean emissive = emissiveData.get(i);

                emitter.color(argb, argb, argb, argb)
                        .emissive(emissive)
                        .lightmap(lightCoords, lightCoords, lightCoords, lightCoords)
                        .cullFace(direction)
                        .uv(0, sprite().getU0(), sprite().getV0())
                        .uv(1, sprite().getU0(), sprite().getV1())
                        .uv(2, sprite().getU1(), sprite().getV1())
                        .uv(3, sprite().getU1(), sprite().getV0());

                // v0
                template.colVector().add(colUnitVec.mul(col, refScalerVec), refVertex);
                refVertex.add(rowUnitVec.mul(row, refScalerVec));
                emitter.pos(0, refVertex);

                // v1 - step col
                refVertex.add(colUnitVec);
                emitter.pos(direction == Direction.UP ? 3 : 1, refVertex);

                // v2 - step row/col
                refVertex.add(rowUnitVec);
                emitter.pos(2, refVertex);

                // v3 - step row
                refVertex.sub(colUnitVec);
                emitter.pos(direction == Direction.UP ? 1 : 3, refVertex);

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

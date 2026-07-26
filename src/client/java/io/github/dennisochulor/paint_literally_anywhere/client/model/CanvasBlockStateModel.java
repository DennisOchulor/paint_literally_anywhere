package io.github.dennisochulor.paint_literally_anywhere.client.model;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.jspecify.annotations.Nullable;

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

//    @Override
//    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
//        super.emitQuads(emitter, level, pos, state, random, cullTest);
//
//        if (level.getBlockEntityRenderData(pos) instanceof CanvasRenderState canvasRenderState) {
//            side(canvasRenderState, emitter, DOWN, EAST, NORTH, 0, 0, 1);
//
//            side(canvasRenderState, emitter, UP, EAST, NORTH, 0, 1, 1);
//
//            side(canvasRenderState, emitter, SOUTH, EAST, DOWN, 0, 1, 1);
//
//            side(canvasRenderState, emitter, EAST, NORTH, DOWN, 1, 1, 1);
//
//            side(canvasRenderState, emitter, NORTH, WEST, DOWN, 1, 1, 0);
//
//            side(canvasRenderState, emitter, WEST, SOUTH, DOWN, 0, 1, 0);
//        }
//    }
//
//    // row/colDir as in the direction of the elements in that row/col
//    private static void side(CanvasRenderState state, QuadEmitter emitter, Direction side, Direction rowDir, Direction colDir,
//                             int xOffset, int yOffset, int zOffset) {
//        int[] pixels = state.sides()[side.ordinal()];
//        int lightCoords = state.perFaceLight()[side.ordinal()];
//
//        if (pixels == null) return;
//
//        // to prevent z-fighting with the block surface
//        Axis biasAxis = side.getAxis();
//        int biasAxisDirectionStep = side.getAxisDirection().getStep();
//        float xOffsetBiased = biasAxis == Axis.X ? biasAxisDirectionStep * 0.0001F + xOffset : xOffset;
//        float yOffsetBiased = biasAxis == Axis.Y ? biasAxisDirectionStep * 0.0001F + yOffset : yOffset;
//        float zOffsetBiased = biasAxis == Axis.Z ? biasAxisDirectionStep * 0.0001F + zOffset : zOffset;
//
//        float xColStep = rowDir.getAxis() == Axis.X ? getStep(rowDir) : 0;
//        float yColStep = rowDir.getAxis() == Axis.Y ? getStep(rowDir) : 0;
//        float zColStep = rowDir.getAxis() == Axis.Z ? getStep(rowDir) : 0;
//
//        float xRowStep = colDir.getAxis() == Axis.X ? getStep(colDir) : 0;
//        float yRowStep = colDir.getAxis() == Axis.Y ? getStep(colDir) : 0;
//        float zRowStep = colDir.getAxis() == Axis.Z ? getStep(colDir) : 0;
//
//        for (int row = 0; row < CanvasBlock.SIZE; row++) {
//            for (int col = 0; col < CanvasBlock.SIZE; col++) {
//                int index = row * CanvasBlock.SIZE + col;
//                int color = pixels[index];
//                int opaqueColor = ARGB.opaque(color); // alpha is really used as emissive indicator, always render as opaque
//                boolean emissive = CanvasBlock.isEmissive(color);
//
//                if (color == CanvasBlock.DEFAULT_COLOR) continue;
//
//                float xBase = xRowStep != 0 ? row * xRowStep + xOffsetBiased : col * xColStep + xOffsetBiased;
//                float yBase = yRowStep != 0 ? row * yRowStep + yOffsetBiased : col * yColStep + yOffsetBiased;
//                float zBase = zRowStep != 0 ? row * zRowStep + zOffsetBiased : col * zColStep + zOffsetBiased;
//
//                emitter.color(opaqueColor, opaqueColor, opaqueColor, opaqueColor)
//                        .emissive(emissive)
//                        .lightmap(lightCoords, lightCoords, lightCoords, lightCoords)
//                        .cullFace(side)
//                        .uv(0, sprite().getU0(), sprite().getV0())
//                        .uv(1, sprite().getU0(), sprite().getV1())
//                        .uv(2, sprite().getU1(), sprite().getV1())
//                        .uv(3, sprite().getU1(), sprite().getV0());
//
//                // base
//                emitter.pos(0, xBase, yBase, zBase);
//
//                // step row
//                emitter.pos(side == UP ? 3 : 1, xBase + xRowStep, yBase + yRowStep, zBase + zRowStep);
//
//                // step row, step col
//                emitter.pos(2, xBase + xRowStep + xColStep, yBase + yRowStep + yColStep, zBase + zRowStep + zColStep);
//
//                // step col
//                emitter.pos(side == UP ? 1 : 3, xBase + xColStep, yBase + yColStep, zBase + zColStep);
//
//                emitter.emit();
//            }
//        }
//    }
//
//    private static float getStep(Direction dir) {
//        return dir.getAxisDirection() == AxisDirection.POSITIVE ? STEP : -STEP;
//    }
//
//
//
//    public record PairKey(@Nullable Object o1, @Nullable Object o2) {}
//
//    @Override
//    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
//        return new PairKey(level.getBlockEntityRenderData(pos), super.createGeometryKey(level, pos, state, random));
//    }
}

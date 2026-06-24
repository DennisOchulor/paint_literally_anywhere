package io.github.dennisochulor.playground.client.canvas;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.dennisochulor.playground.canvas.CanvasBlock;
import io.github.dennisochulor.playground.canvas.CanvasBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static net.minecraft.core.Direction.*;

public class CanvasRenderer implements BlockEntityRenderer<CanvasBlockEntity, CanvasRenderState> {
    private static final float STEP = 1.0F / CanvasBlock.SIZE;

    public CanvasRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CanvasRenderState createRenderState() {
        return new CanvasRenderState();
    }

    @Override
    public void extractRenderState(CanvasBlockEntity blockEntity, CanvasRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        for (Direction dir : Direction.values()) {
            int[] stateArr = state.sides[dir.ordinal()];

            for (int i = 0; i < stateArr.length; i++) {
                stateArr[i] = blockEntity.getPixel(dir, i);
            }
        }
    }

    @Override
    public void submit(CanvasRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        int[][] sides = state.sides;

        poseStack.translate(0, 0, 1);
        side(sides[DOWN.ordinal()], EAST, NORTH, poseStack, submitNodeCollector);

        poseStack.translate(0, 1, 0);
        side(sides[UP.ordinal()], EAST, NORTH, poseStack, submitNodeCollector);
        side(sides[SOUTH.ordinal()], EAST, DOWN, poseStack, submitNodeCollector);

        poseStack.translate(1, 0, 0);
        side(sides[EAST.ordinal()], NORTH, DOWN, poseStack, submitNodeCollector);

        poseStack.translate(0, 0, -1);
        side(sides[NORTH.ordinal()], WEST, DOWN, poseStack, submitNodeCollector);

        poseStack.translate(-1, 0, 0);
        side(sides[WEST.ordinal()], SOUTH, DOWN, poseStack, submitNodeCollector);
    }

    // row/colDir as in the direction of the elements in that row/col
    private static void side(int[] pixels, Direction rowDir, Direction colDir, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        float xColStep = rowDir.getAxis() == Axis.X ? getStep(rowDir) : 0;
        float yColStep = rowDir.getAxis() == Axis.Y ? getStep(rowDir) : 0;
        float zColStep = rowDir.getAxis() == Axis.Z ? getStep(rowDir) : 0;

        float xRowStep = colDir.getAxis() == Axis.X ? getStep(colDir) : 0;
        float yRowStep = colDir.getAxis() == Axis.Y ? getStep(colDir) : 0;
        float zRowStep = colDir.getAxis() == Axis.Z ? getStep(colDir) : 0;

        for (int row = 0; row < CanvasBlock.SIZE; row++) {
            for (int col = 0; col < CanvasBlock.SIZE; col++) {
                // convert RGB to ARGB with max alpha
                int color = ARGB.opaque(pixels[row * CanvasBlock.SIZE + col]);

                if (color == CanvasBlock.DEFAULT_COLOR) continue;

                float xBase = xRowStep != 0 ? row * xRowStep : col * xColStep;
                float yBase = yRowStep != 0 ? row * yRowStep : col * yColStep;
                float zBase = zRowStep != 0 ? row * zRowStep : col * zColStep;
                submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, buffer) -> {
                    // base
                    buffer.addVertex(pose, xBase, yBase, zBase).setColor(color).setUv(0, 0).setLineWidth(1);

                    // step row
                    buffer.addVertex(pose, xBase + xRowStep, yBase + yRowStep, zBase + zRowStep).setColor(color).setUv(0, 0).setLineWidth(1);

                    // step row, step col
                    buffer.addVertex(pose, xBase + xRowStep + xColStep, yBase + yRowStep + yColStep, zBase + zRowStep + zColStep).setColor(color).setUv(0, 0).setLineWidth(1);

                    // step col
                    buffer.addVertex(pose, xBase + xColStep, yBase + yColStep, zBase + zColStep).setColor(color).setUv(0, 0).setLineWidth(1);
                });
            }
        }
    }

    private static float getStep(Direction dir) {
        return dir.getAxisDirection() == AxisDirection.POSITIVE ? STEP : -STEP;
    }
}

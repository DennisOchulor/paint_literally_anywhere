package io.github.dennisochulor.canvas_block.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.dennisochulor.canvas_block.CanvasMod;
import io.github.dennisochulor.canvas_block.block.CanvasBlock;
import io.github.dennisochulor.canvas_block.block.CanvasBlockEntity;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static net.minecraft.core.Direction.*;

public class CanvasRenderer implements BlockEntityRenderer<CanvasBlockEntity, CanvasRenderState> {
    private static final float STEP = 1.0F / CanvasBlock.SIZE;

    // vertex shader copied from position_color.vsh and text_background.vsh
    private static final RenderPipeline NON_EMISSIVE_FILLED_BOX_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation(CanvasMod.id("pipeline/non_emissive_filled_box"))
                    .withVertexShader(CanvasMod.id("core/position_color_lightmap"))
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER2)
                    .withCull(true)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_LIGHTMAP)
                    .build()
    );

    private static final RenderType NON_EMISSIVE_FILLED_BOX_TYPE = RenderType.create("canvas_non_emissive_filled_box",
            RenderSetup.builder(NON_EMISSIVE_FILLED_BOX_PIPELINE).sortOnUpload().useLightmap().setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());


    private static final RenderPipeline EMISSIVE_FILLED_BOX_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation(CanvasMod.id("pipeline/emissive_filled_box"))
                    .withCull(true)
                    .build()
    );

    private static final RenderType EMISSIVE_FILLED_BOX_TYPE = RenderType.create("canvas_emissive_filled_box",
            RenderSetup.builder(EMISSIVE_FILLED_BOX_PIPELINE).sortOnUpload().setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());



    @SuppressWarnings("unused")
    public CanvasRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CanvasRenderState createRenderState() {
        return new CanvasRenderState();
    }

    @Override
    public void extractRenderState(CanvasBlockEntity blockEntity, CanvasRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

        if (blockEntity.getLevel() == null) return;

        BlockState blockState = blockEntity.getBlockState();
        for (Direction dir : Direction.values()) {
            BlockPos relativePos = blockEntity.getBlockPos().relative(dir);

            if (CanvasBlock.shouldRenderFace(blockState, blockEntity.getLevel().getBlockState(relativePos), dir)) {
                state.perFaceLight[dir.ordinal()] = LightCoordsUtil.getLightCoords(LightCoordsUtil.BrightnessGetter.DEFAULT,
                        blockEntity.getLevel(), blockState, relativePos);
                state.sides[dir.ordinal()] = blockEntity.copyPixelColors(dir);
            }
        }
    }

    @Override
    public void submit(CanvasRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.translate(0, 0, 1);
        side(state, DOWN, EAST, NORTH, poseStack, submitNodeCollector);

        poseStack.translate(0, 1, 0);
        side(state, UP, EAST, NORTH, poseStack, submitNodeCollector);
        side(state, SOUTH, EAST, DOWN, poseStack, submitNodeCollector);

        poseStack.translate(1, 0, 0);
        side(state, EAST, NORTH, DOWN, poseStack, submitNodeCollector);

        poseStack.translate(0, 0, -1);
        side(state, NORTH, WEST, DOWN, poseStack, submitNodeCollector);

        poseStack.translate(-1, 0, 0);
        side(state, WEST, SOUTH, DOWN, poseStack, submitNodeCollector);
    }

    // row/colDir as in the direction of the elements in that row/col
    private static void side(CanvasRenderState state, Direction side, Direction rowDir, Direction colDir, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        int[] pixels = state.sides[side.ordinal()];
        int lightCoords = state.perFaceLight[side.ordinal()];

        if (pixels == null) return;

        float xColStep = rowDir.getAxis() == Axis.X ? getStep(rowDir) : 0;
        float yColStep = rowDir.getAxis() == Axis.Y ? getStep(rowDir) : 0;
        float zColStep = rowDir.getAxis() == Axis.Z ? getStep(rowDir) : 0;

        float xRowStep = colDir.getAxis() == Axis.X ? getStep(colDir) : 0;
        float yRowStep = colDir.getAxis() == Axis.Y ? getStep(colDir) : 0;
        float zRowStep = colDir.getAxis() == Axis.Z ? getStep(colDir) : 0;

        for (int row = 0; row < CanvasBlock.SIZE; row++) {
            for (int col = 0; col < CanvasBlock.SIZE; col++) {
                int index = row * CanvasBlock.SIZE + col;
                int color = pixels[index];
                int opaqueColor = ARGB.opaque(color); // alpha is really used as emissive indicator, always render as opaque
                boolean emissive = CanvasBlock.isEmissive(color);

                if (color == CanvasBlock.DEFAULT_COLOR) continue;

                float xBase = xRowStep != 0 ? row * xRowStep : col * xColStep;
                float yBase = yRowStep != 0 ? row * yRowStep : col * yColStep;
                float zBase = zRowStep != 0 ? row * zRowStep : col * zColStep;
                submitNodeCollector.submitCustomGeometry(poseStack, emissive ? EMISSIVE_FILLED_BOX_TYPE : NON_EMISSIVE_FILLED_BOX_TYPE, (pose, buffer) -> {
                    if (side == UP) { // UP needs a different vertex winding order to not be culled for some reason idk man...
                        // base
                        buffer.addVertex(pose, xBase, yBase, zBase)
                                .setColor(opaqueColor).setLight(lightCoords).setUv(0, 1).setLineWidth(1);

                        // step col
                        buffer.addVertex(pose, xBase + xColStep, yBase + yColStep, zBase + zColStep)
                                .setColor(opaqueColor).setLight(lightCoords).setUv(0, 0).setLineWidth(1);

                        // step row, step col
                        buffer.addVertex(pose, xBase + xRowStep + xColStep, yBase + yRowStep + yColStep, zBase + zRowStep + zColStep)
                                .setColor(opaqueColor).setLight(lightCoords).setUv(1, 0).setLineWidth(1);

                        // step row
                        buffer.addVertex(pose, xBase + xRowStep, yBase + yRowStep, zBase + zRowStep)
                                .setColor(opaqueColor).setLight(lightCoords).setUv(1, 1).setLineWidth(1);
                    }
                    else {
                        // base
                        buffer.addVertex(pose, xBase, yBase, zBase)
                                .setColor(opaqueColor).setLight(lightCoords).setUv(0, 1).setLineWidth(1);

                        // step row
                        buffer.addVertex(pose, xBase + xRowStep, yBase + yRowStep, zBase + zRowStep)
                                .setColor(opaqueColor).setLight(lightCoords).setUv(1, 1).setLineWidth(1);

                        // step row, step col
                        buffer.addVertex(pose, xBase + xRowStep + xColStep, yBase + yRowStep + yColStep, zBase + zRowStep + zColStep)
                                .setColor(opaqueColor).setLight(lightCoords).setUv(1, 0).setLineWidth(1);

                        // step col
                        buffer.addVertex(pose, xBase + xColStep, yBase + yColStep, zBase + zColStep)
                                .setColor(opaqueColor).setLight(lightCoords).setUv(0, 0).setLineWidth(1);
                    }
                });
            }
        }
    }

    private static float getStep(Direction dir) {
        return dir.getAxisDirection() == AxisDirection.POSITIVE ? STEP : -STEP;
    }
}

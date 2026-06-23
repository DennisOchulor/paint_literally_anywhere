package io.github.dennisochulor.playground.client.canvas;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.dennisochulor.playground.Playground;
import io.github.dennisochulor.playground.canvas.CanvasBlockEntity;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CanvasRenderer implements BlockEntityRenderer<CanvasBlockEntity, CanvasRenderState> {
    private static final RenderPipeline FILLED_BOX_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Playground.id("pipeline/filled_box"))
            .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
            .build();

    private static final RenderType FILLED_BOX_TYPE = RenderType.create(
            "filled_box",
            RenderSetup.builder(FILLED_BOX_PIPELINE).sortOnUpload().setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup()
    );

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

    }
}

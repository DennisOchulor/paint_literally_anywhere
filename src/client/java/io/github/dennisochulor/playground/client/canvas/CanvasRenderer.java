package io.github.dennisochulor.playground.client.canvas;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.dennisochulor.playground.canvas.CanvasBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class CanvasRenderer implements BlockEntityRenderer<CanvasBlockEntity, CanvasRenderState> {
    public CanvasRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CanvasRenderState createRenderState() {
        return new CanvasRenderState();
    }

    @Override
    public void submit(CanvasRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    }
}

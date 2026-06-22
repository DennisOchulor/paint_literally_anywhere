package io.github.dennisochulor.playground.client.mirror;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.dennisochulor.playground.mirror.MirrorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import java.awt.*;

public class MirrorBlockEntityRenderer implements BlockEntityRenderer<MirrorBlockEntity, MirrorBlockEntityRenderState> {
    public MirrorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public MirrorBlockEntityRenderState createRenderState() {
        return new MirrorBlockEntityRenderState();
    }

    @Override
    public void submit(MirrorBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        side(submitNodeCollector, poseStack, Color.RED.getRGB()); // -Y
        poseStack.translate(0, 1, 0);
        side(submitNodeCollector, poseStack, Color.GREEN.getRGB()); // +Y
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        side(submitNodeCollector, poseStack, Color.BLUE.getRGB()); // -Z
        poseStack.translate(0, 1, 0);
        side(submitNodeCollector, poseStack, Color.YELLOW.getRGB()); // +Z
        poseStack.mulPose(Axis.ZN.rotationDegrees(90));
        side(submitNodeCollector, poseStack, Color.PINK.getRGB()); // +X
        poseStack.translate(0, 1, 0);
        side(submitNodeCollector, poseStack, Color.ORANGE.getRGB()); // -X
    }

    private static void side(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, int color) {
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.debugFilledBox(),
                ((pose, buffer) -> {
                    buffer.addVertex(pose, 0, 0, 0).setColor(color).setUv(0, 0).setLineWidth(1);
                    buffer.addVertex(pose, 1, 0, 0).setColor(color).setUv(0, 0).setLineWidth(1);
                    buffer.addVertex(pose, 1, 0, 1).setColor(color).setUv(0, 0).setLineWidth(1);
                    buffer.addVertex(pose, 0, 0, 1).setColor(color).setUv(0, 0).setLineWidth(1);
                })
        );
    }
}

package io.github.dennisochulor.paint_literally_anywhere.client.debug;

import io.github.dennisochulor.paint_literally_anywhere.ModAttachmentTypes;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Unit;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.Set;

public class QuadsDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    public static final DebugSubscription<Unit> DUMMY = new DebugSubscription<>(Unit.STREAM_CODEC);
    private static final int[] RAINBOW = new int[]{Color.RED.getRGB(), Color.YELLOW.getRGB(),
            Color.GREEN.getRGB(), Color.CYAN.getRGB(), Color.BLUE.getRGB(), Color.MAGENTA.getRGB()};
    private static final boolean ENABLED = true;

    private final Minecraft minecraft;

    public QuadsDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        if (!ENABLED ||
                minecraft.player == null ||
                minecraft.level == null ||
                // else pla$quads() may explode
                !minecraft.level.globalAttachments().hasAttached(ModAttachmentTypes.INACCURATE_NAMESPACES)
        ) {
            return;
        }

        if (minecraft.hitResult instanceof BlockHitResult blockHitResult) {
            BlockPos pos = blockHitResult.getBlockPos();
            Set<QuadTemplate> templates = ((BlockStateBaseExt) minecraft.level.getBlockState(pos)).pla$quads(minecraft.level, pos);

            int i = 0;
            for (QuadTemplate template : templates) {
                Gizmos.rect(
                        new Vec3(QuadTemplate.unlocalize(template.v0(), pos)),
                        new Vec3(QuadTemplate.unlocalize(template.v1(), pos)),
                        new Vec3(QuadTemplate.unlocalize(template.v2(), pos)),
                        new Vec3(QuadTemplate.unlocalize(template.v3(), pos)),
                        GizmoStyle.stroke(Color.CYAN.getRGB()) // change to RAINBOW[i] to check for z-fighting quads
                );

                i = (i + 1) % RAINBOW.length;
            }

            minecraft.gameRenderer.getGameRenderState().levelRenderState.blockOutlineRenderState = null;
        }
    }
}

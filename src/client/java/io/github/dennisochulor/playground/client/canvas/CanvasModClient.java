package io.github.dennisochulor.playground.client.canvas;

import io.github.dennisochulor.playground.canvas.CanvasMod;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class CanvasModClient {
    public static void init() {
        BlockEntityRenderers.register(CanvasMod.CANVAS_BLOCK_ENTITY, CanvasRenderer::new);
    }
}

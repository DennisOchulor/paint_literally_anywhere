package io.github.dennisochulor.playground.client.canvas;

import io.github.dennisochulor.playground.canvas.CanvasBlock;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class CanvasRenderState extends BlockEntityRenderState {
    public final int[][] sides = new int[6][CanvasBlock.SIZE * CanvasBlock.SIZE];
}

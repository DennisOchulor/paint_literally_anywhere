package io.github.dennisochulor.canvas_block.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class CanvasRenderState extends BlockEntityRenderState {
    public final int[] @Nullable [] sides = new int[6][];
    public final int[] perFaceLight = new int[6]; // since the base lightCoords will always be 0, as inside the BE is opaque
}

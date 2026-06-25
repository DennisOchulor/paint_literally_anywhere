package io.github.dennisochulor.playground.client.canvas;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class CanvasRenderState extends BlockEntityRenderState {
    public final int[] @Nullable [] sides = new int[6][];
}

package io.github.dennisochulor.playground.client.mirror;

import io.github.dennisochulor.playground.mirror.MirrorMod;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class MirrorModClient {
    public static void init() {
        BlockEntityRenderers.register(MirrorMod.MIRROR_BLOCK_ENTITY, MirrorBlockEntityRenderer::new);
    }
}

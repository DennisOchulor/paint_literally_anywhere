package io.github.dennisochulor.playground.client;

import io.github.dennisochulor.playground.PlaygroundBlockEntities;
import io.github.dennisochulor.playground.client.mirror.MirrorBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class PlaygroundClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(PlaygroundBlockEntities.MIRROR_BLOCK_ENTITY, MirrorBlockEntityRenderer::new);
	}
}
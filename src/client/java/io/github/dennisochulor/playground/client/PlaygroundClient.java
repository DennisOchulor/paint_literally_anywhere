package io.github.dennisochulor.playground.client;

import io.github.dennisochulor.playground.client.canvas.CanvasModClient;
import io.github.dennisochulor.playground.client.mirror.MirrorModClient;
import net.fabricmc.api.ClientModInitializer;

public class PlaygroundClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MirrorModClient.init();
		CanvasModClient.init();
	}
}
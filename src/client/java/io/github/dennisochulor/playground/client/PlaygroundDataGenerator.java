package io.github.dennisochulor.playground.client;

import io.github.dennisochulor.playground.client.canvas.datagen.CanvasDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PlaygroundDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		CanvasDataGenerator.addProviders(pack);
	}
}

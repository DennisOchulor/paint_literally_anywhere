package io.github.dennisochulor.playground.client.canvas.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CanvasDataGenerator {
    public static void addProviders(FabricDataGenerator.Pack pack) {
        pack.addProvider(CanvasModelProvider::new);
        pack.addProvider(CanvasBlockLootTableProvider::new);
        pack.addProvider(CanvasRecipeProvider::new);
        pack.addProvider(CanvasBlockTagsProvider::new);
    }
}

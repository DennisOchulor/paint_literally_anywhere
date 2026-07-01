package io.github.dennisochulor.canvas_block.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ModDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(CanvasModelProvider::new);
        pack.addProvider(CanvasBlockLootTableProvider::new);
        pack.addProvider(CanvasRecipeProvider::new);
        pack.addProvider(CanvasBlockTagsProvider::new);
    }
}

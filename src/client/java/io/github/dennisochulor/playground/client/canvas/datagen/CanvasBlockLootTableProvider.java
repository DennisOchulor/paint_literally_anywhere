package io.github.dennisochulor.playground.client.canvas.datagen;

import io.github.dennisochulor.playground.canvas.CanvasMod;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class CanvasBlockLootTableProvider extends FabricBlockLootSubProvider {
    protected CanvasBlockLootTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        dropSelf(CanvasMod.CANVAS);
    }
}

package io.github.dennisochulor.canvas_block.client.datagen;

import io.github.dennisochulor.canvas_block.block.ModBlockItemIds;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

public class CanvasBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {
    public CanvasBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlockItemIds.CANVAS.block());

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlockItemIds.CANVAS.block());
    }
}

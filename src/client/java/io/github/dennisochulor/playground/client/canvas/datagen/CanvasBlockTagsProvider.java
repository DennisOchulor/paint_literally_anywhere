package io.github.dennisochulor.playground.client.canvas.datagen;

import io.github.dennisochulor.playground.Utils;
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
    protected void addTags(HolderLookup.Provider registries) { // todo fix
        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(Utils.keyOfBlock("canvas"));

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Utils.keyOfBlock("canvas"));
    }
}

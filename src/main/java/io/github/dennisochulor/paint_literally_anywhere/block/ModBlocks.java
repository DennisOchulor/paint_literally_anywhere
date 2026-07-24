package io.github.dennisochulor.paint_literally_anywhere.block;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks {
    private ModBlocks() {}



    public static void init() {

    }

    private static Block register(BlockItemId id, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties, boolean shouldRegisterItem) {
        // Create the block instance
        Block block = blockFactory.apply(properties.setId(id.block()));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(id.item()).useBlockDescriptionPrefix());
            Registry.register(BuiltInRegistries.ITEM, id.item(), blockItem);
        }

        return Registry.register(BuiltInRegistries.BLOCK, id.block(), block);
    }
}

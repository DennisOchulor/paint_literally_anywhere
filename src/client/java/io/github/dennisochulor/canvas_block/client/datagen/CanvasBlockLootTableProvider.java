package io.github.dennisochulor.canvas_block.client.datagen;

import io.github.dennisochulor.canvas_block.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.concurrent.CompletableFuture;

public class CanvasBlockLootTableProvider extends FabricBlockLootSubProvider {
    protected CanvasBlockLootTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        Block canvas = ModBlocks.CANVAS;

        this.add(canvas, LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .when(this.hasSilkTouch())
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(
                                        LootItem.lootTableItem(canvas)
                                                .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                                                        .include(DataComponents.CUSTOM_DATA).include(DataComponents.ITEM_NAME))
                                )
                )
                .withPool(
                        LootPool.lootPool()
                                .when(this.doesNotHaveSilkTouch())
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(canvas))
                )
        );
    }
}

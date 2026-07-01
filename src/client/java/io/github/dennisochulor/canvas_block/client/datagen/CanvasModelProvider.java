package io.github.dennisochulor.canvas_block.client.datagen;

import io.github.dennisochulor.canvas_block.block.ModBlocks;
import io.github.dennisochulor.canvas_block.client.RGBColorTintSource;
import io.github.dennisochulor.canvas_block.item.ModComponents;
import io.github.dennisochulor.canvas_block.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.client.renderer.item.ItemModel;

import java.util.Optional;

public class CanvasModelProvider extends FabricModelProvider {
    public CanvasModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        blockModelGenerators.createTrivialCube(ModBlocks.CANVAS);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ModItems.PALETTE, ModelTemplates.FLAT_ITEM);

        paintBrush(itemModelGenerators);
    }

    private void paintBrush(ItemModelGenerators itemModelGenerators) {
        // Have to separate into two templates because of differing layer requirements
        // god i hate datagen
        ModelTemplate paintBrushTemplateSingleLayer = new ModelTemplate(
                Optional.of(ModelLocationUtils.getModelLocation(ModItems.PAINT_BRUSH, "_template")),
                Optional.empty(),
                TextureSlot.LAYER0
        );

        ModelTemplate paintBrushTemplateDualLayer = new ModelTemplate(
                Optional.of(ModelLocationUtils.getModelLocation(ModItems.PAINT_BRUSH, "_template")),
                Optional.empty(),
                TextureSlot.LAYER0, TextureSlot.LAYER1
        );

        ItemModel.Unbaked paintBrushStalkEmissive = ItemModelUtils.tintedModel(
                paintBrushTemplateDualLayer.create(
                        ModelLocationUtils.getModelLocation(ModItems.PAINT_BRUSH, "_stalk_emissive"),
                        TextureMapping.layered(
                                TextureMapping.getItemTexture(ModItems.PAINT_BRUSH, "_emissive"),
                                TextureMapping.getItemTexture(ModItems.PAINT_BRUSH, "_stalk")
                        ),
                        itemModelGenerators.modelOutput
                ),
                new RGBColorTintSource(-2172773)
        );

        ItemModel.Unbaked paintBrushStalk = ItemModelUtils.plainModel(
                itemModelGenerators.createFlatItemModel(
                        ModItems.PAINT_BRUSH,
                        "_stalk",
                        paintBrushTemplateSingleLayer
                )
        );

        ItemModel.Unbaked paintBrushPaint = ItemModelUtils.tintedModel(
                itemModelGenerators.createFlatItemModel(
                        ModItems.PAINT_BRUSH,
                        "_paint",
                        paintBrushTemplateSingleLayer
                ),
                new RGBColorTintSource(0)
        );

        itemModelGenerators.itemModelOutput.accept(
                ModItems.PAINT_BRUSH,
                ItemModelUtils.composite(
                        ItemModelUtils.conditional(
                                ItemModelUtils.hasComponent(ModComponents.EMISSIVE),
                                paintBrushStalkEmissive,
                                paintBrushStalk
                        ),
                        ItemModelUtils.conditional(
                                ItemModelUtils.hasComponent(ModComponents.RGB_COLOR),
                                paintBrushPaint,
                                new EmptyModel.Unbaked()
                        )
                )
        );
    }
}

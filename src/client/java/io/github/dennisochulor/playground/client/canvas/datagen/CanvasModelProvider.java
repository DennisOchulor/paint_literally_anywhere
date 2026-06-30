package io.github.dennisochulor.playground.client.canvas.datagen;

import io.github.dennisochulor.playground.canvas.CanvasMod;
import io.github.dennisochulor.playground.client.canvas.RGBColorTintSource;
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
        blockModelGenerators.createTrivialCube(CanvasMod.CANVAS);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(CanvasMod.PALETTE, ModelTemplates.FLAT_ITEM);

        paintBrush(itemModelGenerators);
    }

    private void paintBrush(ItemModelGenerators itemModelGenerators) {
        // Have to separate into two templates because of differing layer requirements
        // god i hate datagen
        ModelTemplate paintBrushTemplateSingleLayer = new ModelTemplate(
                Optional.of(ModelLocationUtils.getModelLocation(CanvasMod.PAINT_BRUSH, "_template")),
                Optional.empty(),
                TextureSlot.LAYER0
        );

        ModelTemplate paintBrushTemplateDualLayer = new ModelTemplate(
                Optional.of(ModelLocationUtils.getModelLocation(CanvasMod.PAINT_BRUSH, "_template")),
                Optional.empty(),
                TextureSlot.LAYER0, TextureSlot.LAYER1
        );

        ItemModel.Unbaked paintBrushStalkEmissive = ItemModelUtils.tintedModel(
                paintBrushTemplateDualLayer.create(
                        ModelLocationUtils.getModelLocation(CanvasMod.PAINT_BRUSH, "_stalk_emissive"),
                        TextureMapping.layered(
                                TextureMapping.getItemTexture(CanvasMod.PAINT_BRUSH, "_emissive"),
                                TextureMapping.getItemTexture(CanvasMod.PAINT_BRUSH, "_stalk")
                        ),
                        itemModelGenerators.modelOutput
                ),
                new RGBColorTintSource(-2172773)
        );

        ItemModel.Unbaked paintBrushStalk = ItemModelUtils.plainModel(
                itemModelGenerators.createFlatItemModel(
                        CanvasMod.PAINT_BRUSH,
                        "_stalk",
                        paintBrushTemplateSingleLayer
                )
        );

        ItemModel.Unbaked paintBrushPaint = ItemModelUtils.tintedModel(
                itemModelGenerators.createFlatItemModel(
                        CanvasMod.PAINT_BRUSH,
                        "_paint",
                        paintBrushTemplateSingleLayer
                ),
                new RGBColorTintSource(0)
        );

        itemModelGenerators.itemModelOutput.accept(
                CanvasMod.PAINT_BRUSH,
                ItemModelUtils.composite(
                        ItemModelUtils.conditional(
                                ItemModelUtils.hasComponent(CanvasMod.EMISSIVE),
                                paintBrushStalkEmissive,
                                paintBrushStalk
                        ),
                        ItemModelUtils.conditional(
                                ItemModelUtils.hasComponent(CanvasMod.RGB_COLOR),
                                paintBrushPaint,
                                new EmptyModel.Unbaked()
                        )
                )
        );
    }
}

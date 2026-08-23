package io.github.dennisochulor.paint_literally_anywhere.client.datagen;

import com.mojang.serialization.MapCodec;
import io.github.dennisochulor.paint_literally_anywhere.client.RGBColorTintSource;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        paintBrush(itemModelGenerators);
    }

    public static class PaintOpacity implements RangeSelectItemModelProperty {
        public static final MapCodec<PaintOpacity> MAP_CODEC = MapCodec.unit(new PaintOpacity());

        @Override
        public float get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
            if (properties.hasActualColor()) {
                return ARGB.alphaFloat(properties.argb()) * 100.0F; // 0-100%
            }
            else {
                return 100; // default max opacity
            }
        }

        @Override
        public MapCodec<? extends RangeSelectItemModelProperty> type() {
            return MAP_CODEC;
        }
    }

    private void paintBrush(ItemModelGenerators itemModelGenerators) {
        // Have to separate into two templates because of differing layer requirements
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


        List<RangeSelectItemModel.Entry> emissives = new ArrayList<>();
        for (int opacity = 10; opacity <= 100; opacity += 10) {
            emissives.add(
                    new RangeSelectItemModel.Entry(opacity - 10, // -10 cause "Will select last entry with threshold less or equal to property value"
                            ItemModelUtils.tintedModel(
                                    paintBrushTemplateDualLayer.create(
                                            ModelLocationUtils.getModelLocation(ModItems.PAINT_BRUSH, "/stalk_emissive_" + opacity),
                                            TextureMapping.layered(
                                                    TextureMapping.getItemTexture(ModItems.PAINT_BRUSH, "/emissive_" + opacity),
                                                    TextureMapping.getItemTexture(ModItems.PAINT_BRUSH, "/stalk")
                                            ),
                                            itemModelGenerators.modelOutput
                                    ),
                                    new RGBColorTintSource(-2172773) // match yellow color of the brush itself
                            )
                    )
            );
        }

        ItemModel.Unbaked paintBrushStalk = ItemModelUtils.plainModel(
                itemModelGenerators.createFlatItemModel(
                        ModItems.PAINT_BRUSH,
                        "/stalk",
                        paintBrushTemplateSingleLayer
                )
        );

        List<RangeSelectItemModel.Entry> paints = new ArrayList<>();
        for (int opacity = 10; opacity <= 100; opacity += 10) {
            paints.add(
                    new RangeSelectItemModel.Entry(opacity - 10, // -10 cause "Will select last entry with threshold less or equal to property value"
                            ItemModelUtils.tintedModel(
                                    itemModelGenerators.createFlatItemModel(
                                            ModItems.PAINT_BRUSH,
                                            "/paint_" + opacity,
                                            paintBrushTemplateSingleLayer
                                    ),
                                    new RGBColorTintSource(0)
                            )
                    )
            );
        }

        itemModelGenerators.itemModelOutput.accept(
                ModItems.PAINT_BRUSH,
                ItemModelUtils.composite(
                        ItemModelUtils.conditional(
                                PaintBrushPredicates.isEmissive(),
                                ItemModelUtils.rangeSelect(new PaintOpacity(), emissives),
                                paintBrushStalk
                        ),
                        ItemModelUtils.conditional(
                               PaintBrushPredicates.hasColor(),
                                ItemModelUtils.rangeSelect(new PaintOpacity(), paints),
                                new EmptyModel.Unbaked()
                        )
                )
        );
    }
}

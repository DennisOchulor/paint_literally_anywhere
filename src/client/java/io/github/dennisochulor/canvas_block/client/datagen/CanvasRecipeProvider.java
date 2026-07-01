package io.github.dennisochulor.canvas_block.client.datagen;

import io.github.dennisochulor.canvas_block.CanvasMod;
import io.github.dennisochulor.canvas_block.block.ModBlocks;
import io.github.dennisochulor.canvas_block.item.ModComponents;
import io.github.dennisochulor.canvas_block.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class CanvasRecipeProvider extends FabricRecipeProvider {
    public CanvasRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                //noinspection unused
                HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

                shaped(RecipeCategory.MISC, ModBlocks.CANVAS)
                        .define('C', Items.CONCRETE.white())
                        .define('W', Items.WOOL.white())
                        .pattern("WWW")
                        .pattern("WCW")
                        .pattern("WWW")
                        .unlockedBy(getHasName(Items.WOOL.white()), has(Items.WOOL.white()))
                        .group("canvas")
                        .save(output);

                shaped(RecipeCategory.MISC, ModItems.PAINT_BRUSH)
                        .define('F', Items.FEATHER)
                        .define('S', Items.STICK)
                        .pattern(" F ")
                        .pattern(" S ")
                        .pattern(" S ")
                        .unlockedBy(getHasName(Items.FEATHER), has(Items.FEATHER))
                        .group("canvas")
                        .save(output);

                shapeless(RecipeCategory.MISC,
                        new ItemStackTemplate(ModItems.PAINT_BRUSH, DataComponentPatch.builder().set(ModComponents.EMISSIVE, Unit.INSTANCE).build()))
                        .requires(ModItems.PAINT_BRUSH)
                        .requires(Items.GLOW_INK_SAC)
                        .unlockedBy(getHasName(Items.GLOW_INK_SAC), has(Items.GLOW_INK_SAC))
                        .unlockedBy(getHasName(ModItems.PAINT_BRUSH), has(ModItems.PAINT_BRUSH))
                        .group("canvas")
                        .save(output, CanvasMod.MOD_ID + ":paint_brush_emissive");
            }
        };
    }

    @Override
    public String getName() {
        return CanvasRecipeProvider.class.getSimpleName();
    }
}

package io.github.dennisochulor.playground.client.canvas.datagen;

import io.github.dennisochulor.playground.canvas.CanvasMod;
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

                shaped(RecipeCategory.MISC, CanvasMod.CANVAS)
                        .define('C', Items.CONCRETE.white())
                        .define('W', Items.WOOL.white())
                        .pattern("WWW")
                        .pattern("WCW")
                        .pattern("WWW")
                        .unlockedBy(getHasName(Items.WOOL.white()), has(Items.WOOL.white()))
                        .group("canvas")
                        .save(output);

                shaped(RecipeCategory.MISC, CanvasMod.PAINT_BRUSH)
                        .define('F', Items.FEATHER)
                        .define('S', Items.STICK)
                        .pattern(" F ")
                        .pattern(" S ")
                        .pattern(" S ")
                        .unlockedBy(getHasName(Items.FEATHER), has(Items.FEATHER))
                        .group("canvas")
                        .save(output);

                shapeless(RecipeCategory.MISC,
                        new ItemStackTemplate(CanvasMod.PAINT_BRUSH, DataComponentPatch.builder().set(CanvasMod.EMISSIVE, Unit.INSTANCE).build()))
                        .requires(CanvasMod.PAINT_BRUSH)
                        .requires(Items.GLOW_INK_SAC)
                        .unlockedBy(getHasName(Items.GLOW_INK_SAC), has(Items.GLOW_INK_SAC))
                        .unlockedBy(getHasName(CanvasMod.PAINT_BRUSH), has(CanvasMod.PAINT_BRUSH))
                        .group("canvas")
                        .save(output, "playground:paint_brush_emissive"); // todo
            }
        };
    }

    @Override
    public String getName() {
        return CanvasRecipeProvider.class.getSimpleName();
    }
}

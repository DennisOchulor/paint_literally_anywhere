package io.github.dennisochulor.paint_literally_anywhere.client.datagen;

import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                //noinspection unused
                HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

                shaped(RecipeCategory.TOOLS, ModItems.PAINT_BRUSH)
                        .define('F', Items.FEATHER)
                        .define('S', Items.STICK)
                        .pattern("FFF")
                        .pattern(" S ")
                        .pattern(" S ")
                        .unlockedBy(getHasName(Items.FEATHER), has(Items.FEATHER))
                        .group("paint_brush")
                        .save(output);
            }
        };
    }

    @Override
    public String getName() {
        return ModRecipeProvider.class.getSimpleName();
    }
}

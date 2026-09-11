package io.github.dennisochulor.paint_literally_anywhere.client.datagen;

import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        return new RecipeProvider(recipes, advancements) {
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

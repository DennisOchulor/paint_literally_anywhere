package io.github.dennisochulor.paint_literally_anywhere.client.datagen;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.TransmuteRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

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

                shaped(RecipeCategory.MISC, ModItems.PAINT_BRUSH)
                        .define('F', Items.FEATHER)
                        .define('S', Items.STICK)
                        .pattern(" F ")
                        .pattern(" S ")
                        .pattern(" S ")
                        .unlockedBy(getHasName(Items.FEATHER), has(Items.FEATHER))
                        .group("paint_brush")
                        .save(output);

                TransmuteRecipeBuilder.transmute(
                        RecipeCategory.MISC,
                        Ingredient.of(ModItems.PAINT_BRUSH),
                        Ingredient.of(Items.GLOW_INK_SAC),
                        new ItemStackTemplate(ModItems.PAINT_BRUSH, DataComponentPatch.builder().set(ModComponents.EMISSIVE, Unit.INSTANCE).build()))
                        .unlockedBy(getHasName(Items.GLOW_INK_SAC), has(Items.GLOW_INK_SAC))
                        .unlockedBy(getHasName(ModItems.PAINT_BRUSH), has(ModItems.PAINT_BRUSH))
                        .group("paint_brush")
                        .save(output, PLAMod.MOD_ID + ":paint_brush_emissive");

                shaped(RecipeCategory.MISC, ModItems.PALETTE)
                        .define('W', Items.WOOL.white())
                        .define('D', ItemTags.DYES)
                        .pattern("DDD")
                        .pattern("WWW")
                        .pattern("   ")
                        .unlockedBy(getHasName(Items.WOOL.white()), has(Items.WOOL.white()))
                        .save(output);
            }
        };
    }

    @Override
    public String getName() {
        return ModRecipeProvider.class.getSimpleName();
    }
}

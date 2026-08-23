package io.github.dennisochulor.paint_literally_anywhere.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {
    private ModItems() {}

    public static final Item PAINT_BRUSH = register(
            ModItemIds.PAINT_BRUSH,
            PaintBrushItem::new,
            new Item.Properties().durability(512).component(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT)
    );



    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> {
            tab.accept(PAINT_BRUSH);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private static <T extends Item> T register(ResourceKey<Item> itemKey, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        // Create the item instance.
        T item = itemFactory.apply(settings.setId(itemKey));
        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
}

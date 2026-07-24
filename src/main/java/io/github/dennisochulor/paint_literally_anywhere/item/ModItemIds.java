package io.github.dennisochulor.paint_literally_anywhere.item;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItemIds {
    private ModItemIds() {}


    public static final ResourceKey<Item> PALETTE = register("palette");
    public static final ResourceKey<Item> PAINT_BRUSH = register("paint_brush");


    private static ResourceKey<Item> register(String name) {
        // Create the item key.
        return ResourceKey.create(Registries.ITEM, PLAMod.id(name));
    }
}

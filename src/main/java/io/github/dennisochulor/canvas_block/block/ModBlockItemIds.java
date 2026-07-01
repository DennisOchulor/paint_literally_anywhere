package io.github.dennisochulor.canvas_block.block;

import io.github.dennisochulor.canvas_block.CanvasMod;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public final class ModBlockItemIds {
    private ModBlockItemIds() {}


    public static final BlockItemId CANVAS = register("canvas");


    private static BlockItemId register(String name) {
        Identifier id = CanvasMod.id(name);
        return BlockItemId.create(id, id);
    }
}

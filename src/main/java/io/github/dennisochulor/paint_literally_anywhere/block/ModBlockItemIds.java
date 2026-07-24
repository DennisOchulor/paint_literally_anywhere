package io.github.dennisochulor.paint_literally_anywhere.block;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;

public final class ModBlockItemIds {
    private ModBlockItemIds() {}


    private static BlockItemId register(String name) {
        Identifier id = PLAMod.id(name);
        return BlockItemId.create(id, id);
    }
}

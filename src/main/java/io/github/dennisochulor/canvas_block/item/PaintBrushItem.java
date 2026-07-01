package io.github.dennisochulor.canvas_block.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;

public class PaintBrushItem extends Item {
    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        int rgb = itemStack.getComponents().getOrDefault(ModComponents.RGB_COLOR, Color.WHITE.getRGB());
        boolean emissive = itemStack.has(ModComponents.EMISSIVE);
        return Component.translatable(
                emissive ? "item.canvas_block.paint_brush.emissive" : "item.canvas_block.paint_brush")
                .withColor(rgb);
    }
}

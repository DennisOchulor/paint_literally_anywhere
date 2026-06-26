package io.github.dennisochulor.playground.canvas;

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
        int rgb = itemStack.getComponents().getOrDefault(CanvasMod.RGB_COLOR, Color.WHITE.getRGB());
        return Component.translatable("item.playground.paint_brush").withColor(rgb);
    }
}

package io.github.dennisochulor.paint_literally_anywhere.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record RGBColorTintSource(int defaultColor) implements ItemTintSource {
    public static final Identifier ID = PLAMod.id("tint/rgb_color");

    public static final MapCodec<RGBColorTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ExtraCodecs.RGB_COLOR_CODEC.fieldOf("defaultColor").forGetter(RGBColorTintSource::defaultColor)).apply(instance, RGBColorTintSource::new)
    );

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
        // make it opaque as opacity is handled by the model itself (see datagen)
        return properties.hasActualColor() ? ARGB.opaque(properties.argb()) : defaultColor;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}

package io.github.dennisochulor.paint_literally_anywhere.client.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record PaintBrushPredicates(boolean hasEmissive, boolean hasActualColor) implements ConditionalItemModelProperty {
    public static final MapCodec<PaintBrushPredicates> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                            Codec.BOOL.fieldOf("hasEmissive").forGetter(PaintBrushPredicates::hasEmissive),
                            Codec.BOOL.fieldOf("hasActualColor").forGetter(PaintBrushPredicates::hasActualColor)
                    )
                    .apply(i, PaintBrushPredicates::new)
    );

    public static PaintBrushPredicates isEmissive() {
        return new PaintBrushPredicates(true, false);
    }

    public static PaintBrushPredicates hasColor() {
        return new PaintBrushPredicates(false, true);
    }

    @Override
    public boolean get(
            final ItemStack itemStack, final @Nullable ClientLevel level, final @Nullable LivingEntity owner, final int seed, final ItemDisplayContext displayContext
    ) {
        PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
        return (!hasEmissive || properties.emissive()) && (!hasActualColor || properties.hasActualColor());
    }

    @Override
    public MapCodec<PaintBrushPredicates> type() {
        return MAP_CODEC;
    }
}

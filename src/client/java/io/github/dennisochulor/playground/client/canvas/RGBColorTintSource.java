package io.github.dennisochulor.playground.client.canvas;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.playground.Playground;
import io.github.dennisochulor.playground.canvas.CanvasMod;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record RGBColorTintSource(int color) implements ItemTintSource {
    public static final Identifier ID = Playground.id("tint/rgb_color");

    public static final MapCodec<RGBColorTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ExtraCodecs.RGB_COLOR_CODEC.fieldOf("color").forGetter(RGBColorTintSource::color)).apply(instance, RGBColorTintSource::new)
    );

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        return itemStack.getComponents().getOrDefault(CanvasMod.RGB_COLOR, color);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return MAP_CODEC;
    }
}

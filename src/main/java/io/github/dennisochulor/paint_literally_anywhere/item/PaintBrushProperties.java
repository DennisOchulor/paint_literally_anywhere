package io.github.dennisochulor.paint_literally_anywhere.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

public record PaintBrushProperties(
        int argb,
        boolean emissive,
        int brushSize,
        int resolution,
        Tool tool
) implements TooltipProvider {

    public enum Tool implements StringRepresentable {
        BRUSH("Paints according to brush size", true),
        FILL("Fills all adjacent pixels of the same color", true),
        ERASER("Removes paint, does not consume durability", false);

        public final Component component;
        public final boolean consumesDurability;

        Tool(String tooltip, boolean consumesDurability) {
            this.component = Component.literal(tooltip);
            this.consumesDurability = consumesDurability;
        }

        @Override
        public String getSerializedName() {
            return this.name();
        }
    }

    public static final Codec<PaintBrushProperties> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(
                Codec.INT.fieldOf("argb").forGetter(PaintBrushProperties::argb),
                Codec.BOOL.fieldOf("emissive").forGetter(PaintBrushProperties::emissive),
                Codec.INT.fieldOf("brushSize").forGetter(PaintBrushProperties::brushSize),
                Codec.INT.fieldOf("resolution").forGetter(PaintBrushProperties::resolution),
                StringRepresentable.fromEnum(Tool::values).fieldOf("tool").forGetter(PaintBrushProperties::tool)
        ).apply(builder, PaintBrushProperties::new)
    );

    public static final StreamCodec<ByteBuf, PaintBrushProperties> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PaintBrushProperties::argb,
            ByteBufCodecs.BOOL, PaintBrushProperties::emissive,
            ByteBufCodecs.INT, PaintBrushProperties::brushSize,
            ByteBufCodecs.INT, PaintBrushProperties::resolution,
            ByteBufCodecs.idMapper(id -> Tool.values()[id], Tool::ordinal), PaintBrushProperties::tool,
            PaintBrushProperties::new
    );

    public static final int MIN_BRUSH_SIZE = 1;
    public static final int MAX_BRUSH_SIZE = 10;
    public static final int[] RESOLUTION_VALUES = {8, 16, 32, 64, 128};
    private static final Set<Integer> RESOLUTION_VALUES_SET = Set.of(8, 16, 32, 64, 128);
    public static final int EMPTY_COLOR = 0;
    public static final PaintBrushProperties DEFAULT = new PaintBrushProperties(EMPTY_COLOR, false, 1, 16, Tool.BRUSH);



    public PaintBrushProperties {
        if (brushSize < MIN_BRUSH_SIZE || brushSize > MAX_BRUSH_SIZE) {
            throw new IllegalArgumentException("Brush size must be >%d and <%d".formatted(MIN_BRUSH_SIZE, MAX_BRUSH_SIZE));
        }
        if (!RESOLUTION_VALUES_SET.contains(resolution)) {
            throw new IllegalArgumentException("Resolution must be one of " + Arrays.toString(RESOLUTION_VALUES));
        }
    }

    public boolean hasActualColor() {
        return argb != EMPTY_COLOR;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        if (hasActualColor()) {
            int opacity = (int) Math.round(ARGB.alpha(argb) / 255.0 * 100);
            String hex = String.format("#%02x%02x%02x", ARGB.red(argb), ARGB.green(argb), ARGB.blue(argb)).toUpperCase(Locale.ROOT);
            consumer.accept(Component.literal("Color: ").append(Component.literal(hex).withColor(argb)).append(" (" + opacity + "%)"));
        }

        consumer.accept(Component.literal("Tool: " + tool));
        consumer.accept(Component.literal("Brush Size: " + brushSize));
        consumer.accept(Component.literal("Resolution: " + resolution));
    }


    // Withers (that one JEP could ship anytime now!)
    public PaintBrushProperties withArgb(int argb) {
        return new PaintBrushProperties(argb, emissive, brushSize, resolution, tool);
    }

    public PaintBrushProperties withEmissive(boolean emissive) {
        return new PaintBrushProperties(argb, emissive, brushSize, resolution, tool);
    }

    public PaintBrushProperties withBrushSize(int brushSize) {
        return new PaintBrushProperties(argb, emissive, brushSize, resolution, tool);
    }

    public PaintBrushProperties withResolution(int resolution) {
        return new PaintBrushProperties(argb, emissive, brushSize, resolution, tool);
    }

    public PaintBrushProperties withTool(Tool tool) {
        return new PaintBrushProperties(argb, emissive, brushSize, resolution, tool);
    }
}

package io.github.dennisochulor.paint_literally_anywhere.item;

import com.mojang.datafixers.util.Pair;
import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintPacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaintBrushItem extends Item {
    private static final Map<Color, Item> COLOR_TO_DYE_MAP;

    static {
        // DyeColor.VALUES.textColor is RGB with max alpha
        Map<Color, Item> map = new HashMap<>(DyeColor.VALUES.size());
        DyeColor.VALUES.forEach(dyeColor -> {
            Color color = new Color(dyeColor.getTextColor());
            map.put(color, Items.DYE.pick(dyeColor));
        });
        COLOR_TO_DYE_MAP = Map.copyOf(map);
    }

    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    public record PaintResult(int[] pixelsPainted, @Nullable String error) {}

    public static void handlePacket(ServerboundPaintPacket packet, ServerPlayer player) {
        ServerLevel level = player.level();
        BlockPos pos = packet.pos();
        ItemStack itemStack = player.getMainHandItem();
        PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);

        // sanity checks - some are copied from vanilla's ServerboundUseItemOnPacket handling
        if (!player.connection.hasClientLoaded() ||
                !player.isWithinBlockInteractionRange(pos, 1.0F) ||
                !level.mayInteract(player, pos) ||
                !itemStack.is(ModItems.PAINT_BRUSH) ||
                properties.argb() == PaintBrushProperties.EMPTY_COLOR ||
                // ensure the clipped quad actually exist for the block state
                !((BlockStateBaseExt) level.getBlockState(pos)).pla$quads(level, pos).contains(packet.template())
        ) {
            return; // simply ignore nonsense packet
        }

        Vector3fc localHitPos = QuadTemplate.localize(packet.hitPos(), pos, packet.template().direction());
        int remainingDurability = itemStack.getMaxDamage() - itemStack.getDamageValue();
        PaintResult result = ChunkCanvasData.paintServer(level.getChunkAt(pos), packet.template(), pos, localHitPos, properties, remainingDurability, !player.isCreative());

        if (properties.tool().consumesDurability && result.pixelsPainted().length > 0) {
            itemStack.hurtAndBreak(result.pixelsPainted().length, player, InteractionHand.MAIN_HAND);
        }
        if (result.error() != null) {
            player.sendOverlayMessage(Component.literal(result.error()));
        }
    }

    @Override
    public Component getName(ItemStack itemStack) {
        PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
        return Component.translatable(
                properties.emissive() ? "item.paint_literally_anywhere.paint_brush.emissive" : "item.paint_literally_anywhere.paint_brush")
                .withColor(properties.argb() == PaintBrushProperties.EMPTY_COLOR ? Color.WHITE.getRGB() : properties.argb());
    }

    @Override
    public boolean canDestroyBlock(ItemStack itemStack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.CONSUME;
    }

    public Pair<ItemStack, ItemStack> getRequiredDyes(Color requestedColor) {
        // sort from closest to furthest from requestedColor
        List<Color> sortedDyeColors = COLOR_TO_DYE_MAP.keySet().stream()
                .sorted(Comparator.comparingDouble(color -> approxDistanceBetweenRGBValues(color, requestedColor))).toList();
        Color firstDyeColor = sortedDyeColors.getFirst();
        Color secondDyeColor = sortedDyeColors.get(1);

        ItemStack first = new ItemStack(COLOR_TO_DYE_MAP.get(firstDyeColor));
        boolean needsSecondDye = approxDistanceBetweenRGBValues(firstDyeColor, requestedColor) > 150;
        return Pair.of(first, needsSecondDye ? new ItemStack(COLOR_TO_DYE_MAP.get(secondDyeColor)) : ItemStack.EMPTY);
    }

    // https://stackoverflow.com/a/9085524
    private static double approxDistanceBetweenRGBValues(Color color1, Color color2) {
        long rmean = ( (long) color1.getRed() + (long) color2.getRed() ) / 2;
        long r = (long) color1.getRed() - (long) color2.getRed();
        long g = (long) color1.getGreen() - (long) color2.getGreen();
        long b = (long) color1.getBlue() - (long) color2.getBlue();
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
    }
}

package io.github.dennisochulor.paint_literally_anywhere.item;

import com.mojang.datafixers.util.Pair;
import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintPacket;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintbrushUpdatePacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PaintBrushItem extends Item {
    private static final Map<Color, Item> COLOR_TO_DYE_MAP;

    static {
        // DyeColor.VALUES.textColor is RGB with max alpha
        Map<Color, Item> map = new HashMap<>(DyeColor.VALUES.size());
        DyeColor.VALUES.forEach(dyeColor -> {
            Color color = new Color(dyeColor.getTextureDiffuseColor());
            map.put(color, Items.DYE.pick(dyeColor));
        });
        COLOR_TO_DYE_MAP = Map.copyOf(map);
    }

    public PaintBrushItem(Properties properties) {
        super(properties);
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



    public record PaintResult(int[] pixelsPainted, @Nullable String error) {}

    public static void handlePaintPacket(ServerboundPaintPacket packet, ServerPlayer player) {
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

    public static void handlePaintbrushUpdatePacket(ServerboundPaintbrushUpdatePacket packet, Player player) {
        ItemStack itemStack = player.getMainHandItem();
        Inventory inventory = player.getInventory();

        if (itemStack.getItem() != ModItems.PAINT_BRUSH) {
            PLAMod.LOGGER.warn("Received paintbrush update packet from player {} not holding paintbrush in main hand!", player);
            return;
        }

        PaintBrushProperties originalProperties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
        PaintBrushProperties newProperties = packet.properties();

        if (!player.hasInfiniteMaterials()) {
            boolean needsInkSac = !originalProperties.emissive() && newProperties.emissive();
            boolean needsDyes = originalProperties.argb() != newProperties.argb();
            Pair<ItemStack, ItemStack> requiredDyes;

            int inkSacSlot = -2;
            int firstDyeSlot = -2;
            int secondDyeSlot = -2;

            if (needsInkSac) {
                inkSacSlot = inventory.findSlotMatchingItem(Items.GLOW_INK_SAC.getDefaultInstance());
                if (inkSacSlot == -1) {
                    PLAMod.LOGGER.warn("Received paintbrush update packet from player {} that needs glow ink sac but doesn't have one!", player);
                    return;
                }
            }
            if (needsDyes) {
                requiredDyes = getRequiredDyesIfNeeded(new Color(originalProperties.argb()), new Color(newProperties.argb()));
                if (!requiredDyes.getFirst().isEmpty()) {
                    firstDyeSlot = inventory.findSlotMatchingItem(requiredDyes.getFirst());
                }
                if (!requiredDyes.getSecond().isEmpty()) {
                    secondDyeSlot = inventory.findSlotMatchingItem(requiredDyes.getSecond());
                }

                if (firstDyeSlot == -1 || secondDyeSlot == -1) {
                    PLAMod.LOGGER.warn("Received paintbrush update packet from player {} that needs dyes but doesn't have the dyes!", player);
                    return;
                }
            }

            if (needsInkSac) inventory.removeItem(inkSacSlot, 1);
            if (needsDyes) {
                inventory.removeItem(firstDyeSlot, 1);
                if (secondDyeSlot != -2) inventory.removeItem(secondDyeSlot, 1);
            }
        }

        itemStack.set(ModComponents.PAINT_BRUSH, newProperties);
    }

    public static Pair<ItemStack, ItemStack> getRequiredDyesIfNeeded(Color oldColor, Color newColor) {
        Pair<ItemStack, ItemStack> oldDyes = getRequiredDyes(oldColor);
        Pair<ItemStack, ItemStack> newDyes = getRequiredDyes(newColor);

        // Find whether the old dyes have all the new dyes
        boolean hasFirst = newDyes.getFirst().isEmpty() || ItemStack.isSameItemSameComponents(newDyes.getFirst(), oldDyes.getFirst()) ||
                ItemStack.isSameItemSameComponents(newDyes.getFirst(), oldDyes.getSecond());

        boolean hasSecond = newDyes.getSecond().isEmpty() || ItemStack.isSameItemSameComponents(newDyes.getSecond(), oldDyes.getFirst()) ||
                ItemStack.isSameItemSameComponents(newDyes.getSecond(), oldDyes.getSecond());

        if (hasFirst) {
            return new Pair<>(hasSecond ? ItemStack.EMPTY : newDyes.getSecond(), ItemStack.EMPTY);
        }
        else {
            return new Pair<>(newDyes.getFirst(), hasSecond ? ItemStack.EMPTY : newDyes.getSecond());
        }
    }



    private static Pair<ItemStack, ItemStack> getRequiredDyes(Color requestedColor) {
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

    private static double xyz_approxDistanceBetweenRGBValues(Color color1, Color color2) {
        Function<Color, Vec3> rgbToXyz = color -> {
            Int2DoubleFunction convert = c -> {
                double result = c;
                result = result / 255;
                result = result > 0.04045 ? Math.pow(((result + 0.055) / 1.055), 2.4) : result / 12.92;
                result = result * 100;
                return result;
            };

            double red = convert.applyAsDouble(color.getRed());
            double green = convert.applyAsDouble(color.getGreen());
            double blue = convert.applyAsDouble(color.getBlue());
            double x = (red * 0.4124564) + (green * 0.3575761) + (blue * 0.1804375);
            double y = (red * 0.2126729) + (green * 0.7151522) + (blue * 0.0721750);
            double z = (red * 0.0193339) + (green * 0.1191920) + (blue * 0.9503041);
            return new Vec3(x, y, z);
        };

        Vec3 color1XYZ = rgbToXyz.apply(color1);
        Vec3 color2XYZ = rgbToXyz.apply(color2);

        return Math.sqrt(Math.pow(color1XYZ.x - color2XYZ.x, 2) + Math.pow(color1XYZ.y - color2XYZ.y, 2) + Math.pow(color1XYZ.z - color2XYZ.z, 2));
    }
}

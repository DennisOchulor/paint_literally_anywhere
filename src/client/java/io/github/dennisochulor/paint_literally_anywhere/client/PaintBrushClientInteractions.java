package io.github.dennisochulor.paint_literally_anywhere.client;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.client.screen.PaintBrushSettingsScreen;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItemIds;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintPacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockHitResultExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class PaintBrushClientInteractions {
    private PaintBrushClientInteractions() {}

    private static boolean init = false;
    private static @Nullable EyedropperState eyedropperState = null;


    private record EyedropperState(PaintBrushSettingsScreen screen, ItemStack paintBrush) {}


    public static void init() {
        if (init) return;
        init = true;

        ClientPreAttackCallback.EVENT.register((_, player, _) -> {
            ItemStack itemStack = player.getMainHandItem();

            if (!itemStack.is(ModItemIds.PAINT_BRUSH)) {
                return false;
            }

            if (handleEyedropper(player, itemStack)) {
                return true;
            }

            BlockHitResult blockHitResult = clipQuad(player);
            if (blockHitResult != null && ((BlockHitResultExt) blockHitResult).pla$clippedQuad() != null) {
                // clipped a quad, now tell the server with a custom packet
                ClientPlayNetworking.send(new ServerboundPaintPacket(blockHitResult));
            }

            return true;
        });

        ItemEvents.USE.register(PaintBrushClientInteractions::onRightClick);
        ItemEvents.USE_ON.register(context -> {
            if (context.getPlayer() != null) {
                return onRightClick(context.getLevel(), context.getPlayer(), context.getHand());
            }

            return null;
        });

        AtomicInteger count = new AtomicInteger();
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.player == null) return;

            ItemStack heldItem = minecraft.player.getMainHandItem();
            if (!heldItem.is(ModItemIds.PAINT_BRUSH)) {
                count.set(0);
                return;
            }

            if (eyedropperState != null) { // right clicking works too
                minecraft.player.sendOverlayMessage(Component.literal("Left click to pick color"));
                return;
            }

            if (count.getAndIncrement() == 0) {
                PaintBrushProperties properties = heldItem.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
                String text = "Size: %d   Tool: %s   Res: %d".formatted(properties.brushSize(), properties.tool().toString(), properties.resolution());
                minecraft.player.sendOverlayMessage(Component.literal(text));
            }

            if (count.get() >= 20) {
                // update every second, to give other overlay messages a chance
                count.set(0);
            }
        });
    }

    public static void useEyedropper(PaintBrushSettingsScreen settingsScreen, ItemStack paintBrush) {
        if (!paintBrush.is(ModItemIds.PAINT_BRUSH)) {
            throw new IllegalArgumentException("Passed in item stack is not a paint brush!");
        }

        eyedropperState = new EyedropperState(settingsScreen, paintBrush);
    }


    private static @Nullable InteractionResult onRightClick(Level level, Player player, InteractionHand interactionHand) {
        if (!level.isClientSide()) return null;
        if (interactionHand != InteractionHand.MAIN_HAND) return null;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.is(ModItemIds.PAINT_BRUSH)) return null;

        if (handleEyedropper((LocalPlayer) player, itemStack)) return InteractionResult.CONSUME;

        Minecraft.getInstance().gui.setScreen(new PaintBrushSettingsScreen(itemStack, player));
        return InteractionResult.CONSUME;
    }

    private static boolean handleEyedropper(LocalPlayer player, ItemStack paintBrush) {
        if (eyedropperState == null) return false;
        if (eyedropperState.paintBrush() != paintBrush) {
            eyedropperState = null;
            return false;
        }

        if (clipQuad(player) instanceof BlockHitResult blockHitResult) {
            QuadTemplate template = ((BlockHitResultExt) blockHitResult).pla$clippedQuad();
            BlockPos pos = blockHitResult.getBlockPos();

            if (template != null) {
                QuadInstance instance = ChunkCanvasData.getQuadInstance(player.level().getChunkAt(pos), pos, template);

                if (instance != null) {
                    QuadInstance.RowCol rowCol = instance.getRowCol(QuadTemplate.localize(blockHitResult.getLocation(), pos, template.direction()));
                    int index = instance.index(rowCol);
                    int argb = instance.pixels()[index];
                    boolean emissive = instance.emissiveData().get(index);

                    eyedropperState.screen().setColorFromEyedropper(argb, emissive);
                    eyedropperState = null;
                }
            }
        }

        return true;
    }

    private static @Nullable BlockHitResult clipQuad(LocalPlayer player) {
        return ScopedValue.where(ShapeUtil.USE_ACCURATE_SHAPE, player).call(() -> {
            if (player.raycastHitResult(1.0F, Objects.requireNonNull(Minecraft.getInstance().getCameraEntity())) instanceof BlockHitResult blockHitResult) {
                return blockHitResult;
            }

            return null;
        });
    }
}

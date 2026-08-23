package io.github.dennisochulor.paint_literally_anywhere.client;

import io.github.dennisochulor.paint_literally_anywhere.client.screen.PaintBrushSettingsScreen;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItemIds;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintPacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockHitResultExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class PaintBrushClientInteractions {
    private PaintBrushClientInteractions() {}

    private static boolean init = false;

    public static void init() {
        if (init) return;
        init = true;

        ClientPreAttackCallback.EVENT.register((minecraft, player, _) -> {
            ItemStack itemStack = player.getMainHandItem();

            if (itemStack.is(ModItemIds.PAINT_BRUSH)) {
                ScopedValue.where(ShapeUtil.USE_ACCURATE_SHAPE, player).run(() -> {
                    if (player.raycastHitResult(1.0F, Objects.requireNonNull(minecraft.getCameraEntity())) instanceof BlockHitResult blockHitResult) {
                        if (((BlockHitResultExt) blockHitResult).pla$clippedQuad() != null) {
                            // clipped a quad, now tell the server with a custom packet
                            ClientPlayNetworking.send(new ServerboundPaintPacket(blockHitResult));
                        }
                    }
                });
                return true;
            }

            return false;
        });

        ItemEvents.USE.register(PaintBrushClientInteractions::onRightClick);
        ItemEvents.USE_ON.register(context -> {
            if (context.getPlayer() != null) {
                return onRightClick(context.getLevel(), context.getPlayer(), context.getHand());
            }

            return null;
        });
    }

    private static @Nullable InteractionResult onRightClick(Level level, Player player, InteractionHand interactionHand) {
        if (!level.isClientSide()) return null;
        if (interactionHand != InteractionHand.MAIN_HAND) return null;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.is(ModItemIds.PAINT_BRUSH)) return null;

        PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
        Minecraft.getInstance().gui.setScreen(new PaintBrushSettingsScreen(properties));
        return InteractionResult.CONSUME;
    }
}

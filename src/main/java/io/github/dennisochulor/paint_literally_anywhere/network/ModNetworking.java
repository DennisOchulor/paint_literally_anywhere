package io.github.dennisochulor.paint_literally_anywhere.network;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import io.github.dennisochulor.paint_literally_anywhere.item.PaletteMenu;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;

public final class ModNetworking {
    private ModNetworking() {}

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPaintbrushUpdatePacket.TYPE, ServerboundPaintbrushUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPaletteMenuUpdatePacket.TYPE, ServerboundPaletteMenuUpdatePacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPaintbrushUpdatePacket.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            ItemStack itemStack = player.getMainHandItem();

            if (!player.isCreative()) return;
            if (itemStack.getItem() != ModItems.PAINT_BRUSH) {
                PLAMod.LOGGER.warn("Received paintbrush update packet from player {} not holding paintbrush in main hand!", player);
                return;
            }

            itemStack.set(ModComponents.RGB_COLOR, payload.rgb());

            if (payload.emissive()) itemStack.set(ModComponents.EMISSIVE, Unit.INSTANCE);
            else itemStack.remove(ModComponents.EMISSIVE);
        });

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPaletteMenuUpdatePacket.TYPE, ((payload, context) -> {
            if (context.player().containerMenu instanceof PaletteMenu menu) {
                menu.setRequestedColor(new Color(payload.rgb()));
            }
        }));
    }
}

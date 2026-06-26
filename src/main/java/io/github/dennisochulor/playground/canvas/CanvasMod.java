package io.github.dennisochulor.playground.canvas;

import io.github.dennisochulor.playground.Playground;
import io.github.dennisochulor.playground.Utils;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.awt.Color;

public class CanvasMod {
    public static final Block CANVAS = Utils.registerBlock(
            "canvas",
            CanvasBlock::new,
            BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE)
                    .strength(1),
            true
    );

    public static final BlockEntityType<CanvasBlockEntity> CANVAS_BLOCK_ENTITY =
            Utils.registerBE("canvas", CanvasBlockEntity::new, CANVAS);

    public static final DataComponentType<Integer> RGB_COLOR = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Playground.id("component/rgb_color"),
            DataComponentType.<Integer>builder().persistent(ExtraCodecs.RGB_COLOR_CODEC).networkSynchronized(ByteBufCodecs.RGB_COLOR).build()
    );

    public static final Item PAINT_BRUSH = Utils.registerItem("paint_brush", PaintBrushItem::new,
            new Item.Properties().durability(300).component(RGB_COLOR, Color.BLACK.getRGB()));


    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> {
            tab.accept(CANVAS.asItem());
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> {
            tab.accept(PAINT_BRUSH);
        });

        PayloadTypeRegistry.clientboundPlay().register(ClientboundCanvasUpdatePacket.TYPE, ClientboundCanvasUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundPaintbrushUpdatePacket.TYPE, ServerboundPaintbrushUpdatePacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundPaintbrushUpdatePacket.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!player.isCreative()) return;
            if (player.getMainHandItem().getItem() != PAINT_BRUSH) {
                Playground.LOGGER.warn("Received paintbrush update packet from player {} not holding paintbrush in main hand!", player);
                return;
            }

            player.getMainHandItem().set(RGB_COLOR, payload.rgb());
        });
    }
}

package io.github.dennisochulor.playground.canvas;

import io.github.dennisochulor.playground.Utils;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

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

    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> {
            tab.accept(CANVAS.asItem());
        });

        PayloadTypeRegistry.clientboundPlay().register(ClientboundCanvasUpdatePacket.TYPE, ClientboundCanvasUpdatePacket.STREAM_CODEC);
    }
}

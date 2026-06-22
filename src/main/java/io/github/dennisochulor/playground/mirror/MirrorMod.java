package io.github.dennisochulor.playground.mirror;

import io.github.dennisochulor.playground.Utils;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;


public class MirrorMod {
    public static final Block MIRROR = Utils.registerBlock(
            "mirror",
            MirrorBlock::new,
            BlockBehaviour.Properties.of()
                    .sound(SoundType.GLASS)
                    .strength(0.3F)
                    .isValidSpawn(Blocks::never)
                    .isRedstoneConductor(Blocks::never)
                    .isSuffocating(Blocks::never),
            true
    );

    public static final BlockEntityType<MirrorBlockEntity> MIRROR_BLOCK_ENTITY =
            Utils.registerBE("mirror", MirrorBlockEntity::new, MIRROR);

    public static void init() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> {
            tab.accept(MIRROR.asItem());
        });
    }
}

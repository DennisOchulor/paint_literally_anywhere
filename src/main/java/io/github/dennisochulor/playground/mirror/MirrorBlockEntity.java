package io.github.dennisochulor.playground.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MirrorBlockEntity extends BlockEntity {
    public MirrorBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(MirrorMod.MIRROR_BLOCK_ENTITY, worldPosition, blockState);
    }
}

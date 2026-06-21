package io.github.dennisochulor.playground.mirror;

import io.github.dennisochulor.playground.PlaygroundBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MirrorBlockEntity extends BlockEntity {
    public MirrorBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(PlaygroundBlockEntities.MIRROR_BLOCK_ENTITY, worldPosition, blockState);
    }
}

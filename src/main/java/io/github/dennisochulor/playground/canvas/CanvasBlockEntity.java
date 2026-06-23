package io.github.dennisochulor.playground.canvas;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CanvasBlockEntity extends BlockEntity {


    public CanvasBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(CanvasMod.CANVAS_BLOCK_ENTITY, worldPosition, blockState);
    }
}

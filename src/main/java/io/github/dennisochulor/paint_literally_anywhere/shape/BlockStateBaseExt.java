package io.github.dennisochulor.paint_literally_anywhere.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public interface BlockStateBaseExt {
    QuadTemplate[] pla$quads(BlockGetter level, BlockPos pos);
}

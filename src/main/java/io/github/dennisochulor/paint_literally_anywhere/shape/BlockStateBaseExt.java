package io.github.dennisochulor.paint_literally_anywhere.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import java.util.Set;

public interface BlockStateBaseExt {
    Set<QuadTemplate> pla$quads(BlockGetter level, BlockPos pos);
}

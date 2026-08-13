package io.github.dennisochulor.paint_literally_anywhere.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Set;

public interface BlockStateBaseExt {
    Set<QuadTemplate> pla$quads(Level level, BlockPos pos);
    void pla$clearCache();
}

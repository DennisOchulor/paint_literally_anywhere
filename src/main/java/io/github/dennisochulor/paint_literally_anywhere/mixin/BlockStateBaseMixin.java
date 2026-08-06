package io.github.dennisochulor.paint_literally_anywhere.mixin;

import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class BlockStateBaseMixin implements BlockStateBaseExt {
    @Shadow
    public abstract VoxelShape getShape(BlockGetter level, BlockPos pos);

    @Unique
    private @Nullable Set<QuadTemplate> pla$quads;

    @Override
    public Set<QuadTemplate> pla$quads(BlockGetter level, BlockPos pos) {
        if (pla$quads == null) {
            //noinspection DataFlowIssue - the cast is safe
            pla$quads = ShapeUtil.getQuadTemplates((BlockState) (Object) this, getShape(level, pos));
        }

        return pla$quads;
    }
}

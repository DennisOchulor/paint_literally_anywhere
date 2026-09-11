package io.github.dennisochulor.paint_literally_anywhere.mixin.moving;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.ModAttachmentTypes;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PistonMovingBlockEntity.class)
abstract class PistonMovingBlockEntityMixin {
    @WrapOperation(method = "finalTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean finalTick(Level level, BlockPos pos, BlockState state, Operation<Boolean> original) {
        boolean result = original.call(level, pos, state);

        if (level.isClientSide()) return result;

        if (((PistonMovingBlockEntity) (Object) this).removeAttached(ModAttachmentTypes.QUAD_INSTANCES) instanceof List<QuadInstance> instances) {
            ChunkCanvasData.addServer(level.getChunkAt(pos), pos, instances);
        }

        return result;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static boolean tick(Level level, BlockPos pos, BlockState blockState, int updateFlags,
                                Operation<Boolean> original, @Local(argsOnly = true, name = "entity") PistonMovingBlockEntity entity) {
        boolean result = original.call(level, pos, blockState, updateFlags);

        if (!level.isClientSide() && entity.removeAttached(ModAttachmentTypes.QUAD_INSTANCES) instanceof List<QuadInstance> instances) {
            ChunkCanvasData.addServer(level.getChunkAt(pos), pos, instances);
        }

        return result;
    }
}

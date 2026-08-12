package io.github.dennisochulor.paint_literally_anywhere.mixin.moving;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.ModAttachmentTypes;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PistonBaseBlock.class)
abstract class PistonBaseBlockMixin {
    @Definition(id = "setBlock", method = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    @Expression("?.setBlock(?, ?, 276)")
    @Inject(method = "triggerEvent", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void triggerEvent$saveQuads(BlockState state, Level level, BlockPos pos, int b0, int b1,
                                       CallbackInfoReturnable<Boolean> cir,
                                       @Share(value = "quads") LocalRef<List<QuadInstance>> quads)
    {
        ChunkCanvasData data = level.getChunkAt(pos).getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);
        if (data != null) {
            List<QuadInstance> instances = data.blocks().get(pos);
            if (instances != null) quads.set(new ArrayList<>(instances));
        }
    }

    @ModifyExpressionValue(method = "triggerEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity triggerEvent$setQuadsToBE(BlockEntity original, @Share(value = "quads") LocalRef<List<QuadInstance>> quads) {
        if (quads.get() instanceof List<QuadInstance> instances) {
            original.setAttached(ModAttachmentTypes.QUAD_INSTANCES, instances);
        }

        return original;
    }



    @Definition(id = "setBlock", method = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    @Expression("?.setBlock(?, ?, 324)")
    @WrapOperation(method = "moveBlocks", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
    private boolean moveBlocks$saveQuads0(Level level, BlockPos pos, BlockState blockState, int updateFlags,
                                         Operation<Boolean> original, @Share(value = "quads") LocalRef<List<QuadInstance>> quads,
                                         @Local(name = "pushDirection") Direction pushDirection)
    {
        BlockPos oriPos = pos.relative(pushDirection.getOpposite());

        ChunkCanvasData data = level.getChunkAt(oriPos).getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);
        if (data != null) {
            List<QuadInstance> instances = data.blocks().get(oriPos);
            if (instances != null) quads.set(new ArrayList<>(instances));
        }

        return original.call(level, pos, blockState, updateFlags);
    }

    @Definition(id = "setBlock", method = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    @Expression("?.setBlock(?, ?, 324)")
    @WrapOperation(method = "moveBlocks", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1))
    private boolean moveBlocks$saveQuads1(Level level, BlockPos pos, BlockState blockState, int updateFlags,
                                         Operation<Boolean> original, @Share(value = "quads") LocalRef<List<QuadInstance>> quads)
    {
        ChunkCanvasData data = level.getChunkAt(pos).getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);
        if (data != null) {
            List<QuadInstance> instances = data.blocks().get(pos);
            if (instances != null) quads.set(new ArrayList<>(instances));
        }

        return original.call(level, pos, blockState, updateFlags);
    }

    @ModifyExpressionValue(method = "moveBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity moveBlocks$setQuadsToBE(BlockEntity original, @Share(value = "quads") LocalRef<List<QuadInstance>> quads) {
        if (quads.get() instanceof List<QuadInstance> instances) {
            original.setAttached(ModAttachmentTypes.QUAD_INSTANCES, instances);
        }

        return original;
    }
}

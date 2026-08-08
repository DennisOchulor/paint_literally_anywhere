package io.github.dennisochulor.paint_literally_anywhere.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(BlockGetter.class)
public interface BlockGetterMixin {

    @ModifyExpressionValue(method = "lambda$clip$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;clipWithInteractionOverride(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private @Nullable BlockHitResult useQuadTemplatesForClip(
            BlockHitResult original,
            @Local(name = "blockState") BlockState blockState,
            @Local(name = "context", argsOnly = true) ClipContext context,
            @Local(name = "pos", argsOnly = true) BlockPos pos
    ) {
        if (!ShapeUtil.USE_ACCURATE_SHAPE.isBound()) {
            return original;
        }

        Level level = (Level) this;
        Set<QuadTemplate> templates = ((BlockStateBaseExt) blockState).pla$quads(level, pos);

        QuadTemplate clippedQuad = null;
        BlockHitResult blockHitResult = null;
        double distance = Double.MAX_VALUE;

        for (QuadTemplate quad : templates) {
            if (quad.clip(context.getFrom().toVector3f(), context.getTo().toVector3f(), pos) instanceof BlockHitResult result) {
                double d = context.getFrom().distanceToSqr(result.getLocation());

                if (clippedQuad == null || d < distance) {
                    clippedQuad = quad;
                    blockHitResult = result;
                    distance = d;
                }
            }
        }

        return blockHitResult;
    }

    @ModifyExpressionValue(method = "lambda$clip$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/VoxelShape;clip(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/BlockHitResult;"))
    private @Nullable BlockHitResult noFluidClip(BlockHitResult original) {
        return ShapeUtil.USE_ACCURATE_SHAPE.isBound() ? null : original;
    }
}

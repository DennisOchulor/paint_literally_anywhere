package io.github.dennisochulor.paint_literally_anywhere.mixin;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
abstract class LevelChunkMixin {
    @Shadow
    @Final
    private Level level;

    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void validateNewBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<@Nullable BlockState> cir) {
        if (level.isClientSide()) return;

        BlockState oldState = cir.getReturnValue();

        if (oldState != null) {
            ChunkCanvasData.removeServer((LevelChunk) (Object) this, pos, state);
        }
    }
}

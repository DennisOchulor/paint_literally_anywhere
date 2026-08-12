package io.github.dennisochulor.paint_literally_anywhere.mixin.moving;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.ModAttachmentTypes;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {
    @Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static void fall(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir,
                             @Local(name = "entity") FallingBlockEntity entity) {
        ChunkCanvasData data = level.getChunkAt(pos).getAttached(ModAttachmentTypes.CHUNK_CANVAS_DATA);
        if (data != null) {
            List<QuadInstance> instances = data.blocks().get(pos);
            if (instances != null) entity.setAttached(ModAttachmentTypes.QUAD_INSTANCES, new ArrayList<>(instances));
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void tick(CallbackInfo ci, @Local(name = "pos") BlockPos pos) {
        FallingBlockEntity entity = (FallingBlockEntity) (Object) this;

        if (!entity.level().isClientSide() && entity.removeAttached(ModAttachmentTypes.QUAD_INSTANCES) instanceof List<QuadInstance> instances) {
            ChunkCanvasData.addServer(entity.level().getChunkAt(pos), pos, instances);
        }
    }
}

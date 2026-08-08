package io.github.dennisochulor.paint_literally_anywhere.mixin;

import io.github.dennisochulor.paint_literally_anywhere.shape.BlockHitResultExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockHitResult.class)
abstract class BlockHitResultMixin implements BlockHitResultExt {
    @Unique
    private @Nullable QuadTemplate clippedQuad;

    @Override
    public @Nullable QuadTemplate pla$clippedQuad() {
        return clippedQuad;
    }

    @Override
    public void pla$setClippedQuad(QuadTemplate clippedQuad) {
        this.clippedQuad = clippedQuad;
    }
}

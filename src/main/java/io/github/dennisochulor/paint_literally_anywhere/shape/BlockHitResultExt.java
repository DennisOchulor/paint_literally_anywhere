package io.github.dennisochulor.paint_literally_anywhere.shape;

import org.jspecify.annotations.Nullable;

public interface BlockHitResultExt {
    @Nullable QuadTemplate pla$clippedQuad();
    void pla$setClippedQuad(QuadTemplate template);
}

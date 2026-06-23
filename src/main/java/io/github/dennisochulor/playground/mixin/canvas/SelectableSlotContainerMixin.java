package io.github.dennisochulor.playground.mixin.canvas;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.block.SelectableSlotContainer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(SelectableSlotContainer.class)
interface SelectableSlotContainerMixin {
    @ModifyExpressionValue(method = "getRelativeHitCoordinatesForBlockFace",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;empty()Ljava/util/Optional;", ordinal = 1))
    private static Optional<Vec2> modifyUpDown(Optional<Vec2> original,
                                               @Local(name = "relativeHit") Vec3 relativeHit)
    {
        return Optional.of(new Vec2((float) relativeHit.x(), (float) relativeHit.z()));
    }
}

package io.github.dennisochulor.paint_literally_anywhere.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintPacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockHitResultExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Shadow private int rightClickDelay;
    @Shadow public abstract @Nullable Entity getCameraEntity();

    @WrapOperation(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult usePaintBrush(MultiPlayerGameMode instance, LocalPlayer player, InteractionHand hand,
                                            BlockHitResult blockHit, Operation<InteractionResult> original)
    {
        if (!player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.PAINT_BRUSH)) {
            return original.call(instance, player, hand, blockHit);
        }

        rightClickDelay = 0;

        ScopedValue.where(ShapeUtil.USE_ACCURATE_SHAPE, Unit.INSTANCE).run(() -> {
            if (player.raycastHitResult(1.0F, Objects.requireNonNull(getCameraEntity())) instanceof BlockHitResult blockHitResult) {
                if (((BlockHitResultExt) blockHitResult).pla$clippedQuad() != null) {
                    // clipped a quad, now tell the server with a custom packet
                    ClientPlayNetworking.send(new ServerboundPaintPacket(blockHitResult));
                }
            }
        });

        // no need to send ServerboundUseItemOnPacket
        return InteractionResult.CONSUME;
    }
}

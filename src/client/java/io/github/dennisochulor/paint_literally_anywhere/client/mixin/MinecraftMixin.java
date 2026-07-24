package io.github.dennisochulor.paint_literally_anywhere.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Shadow
    private int rightClickDelay;

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private void removeRightClickDelayForPaintBrush(CallbackInfo ci, @Local(name = "heldItem") ItemStack heldItem) {
        if (heldItem.getItem() == ModItems.PAINT_BRUSH) {
            rightClickDelay = 0;
        }
    }
}

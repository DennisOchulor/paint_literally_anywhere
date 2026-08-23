package io.github.dennisochulor.paint_literally_anywhere.client;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.client.datagen.ModModelProvider;
import io.github.dennisochulor.paint_literally_anywhere.client.datagen.PaintBrushPredicates;
import io.github.dennisochulor.paint_literally_anywhere.client.debug.QuadsDebugRenderer;
import io.github.dennisochulor.paint_literally_anywhere.client.model.ModModelLoadingPlugin;
import io.github.dennisochulor.paint_literally_anywhere.client.screen.PaintBrushSettingsScreen;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItemIds;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintPacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockHitResultExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.debug.v1.renderer.DebugRendererRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Objects;

public class PLAModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new ModModelLoadingPlugin());
        ItemTintSources.ID_MAPPER.put(RGBColorTintSource.ID, RGBColorTintSource.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(PLAMod.id("paint_opacity"), ModModelProvider.PaintOpacity.MAP_CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(PLAMod.id("paint_brush_predicates"), PaintBrushPredicates.MAP_CODEC);

        ModClientNetworking.init();

        ClientPreAttackCallback.EVENT.register((minecraft, player, _) -> {
            ItemStack itemStack = player.getMainHandItem();

            if (itemStack.is(ModItemIds.PAINT_BRUSH)) {
                ScopedValue.where(ShapeUtil.USE_ACCURATE_SHAPE, player).run(() -> {
                    if (player.raycastHitResult(1.0F, Objects.requireNonNull(minecraft.getCameraEntity())) instanceof BlockHitResult blockHitResult) {
                        if (((BlockHitResultExt) blockHitResult).pla$clippedQuad() != null) {
                            // clipped a quad, now tell the server with a custom packet
                            ClientPlayNetworking.send(new ServerboundPaintPacket(blockHitResult));
                        }
                    }
                });
                return true;
            }

            return false;
        });

        ItemEvents.USE.register((level, player, interactionHand) -> {
            if (!level.isClientSide()) return null;
            if (interactionHand != InteractionHand.MAIN_HAND) return null;
            ItemStack itemStack = player.getItemInHand(interactionHand);
            if (!itemStack.is(ModItemIds.PAINT_BRUSH)) return null;

            PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
            Minecraft.getInstance().gui.setScreen(new PaintBrushSettingsScreen(properties));
            return InteractionResult.CONSUME;
        });

        // I hate this, necessary since diff servers may need diff cached quads
        // See ModAttachmentTypes.INACCURATE_NAMESPACES
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) ->
            BuiltInRegistries.BLOCK.forEach(block ->
                    block.getStateDefinition().getPossibleStates().forEach(state ->
                            ((BlockStateBaseExt) state).pla$clearCache()
                    )
            )
        );

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            DebugRendererRegistry.register(QuadsDebugRenderer.DUMMY, QuadsDebugRenderer::new);
        }
    }
}

package io.github.dennisochulor.paint_literally_anywhere.client;

import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.client.datagen.ModModelProvider;
import io.github.dennisochulor.paint_literally_anywhere.client.datagen.PaintBrushPredicates;
import io.github.dennisochulor.paint_literally_anywhere.client.debug.QuadsDebugRenderer;
import io.github.dennisochulor.paint_literally_anywhere.client.model.ModModelLoadingPlugin;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.debug.v1.renderer.DebugRendererRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.registries.BuiltInRegistries;

public class PLAModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new ModModelLoadingPlugin());
        ItemTintSources.ID_MAPPER.put(RGBColorTintSource.ID, RGBColorTintSource.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(PLAMod.id("paint_opacity"), ModModelProvider.PaintOpacity.MAP_CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(PLAMod.id("paint_brush_predicates"), PaintBrushPredicates.MAP_CODEC);

        ModClientNetworking.init();
        PaintBrushClientInteractions.init();

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

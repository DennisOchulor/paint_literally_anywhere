package io.github.dennisochulor.paint_literally_anywhere.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.dennisochulor.paint_literally_anywhere.ModMenuTypes;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.client.datagen.ModModelProvider;
import io.github.dennisochulor.paint_literally_anywhere.client.debug.QuadsDebugRenderer;
import io.github.dennisochulor.paint_literally_anywhere.client.model.ModModelLoadingPlugin;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.debug.v1.renderer.DebugRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.Locale;

public class PLAModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new ModModelLoadingPlugin());
        ItemTintSources.ID_MAPPER.put(RGBColorTintSource.ID, RGBColorTintSource.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(PLAMod.id("paint_opacity"), ModModelProvider.PaintOpacity.MAP_CODEC);
        MenuScreens.register(ModMenuTypes.PALETTE_MENU, PaletteScreen::new);

        ModClientNetworking.init();

        KeyMapping.Category keyBindingCategory = KeyMapping.Category.register(PLAMod.id("keybinds"));
        KeyMapping creativeColorPickerKeybind = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.paint_literally_anywhere.creative_color_picker",
                        InputConstants.Type.KEYSYM,
                        InputConstants.KEY_B,
                        keyBindingCategory
                )
        );

        ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
            if (!creativeColorPickerKeybind.consumeClick()) {
                return;
            }

            //noinspection StatementWithEmptyBody
            while (creativeColorPickerKeybind.consumeClick()); //consume additional presses

            LocalPlayer player = minecraft.player;
            if (player == null || !player.isCreative() || minecraft.gui.screen() != null) {
                return;
            }

            ItemStack itemStack = player.getMainHandItem();
            if (!itemStack.is(ModItems.PAINT_BRUSH)) {
                player.sendOverlayMessage(Component.literal("Hold a paint brush in your mainhand!"));
                return;
            }

            int argb = itemStack.getOrDefault(ModComponents.ARGB_COLOR, Color.RED.getRGB());
            boolean emissive = itemStack.has(ModComponents.EMISSIVE);
            minecraft.gui.setScreen(new CreativeColorPickerScreen(new Color(argb, true), emissive));
        });

        ItemTooltipCallback.EVENT.register((stack, _, _, lines) -> {
            if (stack.get(ModComponents.ARGB_COLOR) instanceof Integer argb) {
                int opacity = (int) Math.round(ARGB.alpha(argb) / 255.0 * 100);
                String hex = String.format("#%02x%02x%02x", ARGB.red(argb), ARGB.green(argb), ARGB.blue(argb)).toUpperCase(Locale.ROOT);

                lines.add(Component.literal("Color: ").append(Component.literal(hex).withColor(argb)));
                lines.add(Component.literal("Opacity: " + opacity + "%"));
            }
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

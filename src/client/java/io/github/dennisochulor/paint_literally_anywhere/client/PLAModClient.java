package io.github.dennisochulor.paint_literally_anywhere.client;

import io.github.dennisochulor.paint_literally_anywhere.ModMenuTypes;
import io.github.dennisochulor.paint_literally_anywhere.client.model.ModModelLoadingPlugin;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.Locale;

public class PLAModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(new ModModelLoadingPlugin());
        ItemTintSources.ID_MAPPER.put(RGBColorTintSource.ID, RGBColorTintSource.MAP_CODEC);
        MenuScreens.register(ModMenuTypes.PALETTE_MENU, PaletteScreen::new);

        ModClientNetworking.init();

        ItemEvents.USE.register((level, player, _) -> {
            ItemStack itemStack = player.getMainHandItem();

            if (itemStack.getItem() != ModItems.PAINT_BRUSH) return null;
            if (!level.isClientSide() || !player.isCreative() || !player.isCrouching()) return null;

            int argb = itemStack.getOrDefault(ModComponents.ARGB_COLOR, Color.YELLOW.getRGB());
            boolean emissive = itemStack.has(ModComponents.EMISSIVE);

            Minecraft.getInstance().gui.setScreen(new CreativeColorPickerScreen(new Color(argb, true), emissive));
            return InteractionResult.CONSUME;
        });

        ItemTooltipCallback.EVENT.register((stack, _, _, lines) -> {
            if (stack.get(ModComponents.ARGB_COLOR) instanceof Integer argb) {
                int opacity = (int) Math.round(ARGB.alpha(argb) / 255.0 * 100);
                String hex = "#" + Integer.toHexString(argb).substring(2).toUpperCase(Locale.ROOT);

                lines.add(Component.literal("Color: ").append(Component.literal(hex).withColor(argb)));
                lines.add(Component.literal("Opacity: " + opacity + "%"));
            }
        });
    }
}

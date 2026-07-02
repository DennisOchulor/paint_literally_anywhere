package io.github.dennisochulor.canvas_block;

import io.github.dennisochulor.canvas_block.block.ModBlockEntities;
import io.github.dennisochulor.canvas_block.block.ModBlocks;
import io.github.dennisochulor.canvas_block.item.ModComponents;
import io.github.dennisochulor.canvas_block.item.ModItems;
import io.github.dennisochulor.canvas_block.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class CanvasMod implements ModInitializer {
    public static final String MOD_ID = "canvas_block";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initialising Canvas Block Mod!");

        ModNetworking.init();
        ModComponents.init();
        ModItems.init();
        ModBlocks.init();
        ModBlockEntities.init();
        ModMenuTypes.init();

        AttackBlockCallback.EVENT.register((player, _, _, _, _) -> {
            // prevent accidentally breaking a canvas block while painting it
            // I can't count how many times I have made this mistake...
            if (player.isCreative() && player.getMainHandItem().is(ModItems.PAINT_BRUSH)) return InteractionResult.FAIL;
            else return InteractionResult.PASS;
        });


        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }
}

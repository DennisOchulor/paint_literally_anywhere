package io.github.dennisochulor.paint_literally_anywhere;

import io.github.dennisochulor.paint_literally_anywhere.block.ModBlockEntities;
import io.github.dennisochulor.paint_literally_anywhere.block.ModBlocks;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import io.github.dennisochulor.paint_literally_anywhere.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.player.BlockEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.context.UseOnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class PLAMod implements ModInitializer {
    public static final String MOD_ID = "paint_literally_anywhere";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initialising Paint Literally Anywhere Mod!");

        ModNetworking.init();
        ModComponents.init();
        ModItems.init();
        ModBlocks.init();
        ModBlockEntities.init();
        ModMenuTypes.init();
        ModAttachmentTypes.init();

        ServerChunkEvents.CHUNK_LOAD.register((_, chunk, generated) -> {
            if (!generated) ChunkCanvasData.validateOnChunkLoad(chunk);
        });

        // Force all right click interactions (sneak or not) with paint brush to attempt painting instead of interacting with block
        BlockEvents.USE_ITEM_ON.register((itemStack, _, _, _, player, hand, blockHitResult) -> {
            if (itemStack.is(ModItems.PAINT_BRUSH)) return ModItems.PAINT_BRUSH.useOn(new UseOnContext(player, hand, blockHitResult));
            else return null;
        });

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }
}

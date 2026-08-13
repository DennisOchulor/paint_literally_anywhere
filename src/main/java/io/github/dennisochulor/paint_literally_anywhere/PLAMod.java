package io.github.dennisochulor.paint_literally_anywhere;

import io.github.dennisochulor.paint_literally_anywhere.block.ModBlockEntities;
import io.github.dennisochulor.paint_literally_anywhere.block.ModBlocks;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import io.github.dennisochulor.paint_literally_anywhere.network.ModNetworking;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParseResult;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
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

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ShapeFileParseResult result;
            if (server.isDedicatedServer()) {
                result = ShapeFileParser.parse(server.getServerDirectory());
                ShapeUtil.setParseResult(result);
            }
            else { // we have a client, so parsing/generating was already done during model baking
                result = ShapeUtil.getParseResult();
            }

            server.globalAttachments().setAttached(ModAttachmentTypes.INACCURATE_NAMESPACES,
                    result.missingNamespaces(false).keySet());
        });

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }
}

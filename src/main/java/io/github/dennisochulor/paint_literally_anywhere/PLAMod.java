package io.github.dennisochulor.paint_literally_anywhere;

import com.mojang.datafixers.DSL;
import io.github.dennisochulor.paint_literally_anywhere.block.ModBlockEntities;
import io.github.dennisochulor.paint_literally_anywhere.item.ModComponents;
import io.github.dennisochulor.paint_literally_anywhere.item.ModItems;
import io.github.dennisochulor.paint_literally_anywhere.network.ModNetworking;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParseResult;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.HashSet;
import java.util.Set;

public class PLAMod implements ModInitializer {
    public static final String MOD_ID = "paint_literally_anywhere";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final DSL.TypeReference SHAPE_FILE_REFERENCE = References.reference(id("shape_file").toString());


    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    
    @Override
    public void onInitialize() {
        ModNetworking.init();
        ModComponents.init();
        ModItems.init();
        ModBlockEntities.init();
        ModAttachmentTypes.init();

        ItemComponentTooltipProviderRegistry.addFirst(ModComponents.PAINT_BRUSH);


        ServerChunkEvents.CHUNK_LOAD.register((_, chunk, generated) -> {
            if (!generated) ChunkCanvasData.validateOnChunkLoad(chunk);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ShapeFileParseResult result;

            if (server.isDedicatedServer()) {
                result = ShapeFileParser.parse(server.getServerDirectory());
                ShapeUtil.setParseResult(result);

                StringBuilder sb = new StringBuilder("\n[Paint Literally Anywhere (PLA)]\n");
                Set<String> missingShapeFiles = result.missingNamespaces(false).keySet();
                Set<String> maybeOutdatedShapeFiles = new HashSet<>(result.missingNamespaces(true).keySet());
                maybeOutdatedShapeFiles.removeAll(missingShapeFiles);

                boolean shouldLog = !missingShapeFiles.isEmpty() || !maybeOutdatedShapeFiles.isEmpty();
                if (!missingShapeFiles.isEmpty()) {
                    sb.append("Missing shape files: ");
                    missingShapeFiles.forEach(namespace -> sb.append(namespace).append(", "));
                    sb.append("\n");
                }
                if (!maybeOutdatedShapeFiles.isEmpty()) {
                    sb.append("Potentially outdated shape files: ");
                    maybeOutdatedShapeFiles.forEach(namespace -> sb.append(namespace).append(", "));
                    sb.append("\n");
                }

                if (shouldLog) {
                    sb.append("See https://modrinth.com/mod/paint-literally-anywhere#:~:text=Important%20note%20for%20Dedicated%20Servers for more info.\n");
                    LOGGER.warn(sb.toString());
                }
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

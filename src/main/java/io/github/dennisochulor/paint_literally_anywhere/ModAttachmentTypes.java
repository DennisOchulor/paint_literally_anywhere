package io.github.dennisochulor.paint_literally_anywhere;

import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import java.util.ArrayList;
import java.util.List;

public final class ModAttachmentTypes {
    private ModAttachmentTypes() {}

    public static final AttachmentType<ChunkCanvasData> CHUNK_CANVAS_DATA =
            AttachmentRegistry.create(PLAMod.id("chunk_canvas_data"),
                     builder ->
                             builder.persistent(ChunkCanvasData.CODEC)
            );

    public static final AttachmentType<List<QuadInstance>> QUAD_INSTANCES =
            AttachmentRegistry.create(PLAMod.id("quad_instances"),
                    builder ->
                            builder.persistent(QuadInstance.CODEC.listOf().xmap(ArrayList::new, List::copyOf))
            );


    public static void init() {}
}

package io.github.dennisochulor.paint_literally_anywhere;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ModAttachmentTypes {
    private ModAttachmentTypes() {}

    public static final AttachmentType<ChunkCanvasData> CHUNK_CANVAS_DATA =
            AttachmentRegistry.create(PLAMod.id("chunk_canvas_data"),
                     builder ->
                             builder.persistent(ChunkCanvasData.CODEC)
            );


    public static void init() {}
}
